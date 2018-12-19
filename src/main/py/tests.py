# Source code authorship analysis with SVM and syntax tree.
# Clarence Rodgers, Miles Krusniak
# Dr. Robert Frank, LING 227


# Utilities for reading and interpreting JSON and Java files
import json # json reader
import os # file io
import collections # general library
import re # regular expressions for stylistic components
import sys # also file io

# GenSim, for SVM/TF-IDF
from gensim import corpora, models, similarities
import gensim
from sklearn.svm import *
from sklearn.multiclass import *
from sklearn.preprocessing import LabelEncoder


# Utilities for plot drawing
from sklearn.manifold import TSNE # for vector flattening
import matplotlib.pyplot as plt # for plot drawing
import matplotlib.cm as cm # for plot drawing
import numpy as np # for general math

# A list of Java keywords (and the literals null, true, and false.)
# Used in some stylistic features.
java_keywords = ("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", 
    "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", 
    "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", 
    "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", 
    "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", 
    "const", "float", "native", "super", "while", "null", "true", "false")


# Unigram structural feature.
def count_nodes(parse, counts):
    if "struct/unitree/" + parse["name"] not in counts: counts["struct/unitree/" + parse["name"]] = 0
    counts["struct/unitree/" + parse["name"]] += 1

    if "children" in parse: 
        for c in parse["children"]:
            count_nodes(c, counts)
    return

def count_node_characteristics(parse, counts, depth=0):
    result = []
    if "struct/nchild/"+parse["name"] not in counts: counts["struct/nchild/"+parse["name"]] = 0
    if "struct/max_depth" not in counts: counts["struct/max_depth"] = []


    if "children" in parse: counts["struct/nchild/"+parse["name"]] += len(parse["children"])
    counts["struct/max_depth"] += [depth]

    if "children" in parse: 
        for c in parse["children"]:
            count_node_characteristics(c, counts, depth+1)

    # Processing once the recursive part is done
    if parse["name"] == "CompilationUnit":
        counts["struct/max_depth"] = max(counts["struct/max_depth"])
        for n in counts:
            if n.startswith("struct/nchild/"):
                v = n.replace("struct/nchild/", "")
                counts["struct/nchild/"+v] /= counts["struct/unitree/"+v]


def count_bitrees(parse, counts):
    result = []
    if "children" not in parse: return
    for c in parse["children"]:
        key = "struct/bitree/" + parse["name"] + "+" + c["name"]
        if key not in counts: counts[key] = 0
        counts[key] += 1
        count_bitrees(c, counts)

def count_tritrees(parse, counts):
    result = []
    if "children" not in parse: return
    for c in parse["children"]:
        if "children" not in c: continue
        for g in c["children"]: 
            key = "struct/tritree/" + parse["name"] + "+" + c["name"] + "+" + g["name"]
            if key not in counts: counts[key] = 0
            counts[key] += 1
            count_tritrees(g, counts)

def count_style(file, counts):
    single_style_markers = ("comments", "blocks", "d_braces", "blanks", "s_braces", "one_ifs", "opspace",
        "line_len", "num_lines", "var_len")
    # These aren't all the style markers; they're just the ones that aren't done in bulk
    for i in single_style_markers: counts["style/"+i] = 0

    block = False # We need to keep track whether we're in a block comment (so we don't accidentally 
    # measure stats about content that isn't part of the program)
    names = set([])

    for line in file:
        if "/*" in line: 
            counts["style/blocks"] += 1
            block = True
        if "*/" in line: block = False

        if not block:
            pure_line = re.sub(r" [0-9]+\w?", "", re.sub(r"\W", " ", re.sub(r"//.*", "",line)))

            if "//" in line: counts["style/comments"] += 1
            if line.strip() == '{': counts["style/d_braces"] += 1
            if line.strip() == '': counts["style/blanks"] += 1
            if " {" in line: counts["style/s_braces"] += 1 # Bing et. al. STY1b
            if re.search(r"if ?\(.+\) ?\S+", line): counts["style/one_ifs"] += 1
            if re.search(r" [\+\-\*/%=]=? ", line): counts["style/opspace"] += 1
            counts["style/num_lines"] += 1
            counts["style/line_len"] += len(line)

            for x in pure_line.split(" "):
                if x not in java_keywords and x is not '': names.add(x)

            for word in java_keywords:
                if word in line: 
                    if "style/keyword/"+word not in counts: counts["style/keyword/"+word] = 0
                    counts["style/keyword/"+word] += 1

    for name in names:
        counts["style/var_len"] += len(name)
    counts["style/var_len"] /= len(names)

    counts["style/line_len"] /= counts["style/num_lines"]




