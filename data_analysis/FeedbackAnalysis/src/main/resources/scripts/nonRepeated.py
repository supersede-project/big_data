def nonRepeated(bigram):
	"""This function receives a dictionary whose keys are tuples (bigrams) and the values are integers (frequencies)
	and returns a new dictionary without the same key value pairs but without repeated. So, for example, if the following
	values are in 'bigram':
	('a','b'): x
	('b','a'): y
	this funcion replaces the values in the following way:
	('a','b'): x+y
	('b','a'): 0
	and then returns the new dictionary."""
	keys = bigram.keys()
	start  = 0
	newvalues = bigram.copy()
	while start<len(keys):
		for i in range(start+1,len(keys)):
			if keys[start][0] == keys[i][1] and keys[start][1] == keys[i][0]:
			#	Uncomment this to debug!
			#	print ("repeated!")
			#	print ("{0} {1} == {2} {3}".format(keys[start][0],keys[start][1],keys[i][0],keys[i][1]))
			#	print ("values before(bigram): {0}  ,  {1}".format(bigram[keys[start]],bigram[keys[i]]))
			#	print ("values before(newvalues): {0}  ,  {1}".format(newvalues[keys[start]],newvalues[keys[i]]))	
				newvalues[keys[start]] = newvalues[keys[start]]+newvalues[keys[i]]
				newvalues[keys[i]] = 0
			#	print ("values after(newvalues): {0}  ,  {1}".format(newvalues[keys[start]],newvalues[keys[i]]))
				break
		start = start + 1
	return newvalues

def eliminateFromList(bigram,textList):
	"""Function to discard words that are not in the textList"""
	keys = bigram.keys()
	newBigram = bigram.copy()
	for value in keys:
		if not isInDict(value,textList):
			newBigram[value] = 0
	return newBigram

def isInDict(value,textList):
	""" check whether the strings in value are in textList"""
	for item in textList:
		if item == value[0] or item == value[1]:
			return True
	return False



#def mynonRepeated(bigram)

