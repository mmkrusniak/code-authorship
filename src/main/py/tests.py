import json
import os
import collections
import re

from gensim import corpora, models, similarities


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
    pass
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

    if len(names) == 0: print file.name

    for name in names:
        counts["var_len"] += len(name)
    counts["var_len"] /= len(names)

    counts["line_len"] /= counts["num_lines"]



        # Best: 0.875471698113


def trim(vec):
    to_remove = []
    for tup in vec:
        if tup[1] == 0:
            to_remove.append(tup)

    for tup in to_remove:
        vec.remove(tup)

countsCollection = []
base_labels = []

data_labels = ("camera","bined","turtle", "bot", "mk", "kr", "play")
num_files = {}

for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    java_dir = (".."+os.sep)*3+"data"+os.sep+label
    for filename in os.listdir(data_dir):
        if label not in num_files: num_files[label] = 0
        num_files[label] += 1
        #print filename
        with open(data_dir + os.sep + filename) as parseFile:
            with open(java_dir + os.sep + (filename.replace(".json", ".java"))) as javaFile:
                parse = json.load(parseFile)
                counts = {}
                # count(parse, counts)
                count_bitrees(parse, counts)
                count_style(javaFile, counts)
                # print counts
                count_tritrees(parse, counts) 
                countsCollection.append(counts)
                base_labels.append(label)

types = []
for counts in countsCollection:
    for type in counts:
        types.append(type)

types = set(sorted(types))

base_corpus = []
for counts in countsCollection:
    counts_vec = []
    i = 0
    for type in types:
        count = 0
        if type in counts:
            count = counts[type]

        counts_vec.append((i,count))
        i+=1

    base_corpus.append(counts_vec)

for vec in base_corpus:
    trim(vec)

mistakes = {}

results = []
for i in range(0,len(base_corpus)):
    corpus = list(base_corpus)
    labels = list(base_labels)
    test_vec = corpus[i]
    label = labels[i]
    # print test_vec
    corpus.remove(test_vec)
    labels.remove(label)

    tfidf = models.TfidfModel(corpus)
    index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))

    sims = index[tfidf[test_vec]]
    max_index = 0
    max_similarity = 0
    for (index, similarity) in list(enumerate(sims)):
        if float(similarity) > max_similarity:
            max_similarity = float(similarity)
            max_index = int(index)

    # print "index is: " + str(max_index)
    # print "similarity is: " + str(max_similarity)
    # print "predicted label is: " + str(labels[int(max_index)])
    # print "actual label is: " + str(label)
    # print list(enumerate(sims))
    if str(labels[int(max_index)]) == str(label):
        print "correct"
        results.append(1)
    else:
        print "incorrect (was %s, predicted %s)" % (label, str(labels[int(max_index)]))
        results.append(0)
        if label not in mistakes: mistakes[label] = 0
        mistakes[label] += 1

accuracy = float(sum(results)) / len(results)
print "accuracy is: " + str(accuracy)
for key in mistakes:
    mistakes[key] = "%2.2f" % (100 - ((mistakes[key] * 100.0) / num_files[key])) + "%"
print mistakes