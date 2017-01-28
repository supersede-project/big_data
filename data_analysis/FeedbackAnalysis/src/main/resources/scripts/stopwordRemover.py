#! /usr/bin/env python
# module to remove stopwords
import os



# Parameters:

def remove_stopwords(text):
    print os.path.dirname(__file__)
    
    stopwords = [line.strip() for line in open( os.path.dirname(__file__)+ '/stopwords_all.txt')]
    content = [w for w in text if w.lower() not in stopwords]
    content_refined = [w for w in content if w.lower() not in ('\'', '-', '_', '=', ':-')]
    return content_refined