def get_depth(parse, current_depth):
    max_depth = current_depth
    if "children" in parse:
        children = parse["children"]
        for child in children:
            test_depth = get_depth(child, current_depth + 1)
            if test_depth > max_depth:
                max_depth = test_depth
                 
    return max_depth

def trim(vec):
    result = []
    for tup in vec:
        if tup[1] != 0:
            result.append(tup)

    return result


def untrim(vec, length):
    out_vec = []
    current_index = 0
    for (index, val) in vec:
        while index > current_index:
            out_vec.append(0)
            current_index+=1

        out_vec.append(val)
        current_index+=1
        
    while current_index < length:
        out_vec.append(0)
        current_index+=1

    return out_vec

MODE = "SVM" # "MAXSIM" or "SVM"


# Average accuracy for 10-fold cross validation is: 97.85%
# Average F-score for 10-fold cross validation is: 97.86%

countsCollection = []
base_labels = []
num_files = {}
max_num_files = 18

data_labels = ("mk", "kr")
filenames = {}
for label in data_labels: filenames[label] = []

print "Loading features..."

min_length = sys.maxint
for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    if min_length > len(os.listdir(data_dir)):
        min_length = len(os.listdir(data_dir))

for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    java_dir = (".."+os.sep)*3+"data"+os.sep+label
    i = 0
    for filename in os.listdir(data_dir):
        if i >= max_num_files: break
        filenames[label] += [label + "/" + filename.replace(".json", ".java")]
        if label not in num_files: num_files[label] = 0
        num_files[label] += 1
        with open(data_dir + os.sep + filename) as parseFile:
            with open(java_dir + os.sep + (filename.replace(".json", ".java"))) as javaFile:
                parse = json.load(parseFile)
                counts = {}
                count_nodes(parse, counts)
                count_bitrees(parse, counts)
                count_tritrees(parse, counts) 
                count_node_characteristics(parse, counts)
                counts["depth"] = get_depth(parse, 1)
                count_style(javaFile, counts)
                countsCollection.append(counts)
                base_labels.append(label)
        i += 1
    print "  for %s (%d files)" % (label, i)

types = []
for counts in countsCollection:
    for type in counts:
        types.append(type)

types = set(sorted(types))

max_length = len(types)

print "  %d features found." % max_length

base_corpus = []
for counts in countsCollection:
    counts_vec = []
    count_sum = 0
    for type in counts:
        count_sum+=counts[type]
    i = 0
    for type in types:
        count = 0
        if type in counts:
            count = float(counts[type])/count_sum

        counts_vec.append((i,count))
        i+=1

    base_corpus.append(counts_vec)

# Yeah, this will take up a lot of space...
if MODE=="MAXSIM": base_corpus = [trim(vec) for vec in base_corpus]


model = models.TfidfModel(base_corpus)
vector = model[base_corpus[0]]
modeled_vecs = [model[vec] for vec in base_corpus]

untrimmed_vecs = [untrim(vec, max_length) for vec in modeled_vecs] if MODE=="MAXSIM" else np.array([[y[1] for y in x] for x in base_corpus])

tsne = TSNE(n_components=2, init='random')
flattened = tsne.fit_transform(untrimmed_vecs)

prev_label = base_labels[0]
separated = []
separated.append([])
colors = cm.rainbow(np.linspace(0,1,len(data_labels)))

for label, l in zip(base_labels, flattened):
    if label != prev_label:
        prev_label = label
        separated.append([])

    separated[len(separated) - 1].append(l)

fig, ax = plt.subplots()


for l, color, data_label in zip(separated, colors, data_labels):
    x = [a[0] for a in l[:]]
    y = [a[1] for a in l[:]]
    col = ax.scatter(x, y, c = color, label=data_label, alpha = 0.3)
    
    # Sometimes it's nice to be able to locate files, so here's that:
    # for name, location in zip(filenames[data_label], l):
    #     print " File %s is at %2.0f, %2.0f" % (name, location[0], location[1])

ax.legend()
# plt.show()



n = 10 # number of subsamples
k = len(base_corpus)/n # number of elements per subsample

