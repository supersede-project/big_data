#! /usr/bin/env python
# Author: Emitza Guzman
# Parameters: ngram to search and project where it should seach it in
# Searches for nGrams in a given directory and sends the document and snippet based sentiments

import os
import string
import csv
import MySQLdb
import nltk
import re
from collections import Counter
from collections import OrderedDict


def insertIndividualNGram(ngram, review_id, pos_score, neg_score, project_id, db, cur):
    
    #print 'data to insert %s, %s, %s, %s %s %s %s', ngram, pos_score, neg_score, length, total_pos, total_neg, review_id
    cur.execute('INSERT into automatically_extracted_features (review_id, name, pos_score, neg_score, project_id) values (%s, %s, %s, %s, %s)', (review_id, ngram, pos_score, neg_score, project_id))

    db.commit()

    return [pos_score, neg_score]


# returns the files and average sentiment associated to a specific nGram given as input
def findBigram (ngram, project, project_id):
    senti_dir = "/Users/Emitza/Documents/workspace/UserFeedback/SentimentIndexing/Data/" + project + "/ProcessedSentimentsLemmatized"
   
    db = MySQLdb.connect(unix_socket = "/Applications/MAMP/tmp/mysql/mysql.sock",
                        host="localhost", # your host, usually localhost
                        user="root", # your username
                        passwd="root", # your password
                        db="supersede_feedback") # name of the data base

    cur = db.cursor()

    # num of times it appeared in (it can be more than  one time in a file)
    ngram_count = 0
    
    for root, dirs, filenames in os.walk(senti_dir):
        for f in filenames:
            file = open(os.path.join(root, f),'r')
            file_content = file.read()
            #IF I were to look for exactly together I need this here instad of for loop. also modify below line 70 :) x = file_content.find(ngram)
            found = True
            for word in ngram.split():
                x = file_content.find(word)
                if(x == -1):
                    found = False
                    continue
        
            # Look for line that contains the value
            if(found == True and file != ".DS_Store"):
                filenumber = f[f.find("_")+1:f.find(".")]
                # look for snippet total
                senti_file = csv.reader(open(senti_dir + '/' + project + '_' + filenumber + '.txt'), delimiter = '\t')
                words_of_ngram = ngram.split(' ')
                        
                # TODO: could have a function for bigrams and another one for trigrams
                w1  = words_of_ngram[0]
                w2 = words_of_ngram[1]
                
                row_count=0
                # iterate over all rows in cvs file!!!!
                for row in senti_file:
                    found_in_csv = re.search(w1+"\W+(?:\w+\W+){0,3}"+w2, row[4])
                                
                    # check if inverse order of words works!
                    if found_in_csv is None:
                        found_in_csv = re.search(w2+"\W+(?:\w+\W+){0,3}"+w1, row[4])
                    
                    if found_in_csv is not None:
                        # Add the snippet positive and negative score
                        pos_score = float(row[0])
                        neg_score = float(row[1])
                        ngram_count += 1
                        row_count += 1
                        
                        review_id = int(filenumber.split('_')[0])/10
                        print review_id
                        # TODO: TODO: URGENT review id vs filenumber situation needs to be cleared!!!!!
                        current = insertIndividualNGram(ngram, review_id, pos_score, neg_score, project_id, db, cur)

            file.close()

    if ngram_count > 0:
        print 'ngram %s count %i', ngram, ngram_count
        
    # closing DB connection
    cur.close()
    db.close()

    return ngram_count



