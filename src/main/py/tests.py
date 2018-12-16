import json
import os
import collections
import re
import sys

from gensim import corpora, models, similarities
from sklearn.manifold import TSNE
import matplotlib.pyplot as plt
import matplotlib.cm as cm
import numpy as np

java_keywords = ("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", 
    "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", 
    "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", 
    "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", 
    "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", 
    "const", "float", "native", "super", "while", "null", "true", "false")

def count(parse, counts):
    for key in parse:
        if key == "name":
            name = parse[key]
            if name not in counts:
                counts[name] = 0

            counts[name]+=1
        else:
            children = parse[key]
            for child in children:
                count(child, counts)
    return

def count_bitrees(parse, counts):
    result = []
    if "children" not in parse: return
    for c in parse["children"]:
        key = parse["name"] + "+" + c["name"]
        if key not in counts: counts[key] = 0
        counts[key] += 1
        count_bitrees(c, counts)

def count_tritrees(parse, counts):
    result = []
    if "children" not in parse: return
    for c in parse["children"]:
        if "children" not in c: continue
        for g in c["children"]: 
            key = parse["name"] + "+" + c["name"] + "+" + g["name"]
            if key not in counts: counts[key] = 0
            counts[key] += 1
            count_tritrees(g, counts)

def count_style(file, counts):
    style_markers = ("comments", "block_comments", "dangling_braces", "blank_lines", "spaced_braces", "oneline_ifs", "spaced_ops",
        "line_len", "num_lines", "var_len", "numbers")
    for i in style_markers: counts[i] = 0
    block = False
    names = set([])

    for line in file:
        if "/*" in line: 
            counts["block_comments"] += 1
            block = True
        if "*/" in line: block = False

        if not block:
            pure_line = re.sub(r" [0-9]+\w?", "", re.sub(r"\W", " ", re.sub(r"//.*", "",line)))

            if "//" in line: counts["comments"] += 1
            if line.strip() == '{': counts["dangling_braces"] += 1
            if line.strip() == '': counts["blank_lines"] += 1
            if " {" in line: counts["spaced_braces"] += 1 # Bing et. al. STY1b
            if re.search(r"if ?\(.+\) ?\S+", line): counts["oneline_ifs"] += 1
            if re.search(r" [\+\-\*/%=]=? ", line): counts["spaced_ops"] += 1
            counts["numbers"] += len(re.findall(r"\W[0-9]+\w?", line))
            counts["num_lines"] += 1
            counts["line_len"] += len(line)

            for x in pure_line.split(" "):
                if x not in java_keywords and x is not '': names.add(x)

    for name in names:
        counts["var_len"] += len(name)
    counts["var_len"] /= len(names)

    counts["line_len"] /= counts["num_lines"]




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
    to_remove = []
    for tup in vec:
        if tup[1] == 0:
            to_remove.append(tup)

    for tup in to_remove:
        vec.remove(tup)

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

countsCollection = []
base_labels = []
num_files = {}

data_labels = ("mk", "kr", "camera", "bined", "turtle")

print "Loading features..."

min_length = sys.maxint
for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    if min_length > len(os.listdir(data_dir)):
        min_length = len(os.listdir(data_dir))

for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    java_dir = (".."+os.sep)*3+"data"+os.sep+label
    print "  for %s (%d files)" % (label, len(os.listdir(data_dir)))
    for filename in os.listdir(data_dir):
        if label not in num_files: num_files[label] = 0
        num_files[label] += 1
        with open(data_dir + os.sep + filename) as parseFile:
            with open(java_dir + os.sep + (filename.replace(".json", ".java"))) as javaFile:
                parse = json.load(parseFile)
                counts = {}
                count(parse, counts)
                count_bitrees(parse, counts)
                count_style(javaFile, counts)
                # count_tritrees(parse, counts) 
                counts["depth"] = get_depth(parse, 1)
                countsCollection.append(counts)
                base_labels.append(label)

types = []
for counts in countsCollection:
    for type in counts:
        types.append(type)

types = set(sorted(types))

max_length = len(types)

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

for vec in base_corpus:
    trim(vec)

