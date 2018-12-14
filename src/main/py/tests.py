import json
import os
import collections

from gensim import corpora, models, similarities

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

def trim(vec):
    to_remove = []
    for tup in vec:
        if tup[1] == 0:
            to_remove.append(tup)

    for tup in to_remove:
        vec.remove(tup)

countsCollection = []
base_labels = []

data_labels = ("camera","kr","mk")

for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    for filename in os.listdir(data_dir):
        #print filename
        with open(data_dir + os.sep + filename) as parseFile:
            parse = json.load(parseFile)
            counts = {}
            count(parse, counts)
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

results = []
for i in range(0,len(base_corpus)):
    corpus = list(base_corpus)
    labels = list(base_labels)
    test_vec = corpus[i]
    label = labels[i]
    #print test_vec
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

    #print "index is: " + str(max_index)
    #print "similarity is: " + str(max_similarity)
    print "predicted label is: " + str(labels[int(max_index)])
    print "actual label is: " + str(label)
    #print list(enumerate(sims))
    if str(labels[int(max_index)]) == str(label):
        print "correct"
        results.append(1)
    else:
        print "incorrect"
        results.append(0)

accuracy = float(sum(results)) / len(results)
print "accuracy is: " + str(accuracy)