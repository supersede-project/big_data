#! /usr/bin/env python
# Author: Emitza Guzman
# Parameters: name of file to extract features from, name of output file
# extracts nGrams from prepared txt file applied stemming to it

import string
import sys
import nltk
from nltk.collocations import *
from nltk.stem.wordnet import WordNetLemmatizer
from nltk.tokenize import RegexpTokenizer
from nltk.corpus import wordnet
from collections import Counter
from collections import OrderedDict

import searchNGram
import stopwordRemover
import nonRepeated
import csv


input_file_name = str(sys.argv[1])
output_file_name = str(sys.argv[2])
# filtering = int(sys.argv[3]) # 0 for no senti filtering, 1 for senti filtering



#########################################################################################################################
# saving nGrams to file
def saveNGrams(nGrams):
    file = open(output_file_name, 'w')
    csv_writer = csv.writer(file)
    
    features = [[]]

    for key in nGrams:
        freq = nGrams[key]
        nGram = "".join(str(x)+" " for x in key)
        nGram = nGram[:len(nGram)-1]
        words_of_ngram = nGram.split(' ')
        # look for sentiment (last two of returned list), files (second of returned list) and together frequency of nGram
        #print key, freq
        # > 3 validation makes sure that ngrams are not repeated
        if freq > 3 and len(words_of_ngram[0])>=2 and len(words_of_ngram[1])>=2:
            features.append([nGram, freq])
    csv_writer.writerows(features)
    file.close()


#########################################################################################################################
# Auxiliary function which converts POS tags to Wordnet tags

def convertToWordnetTag(tag):
    if tag.startswith('NN'):
        return wordnet.NOUN
    elif tag.startswith('VB'):
        return wordnet.VERB
    elif tag.startswith('JJ'):
        return wordnet.ADJ
    else:
        return ''

#########################################################################################################################
# This is the ngram extraction part of the program

# read and tokenize my file
f = open(input_file_name)
data_str = f.read()
f.close()

data_str = data_str.lower()

tokenizer = RegexpTokenizer(r'\w+')
tokens = tokenizer.tokenize(data_str)

tagged = nltk.pos_tag(tokens)

# converting to wordnet tags and filtering out those that are not noun, adjective, verb
filtered = []
for tuple_tag in tagged:
    wn_tag = convertToWordnetTag(tuple_tag[1])
    if wn_tag != '':
        filtered.append((tuple_tag[0],wn_tag))

# lemmatizing the words
lemmatized = []
l = WordNetLemmatizer()
for tuple_wntag in filtered:
    lemmatized.append(l.lemmatize(tuple_wntag[0], tuple_wntag[1]))

processed = stopwordRemover.remove_stopwords(lemmatized)

bigram = BigramCollocationFinder.from_words(processed)
trigram = TrigramCollocationFinder.from_words(processed)

bigram_measures = nltk.collocations.BigramAssocMeasures()
trigram_measures = nltk.collocations.TrigramAssocMeasures()


bigram.ngram_fd.viewitems()

# TODO: I am extracting trigrams but currently not storing them, depending on the user feedback we get from use cases, me might want to consider them

# getting unigrams
results_unigram = Counter(processed)

#extra filter for numbers
bigram.apply_word_filter(lambda w: w.isdigit()) #numbers should not be part of bigram

# TODO: these type of filters are not working on frequency collocations, only on pmi
bigram.apply_freq_filter(3)
trigram.apply_freq_filter(3)

bigram.apply_word_filter(lambda w: w in ('\'', '-', '_', '=', ':-'))
trigram.apply_word_filter(lambda w: w in ('\'', '-', '_', '=', ':-'))
results_bigram = bigram.ngram_fd
results_trigram = trigram.ngram_fd
#print type(results_bigram)


bgm = nltk.collocations.BigramAssocMeasures()
finder = nltk.collocations.BigramCollocationFinder.from_words(processed)
finder.apply_freq_filter(3)
finder.apply_word_filter(lambda w: w in ('\'', '-', '_', '=', ':-'))
scored = finder.score_ngrams( bgm.likelihood_ratio  )

sofisticated_bigrams ={}

# scored is stored in a tuple
for i, s in enumerate(scored):
    sofisticated_bigrams[s[0]]=s[1]


#TODO: there are different score functions. Which one to take will probably be decided after the evaluation on the Supersede data.
results_bmi_bigram = bigram.nbest(bigram_measures.pmi, 300)

#to see bmi collocation with their frequencies and later graph the values
big = []
freq = []
fdist = nltk.FreqDist(results_bmi_bigram)
for k,v in fdist.items():
    big.append(k)
    freq.append(v)

# print("Finished finding n-grams!")


#########################################################################################################################
# Eliminating repeated elements: what I did was, to create a copy of the original results_bigram, find the repeated ones,
# sum up the frequency and assign 0 to the repeated one. This way you dont have to modify your code that much.
# 
# So you can take a look to the files. Now you can use the funtion seriously. Uncomment this to get the whole bigrams dictionary
# without the repeated values
#print 'Number of repeated bigrams %d', len(results_bmi_bigram.keys())
nonrepeated_bigrams = nonRepeated.nonRepeated(sofisticated_bigrams)
# print 'Finished non repeated bigrams! %d', len(nonrepeated_bigrams.keys())"""

#########################################################################################################################
#inserting non-duplicated ngrams to the DB
saveNGrams(nonrepeated_bigrams)

