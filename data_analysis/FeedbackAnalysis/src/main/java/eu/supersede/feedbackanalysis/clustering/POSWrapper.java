package eu.supersede.feedbackanalysis.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator.AnalysisType;

/**
 * 
 * @author fitsum
 *
 */
public class POSWrapper {

	private MaxentTagger maxentTagger;

	private String modelFile;

	private StanfordCoreNLP pipeline;

	private AnalysisType analysisType;

	public POSWrapper(AnalysisType at) {
		analysisType = at;

		modelFile = "/data/workspace/wordnet/stanford-postagger-2017-06-09/models/english-left3words-distsim.tagger";
		// maxentTagger = new MaxentTagger(modelFile);

		Properties props;
		props = new Properties();
		String annotators = "tokenize, ssplit, pos, lemma";
		props.put("annotators", annotators);

		/*
		 * This is a pipeline that takes in a string and returns various analyzed
		 * linguistic forms. The String is tokenized via a tokenizer (such as
		 * PTBTokenizerAnnotator), and then other sequence model style annotation can be
		 * used to add things like lemmas, POS tags, and named entities. These are
		 * returned as a list of CoreLabels. Other analysis components build and store
		 * parse trees, dependency graphs, etc.
		 * 
		 * This class is designed to apply multiple Annotators to an Annotation. The
		 * idea is that you first build up the pipeline by adding Annotators, and then
		 * you take the objects you wish to annotate and pass them in and get in return
		 * a fully annotated object.
		 * 
		 * StanfordCoreNLP loads a lot of models, so you probably only want to do this
		 * once per execution
		 */
		pipeline = new StanfordCoreNLP(props);
	}

	public Set<String> lemmatize(String documentText) {
		Set<String> lemmas = new HashSet<String>();
		// Create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);
		// run all Annotators on this text
		this.pipeline.annotate(document);
		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// get POS tag and collect the lemma only for NOUNS, ADJ, VERB
				String pos = token.get(PartOfSpeechAnnotation.class);

				if (includeTag(pos)) {

					// Retrieve and add the lemma for each word into the
					// list of lemmas
					lemmas.add(token.get(LemmaAnnotation.class));
				}
			}
		}
		return lemmas;
	}

	public boolean includeTag(String tag) {
		boolean include = false;
		switch (analysisType) {
		case NOUNS_ONLY:
			switch (tag) {
			case "NN":
			case "NNS":
			case "NNP":
			case "NNPS":
				include = true;
				break;
			}
			break;
		case VERBS_ONLY:
			switch (tag) {
			case "VB":
			case "VBZ":
			case "VBG":
			case "VBN":
			case "VBP":
				include = true;
				break;
			}
			break;
		case NOUNS_AND_VERBS:
			switch (tag) {
			case "NN":
			case "NNS":
			case "NNP":
			case "NNPS":
			case "VB":
			case "VBZ":
			case "VBG":
			case "VBN":
			case "VBP":
				include = true;
				break;
			}
			break;
		default: // ALL
			switch (tag) {
			case "NN":
			case "NNS":
			case "NNP":
			case "NNPS":
			case "JJ":
			case "VB":
			case "VBZ":
			case "VBG":
			case "VBN":
			case "VBP":
				include = true;
				break;
			}
		}
		return include;
	}

	// public Set<String> getTerms(String sentence) {
	// Set<String> terms = new HashSet<String>();
	// String tagged = maxentTagger.tagString(sentence);
	// for (String part : tagged.split(" ")) {
	// String[] parts = part.split("_");
	// String word = parts[0];
	// String tag = parts[1];
	//
	// switch (tag) {
	// case "NN":
	// case "NNS":
	// case "JJ":
	// terms.add(word);
	// break;
	// case "MD":
	// break;
	// case "VB":
	// case "VBZ":
	// case "VBP":
	// case "DT":
	// System.out.println("Verb: ");
	// terms.add(word);
	// break;
	// case "PRP":
	// break;
	// default:
	// // System.err.println("TAG:" + tag + "; WORD: " + word);
	// }
	// // System.out.print(word);
	// // System.out.println();
	// }
	// // System.out.println(tagged);
	// return terms;
	// }

	public static void main(String[] args) {
		POSWrapper posWrapper = new POSWrapper(AnalysisType.ALL);
		String feedback = "meter readings appearing incorrect. can you please have a look? thank you.";
		posWrapper.lemmatize(feedback);
	}
}
