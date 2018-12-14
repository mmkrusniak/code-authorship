import json
import os
import collections
import sys

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

countsCollection = []
base_labels = []

data_labels = ("mk", "kr", "camera", "turtle", "jigsaw", "jetty")

min_length = sys.maxint
for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    if min_length > len(os.listdir(data_dir)):
        min_length = len(os.listdir(data_dir))

for label in data_labels:
    data_dir = (".."+os.sep)*3+"out"+os.sep+label
    for i in range(0, min_length):
    #for filename in os.listdir(data_dir):
        filename = os.listdir(data_dir)[i]
        #print filename
        with open(data_dir + os.sep + filename) as parseFile:
            parse = json.load(parseFile)
            counts = {}
            count(parse, counts)
            counts["depth"] = get_depth(parse, 1)
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

print(base_labels)

k = 10
n = len(base_corpus)/k
#print n
accuracies = []
for i in xrange(0, len(base_corpus), n):
    test_vecs = base_corpus[i:i + n]
    if len(test_vecs) < n:
        print "skipping iteration " + str(float(i)/n) +".  Only " + str(len(test_vecs)) + " vectors remaining."
        continue
    #print len(test_vecs)
    test_lables = base_labels[i:i + n]
    #print len(test_lables)
    corpus = list(base_corpus)
    labels = list(base_labels)
    for vec in test_vecs:
        corpus.remove(vec)
    for label in test_lables:
        labels.remove(label)

    tfidf = models.TfidfModel(corpus)
    index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))

    results = []
    for j in range(0, len(test_vecs)):
        #print str(float(j)/len(test_vecs) * 100) + "% through fold " +  str(float(i)/n)
        test_vec = test_vecs[j]
        label = test_lables[j]
        sims = index[tfidf[test_vec]]
        max_index = 0
        max_similarity = 0
        for (sim_index, similarity) in list(enumerate(sims)):
            if float(similarity) > max_similarity:
                max_similarity = float(similarity)
                max_index = int(sim_index)

        if str(labels[int(max_index)]) == str(label):
            #print "correct"
            results.append(1)
        else:
            #print "incorrect"
            results.append(0)

    accuracy = float(sum(results)) / len(results)
    accuracies.append(accuracy)
    print "accuracy " + str(float(i)/n) + " is: " + str(accuracy)

avg_acc = sum(accuracies) / float(len(accuracies))
print "average accuracy for " + str(k) + "-fold cross validation is: " + str(avg_acc)

#results = []
#for i in range(0,len(base_corpus)):
#    corpus = list(base_corpus)
#    labels = list(base_labels)
#    test_vec = corpus[i]
#    label = labels[i]
#    #print test_vec
#    corpus.remove(test_vec)
#    labels.remove(label)

#    tfidf = models.TfidfModel(corpus)
#    index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))

#    sims = index[tfidf[test_vec]]
#    max_index = 0
#    max_similarity = 0
#    for (index, similarity) in list(enumerate(sims)):
#        if float(similarity) > max_similarity:
#            max_similarity = float(similarity)
#            max_index = int(index)

    #print "index is: " + str(max_index)
    #print "similarity is: " + str(max_similarity)
    #print "predicted label is: " + str(labels[int(max_index)])
    #print "actual label is: " + str(label)
    #print list(enumerate(sims))
#    if str(labels[int(max_index)]) == str(label):
#        print "correct"
#        results.append(1)
#    else:
#        print "incorrect"
#        results.append(0)

#accuracy = float(sum(results)) / len(results)
#print "accuracy is: " + str(accuracy)