model = models.TfidfModel(base_corpus)
vector = model[base_corpus[0]]
modeled_vecs = [model[vec] for vec in base_corpus]
untrimmed_vecs = [untrim(vec, max_length) for vec in modeled_vecs]

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
    ax.scatter(x, y, c = color, label=data_label, alpha = 0.3)

ax.legend()
plt.show()

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
    if len(test_vecs) < n:
        print "  stubbing iteration %d, only %d vectors remaining." % (i, len(test_vecs))
        

    test_labels = []
    test_vecs = []
    labels = list(base_labels)
    corpus = list(base_corpus)
    for j in range(i, len(base_corpus), n):
        test_labels += [base_labels[j]]
        test_vecs += [base_corpus[j]]
        labels[j] = None
        corpus[j] = None

    labels = [x for x in labels if x is not None]
    corpus = [x for x in corpus if x is not None]

    if len(labels) != len(corpus) or len(test_vecs) != len(test_labels): 
        print "Warning: length error incoming"
        print str(len(test_vecs)) + " elements in test_vecs: " + str(test_vecs)
        print str(len(test_labels)) + " elements in test_labels: " + str(test_labels)


    tfidf = models.TfidfModel(corpus)
    index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))

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

        if str(labels[int(max_index)]) == str(label):
            # print "correct"
            results.append(1)
            confusion_matrix[label][label] += 1
            # We don't really need to keep track of true negatives
        else:
            # print "incorrect"
            results.append(0)
            confusion_matrix[str(labels[int(max_index)])][label] += 1


    accuracy = float(sum(results)) / len(results)
    accuracies.append(accuracy)
    print "Accuracy %d is: %2.2f%%" % (i, accuracy*100)
    
    # Precision: truepos/pos
    # Recall: truepos/(truepos+falseneg)

    recall = 0
    precision = 0
    used_labels = 0

    for label in data_labels:
        truepos = confusion_matrix[label][label]
        falsepos = sum([confusion_matrix[z][label] for z in data_labels if z != label])
        falseneg = sum([confusion_matrix[label][z] for z in data_labels if z != label])

        if truepos != 0: 
            used_labels += 1
            precision += truepos/float(truepos+falsepos)
            recall    += truepos/float(truepos+falseneg)

    precision /= used_labels
    recall /= used_labels
    fscore = 1.0/(.5/precision + .5/recall)
    fscores += [fscore]

    print "  Precision %d is: %2.2f%%" % (i, precision*100)
    print "  Recall %d is: %2.2f%%" % (i, recall*100)
    print "  F-score %d is: %2.2f%%" % (i, fscore*100)


print "------"
print "Average accuracy for %d-fold cross validation is: %2.2f%%" % (n, sum(accuracies) / float(len(accuracies))*100)
print "Average F-score for %d-fold cross validation is: %2.2f%%" % (n, sum(fscores) / float(len(fscores))*100)

# results = []
# for i in range(0,len(base_corpus)):
#     corpus = list(base_corpus)
#     labels = list(base_labels)
#     test_vec = corpus[i]
#     label = labels[i]
#     #print test_vec
#     corpus.remove(test_vec)
#     labels.remove(label)

#     tfidf = models.TfidfModel(corpus)
#     index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))
 
#     sims = index[tfidf[test_vec]]
#     max_index = 0
#     max_similarity = 0
#     for (index, similarity) in list(enumerate(sims)):
#         if float(similarity) > max_similarity:
#             max_similarity = float(similarity)
#             max_index = int(index)


#     # print "index is: " + str(max_index)
#     # print "similarity is: " + str(max_similarity)
#     # print "predicted label is: " + str(labels[int(max_index)])
#     # print "actual label is: " + str(label)
#     # print list(enumerate(sims))
#     if str(labels[int(max_index)]) == str(label):
#         print "correct, was %s" % label
#         results.append(1)
#     else:
#         print "incorrect, was %s, guessed %s" % (label, labels[int(max_index)])
#         results.append(0)

# accuracy = float(sum(results)) / len(results)
# print "accuracy is: " + str(accuracy)