print "Calculating similarities (%d-fold, %d per sample)..." % (n, k)

accuracies = []
fscores = []
for i in xrange(0, n):

    confusion_matrix = {}
    for u in data_labels: 
        confusion_matrix[u] = {}
        for v in data_labels:
            confusion_matrix[u][v] = 0

    results = []
    test_vecs = base_corpus[i:len(base_corpus):n]
    if test_vecs == []: continue
    if len(test_vecs) < n:
        print "  stubbed iteration %d, used only %d vectors." % (i, len(test_vecs))
        

    # Separate the test data
    test_labels = []
    test_vecs = []
    labels = list(base_labels)
    corpus = list(base_corpus)
    for j in range(i, len(base_corpus), n):
        test_labels += [base_labels[j]]
        test_vecs += [base_corpus[j]]
        labels[j] = None
        corpus[j] = None

    # Get rid of the documents we added to the test data
    labels = [x for x in labels if x is not None]
    corpus = [x for x in corpus if x is not None]

    if MODE=="SVM":
        le = LabelEncoder()
        labels = le.fit_transform(labels)

        scikit_model = NuSVC(gamma='scale')
        # scikit_model = OneVsRestClassifier(estimator=SVC(gamma='scale', random_state=0, C=2))
        scikit_model.fit(np.array([[y[1] for y in x] for x in corpus]), np.array(labels))

        uvec = np.array([[u[1] for u in v] for v in test_vecs])
        guess = scikit_model.predict(uvec)
        print guess
        labels = le.inverse_transform(labels)
        guess = le.inverse_transform(guess)

        for real, guess in zip(test_labels, guess): 
            results.append(1 if guess == real else 0)
            confusion_matrix[guess][real] += 1


    if MODE == "MAXSIM":

        tfidf = models.TfidfModel(corpus)
        index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))
        gensim.models.tfidfmodel.smartirs_wglobal(18, 47, 't')

        results = []
        for j in range(0, len(test_vecs)):
            #print str(float(j)/len(test_vecs) * 100) + "% through fold " +  str(float(i)/n)
            test_vec = test_vecs[j]
            label = test_labels[j]
            sims = index[tfidf[test_vec]]
            max_index = 0
            max_similarity = 0
            for (sim_index, similarity) in list(enumerate(sims)):
                if float(similarity) > max_similarity:
                    max_similarity = float(similarity)
                    max_index = int(sim_index)

            confusion_matrix[str(labels[int(max_index)])][label] += 1   
            results.append(1 if labels[int(max_index)] == label else 0)


    if results != []:
        accuracy = float(sum(results)) / len(results)
        accuracies.append(accuracy)

    label_counts = {}
    for b in data_labels: label_counts[b] = str(confusion_matrix[b][b]) + "/" + str(test_labels.count(b))
    print "Tested group %d; accuracies " % i + str(label_counts)
    print "  Accuracy %d is: %2.2f%%" % (i, accuracy*100)
    
    # Precision: truepos/pos
    # Recall: truepos/(truepos+falseneg)

    recall = 0
    precision = 0
    fscore = 0
    used_labels = 0

    for label in set(test_labels):
        truepos = confusion_matrix[label][label]
        falsepos = sum([confusion_matrix[z][label] for z in data_labels if z != label])
        falseneg = sum([confusion_matrix[label][z] for z in data_labels if z != label])
        used_labels += 1

        if truepos != 0: 
            if precision == None: precision = 0
            if recall == None: recall = 0
            precision += truepos/float(truepos+falsepos)
            recall    += truepos/float(truepos+falseneg)

    if used_labels != 0:
        if precision == 0 or recall == 0: 
            fscore = 0
            fscores += [0]
        else:
            precision /= used_labels
            recall /= used_labels
            fscore = 1.0/(.5/precision + .5/recall)
            print "  Precision %d is: %2.2f%%" % (i, precision*100)
            print "  Recall %d is: %2.2f%%" % (i, recall*100)
            fscores += [fscore]

        print "  F-score %d is: %2.2f%%" % (i, fscore*100 if fscore is not None else -1)


print "------"
print "Average accuracy for %d-fold cross validation is: %2.2f%%" % (n, sum(accuracies) / float(len(accuracies))*100)
print "Average F-score for %d-fold cross validation is: %2.2f%%" % (n, sum(fscores) / float(len(fscores))*100)