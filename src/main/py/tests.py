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

countsCollection = []

data_dir = (".."+os.sep)*3+"out"+os.sep+"kr"
for filename in os.listdir(data_dir):
    print filename
    with open(data_dir + os.sep + filename) as parseFile:
        parse = json.load(parseFile)
        counts = {}
        count(parse, counts)
        countsCollection.append(counts)

types = []
for counts in countsCollection:
    for type in counts:
        types.append(type)

types = set(sorted(types))

corpus = []
for counts in countsCollection:
    counts_vec = []
    i = 0
    for type in types:
        count = 0
        if type in counts:
            count = counts[type]

        counts_vec.append((i,count))
        i+=1

    corpus.append(counts_vec)

test_vec = corpus[1]
corpus.remove(test_vec)

tfidf = models.TfidfModel(corpus)
index = similarities.SparseMatrixSimilarity(tfidf[corpus], num_features=len(types))

sims = index[tfidf[test_vec]]
print list(enumerate(sims))
