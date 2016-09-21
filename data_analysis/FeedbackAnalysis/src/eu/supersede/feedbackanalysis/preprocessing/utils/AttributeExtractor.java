package eu.supersede.feedbackanalysis.preprocessing.utils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.opencsv.CSVReader;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import eu.supersede.feedbackanalysis.ds.UserFeedback;


public class AttributeExtractor {
	//PrintWriter out;
	int num_verbs=0;
	int num_pos_verbs=0;
	double pos_verbs_score=0;
	int num_neg_verbs=0;
	double neg_verbs_score=0;
	int contain_modal=2; // yes =1, no =2
	int modal=-1; //MD - modal: 0 ‘can’, 1 ‘could’, 2 ‘dare’, 3 ‘may’, 4 ‘might’, 5 ‘must’,6  ‘ought’,7 ‘shall’,8 ‘should’,9 ‘will’,10 ‘would’.
	int num_nouns=0;
	int num_pos_nouns=0;
	double pos_nouns_score=0;
	int num_neg_nouns=0;
	double neg_nouns_score=0;
	int num_pos_adj=0;
	double pos_score=0.0;
	int num_neg_adj=0;
	double neg_score=0.0;
	int num_adj=0;
	int sent_length=0;
	int num_code_lines=0;
	int num_question_marks=0;
	int num_exclamation_marks=0;
	int num_slash=0;
	int num_underscore=0;
	int num_SAD=0;
	int num_SMILEY=0;
	int num_http=0;// it can be http, https or url_link
	int num_ftp=0;
	int num_hash=0; //number of times that # appears
	int num_currency=0; //number of times that $ appears
	int num_percentage=0; //number of times that $ appears
	int num_brackets_type1=0; //{}
	int num_brackets_type2=0; //[]
	int num_assertive_verbs=0;
	int num_requestive_verbs=0;
	int num_responsive_verbs=0;
	int num_informative_verbs=0;
	int num_assertive_expressions=0;
	int num_requestive_expressions=0;
	int num_responsive_expressions=0;
	double overall_sentiment=0; //0:neutral, -1:negative, 2:positive

	ArrayList<String> assertive_verbs;
	ArrayList<String> responsive_verbs;
	ArrayList<String> requestive_verbs;
	ArrayList<String> informative_verbs;
	ArrayList<String> assertive_expressions;
	ArrayList<String> requestive_expressions;
	ArrayList<String> responsive_expressions;

	StanfordCoreNLP pipeline ;
	static String pathToSWN = "resources/SentiWordNet_3.0.0_20130122.txt";
	static SentiWord sentiwordnet;


	public AttributeExtractor() throws IOException{

		//out = new PrintWriter(System.out);
		// Add in sentiment
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");

		pipeline = new StanfordCoreNLP(props);
		informative_verbs= new ArrayList<String>(){{
			add("announce");
			add("apprise");
			add("disclose");
			add("inform");
			add("notify");
			add("point out");
			add("report");
			add("reveal");
		}};

		assertive_expressions= new ArrayList<String>(){{
			add("ou are right");
			add("ou were right");
			add("ou are correct");
			add("ou were correct");
			add("not sure");
			add("not sure if I");
			add("not sure if i");
			add("have no idea");
			add("not an expert");
			add("not expert");
			add("n't underst");
			add("n't know");
			add("not able");
			add("I have");
			add("I had");
			add("I get");
			add("I got");
			add("am having");
			add("am getting");
		}};
		
		assertive_verbs= new ArrayList<String>(){{
			add("confuse");
			add("affirm");
			add("allege");
			add("assert");
			add("aver");
			add("avow");
			add("claim");
			add("declare");
			add("indicate");
			add("maintain");
			add("propound");
			add("say");
			add("state");
			add("submit");
			add("appraise");
			add("assess");
			add("bear witness");
			add("certify");
			add("conclude");
			add("confirm");
			add("corroborate");
			add("diagnose");
			add("judge");
			add("substantiate");
			add("testify");
			add("validate");
			add("verify");
			add("vouch for");
			add("receive");
			add("acknowledge");
			add("admit");
			add("allow");
			add("assent");
			add("concede");
			add("concur");
			add("confess");
			add("grant");
			add("own");
		}};
		responsive_verbs= new ArrayList<String>(){{
			add("suggest");
			add("recommend");
			add("advise");
			add("propose");
			add("assume");
			add("postulate");
			add("stipulate");
			add("suppose");
			add("theorise");
			add("think");
			add("guess");
			add("believe");
			add("suspect");
			add("answer");
			add("reply");
			add("respond");
			add("retort");
		}};
		
		responsive_expressions= new ArrayList<String>(){{
			add("ou can check");
			add("ou could check");
			add("ou could look at");
			add("ou can look at");
			add("ou could try");
			add("ou can try");
			add("ou could use");
			add("ou can use");
			add("ou could consider");
			add("ou can consider");
			add("ou could revise");
			add("ou can revise");
			add("suggestion");
			add("maybe");
			add("HTH");
			add("lad to be useful");
			add("can fix");
			add("can solve");
			add("can work");
			add("could fix");
			add("could solve");
			add("could work");
			add("might fix");
			add("might solve");
			add("might work");
			add("should fix");
			add("should solve");
			add("should work");
			add("must fix");
			add("must solve");
			add("must work");
			add("will fix");
			add("will solve");
			add("will work");
			add("indeed");
			add("Indeed");
			add("Let me know");
			add("Let us know");
			add("let me know");
			add("let us know");
			add("ee this");
			add("ee below");
		}};

		requestive_verbs= new ArrayList<String>(){{
			add("ask");
			add("beg");
			add("beseech");
			add("implore");
			add("insist");
			add("invite");
			add("petition");
			add("plead");
			add("pray");
			add("request");
			add("solicit");
			add("summon");
			add("supplicate");
			add("tell");
			add("urge");
			add("inquire");
			add("interrogate");
			add("query");
			add("question");
			add("quiz");
			add("bid");
			add("charge");
			add("command");
			add("demand");
			add("dictate");
			add("direct");
			add("enjoin");
			add("instruct");
			add("order");
			add("prescribe");
			add("require");
			add("need");
		}};

		requestive_expressions= new ArrayList<String>(){{
			add("WDYT");
			add("eally?");
			add("ight?");
			add("elp me");
			add("would like to");
			add("would be nice");
			add("feature");
			add("dd an option");
			add("I want");
			add("i want");
			add("I wish");
			add("i wish");
			add("would prefer");
			add("please");
		}};

		sentiwordnet = new SentiWord(pathToSWN);
	}

	public String execute(String text){


		// Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
		Annotation annotation;

		annotation = new Annotation(text);
		countAssertiveExpressions(text);
		//System.out.println(num_assertive_expressions);
		countRequestiveExpressions(text);
		//System.out.println(num_requestive_expressions);
		countResponsiveExpressions(text);
		//System.out.println(num_responsive_expressions);

		// run all the selected Annotators on this text
		pipeline.annotate(annotation);

		// An Annotation is a Map with Class keys for the linguistic analysis types.
		// You can get and use the various analyses individually.
		// For instance, this gets the parse tree of the first sentence in the text.
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && ! sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			//out.println("The first sentence tokens are:");
			
			for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				System.out.println(token.toShorterString());
				String [] strToken=token.toShorterString().split(" ");
				//String word=strToken[2].split("=")[1];
				String word=strToken[2].substring(strToken[2].indexOf("=")+1,strToken[2].length());
				//System.out.println(word);	
				String partOfSpeech=token.toShorterString().substring(token.toShorterString().indexOf("PartOfSpeech")+13,token.toShorterString().indexOf("Lemma"));
				//System.out.println(partOfSpeech);
				
				
				//System.out.println(partOfSpeech);
				//System.out.println(word);
				if(partOfSpeech.startsWith("MD"))
				{
					contain_modal=1;
					switch(word.trim())
					{
					case "can": modal=0; break;
					case "could": modal=1; break;
					case "dare": modal=2; break;
					case "may": modal=3; break;
					case "might": modal=4; break;
					case "must": modal=5; break;
					case "ought": modal=6; break;
					case "shall": modal=7; break;
					case "should": modal=8; break;
					case "will": modal=9; break;
					case "would": modal=10; break;
					}
					
				}
				if(partOfSpeech.startsWith("VB"))
				{
					System.out.println(word);	
					System.out.println(partOfSpeech);
					String lemma=token.toShorterString().substring(token.toShorterString().indexOf("Lemma")+6,token.toShorterString().indexOf("BeginIndex"));
					//System.out.println("Lemma: " + lemma);
					if(assertive_verbs.contains(lemma.trim()))
						num_assertive_verbs++;
					if(responsive_verbs.contains(lemma.trim()))
						num_responsive_verbs++;
					if(requestive_verbs.contains(lemma.trim()))
						num_requestive_verbs++;
					if(informative_verbs.contains(lemma.trim()))
						num_informative_verbs++;
					double sentScore=sentiwordnet.extract(lemma, "v");
					if(sentScore > 0)
					{
						pos_verbs_score+=sentScore;
						num_pos_verbs++;
						//System.out.println("Positive:" + sentScore);
					}
					else if(sentScore < 0)
					{
						neg_verbs_score+=sentScore;
						num_neg_verbs++;
						//System.out.println("Negative: " + sentScore);
					}
					else
					{
						num_verbs++;
						//System.out.println("Score:" + sentScore);
					}
					
				}
				if(partOfSpeech.startsWith("NN"))
				{
					double sentScore=sentiwordnet.extract(word, "n");
					if(sentScore > 0)
					{
						pos_nouns_score+=sentScore;
						num_pos_nouns++;
						//System.out.println("Positive:" + sentScore);
					}
					else if(sentScore < 0)
					{
						neg_nouns_score+=sentScore;
						num_neg_nouns++;
						//System.out.println("Negative: " + sentScore);
					}
					else
					{
						num_nouns++;
						//System.out.println("Score:" + sentScore);
					}
					
				}
				if(partOfSpeech.startsWith("JJ"))
				{
					//System.out.println(partOfSpeech);
					//System.out.println(word);
					double sentScore=sentiwordnet.extract(word, "a");
					if(sentScore > 0)
					{
						pos_score+=sentScore;
						num_pos_adj++;
						//System.out.println("Positive:" + sentScore);
					}
					else if(sentScore < 0)
					{
						neg_score+=sentScore;
						num_neg_adj++;
						//System.out.println("Negative: " + sentScore);
					}
					else
					{
						num_adj++;
						//System.out.println("Score:" + sentScore);
					}
				}
				if(word.compareTo("CODELINE")==0 || word.compareTo("STARTCODE")==0)
					num_code_lines++;
				if(word.compareTo("?")==0)
					num_question_marks++;
				if(word.compareTo("!")==0)
					num_exclamation_marks++;
				if(word.compareTo("/")==0)
					num_slash++;
				if(word.compareTo("_")==0)
					num_underscore++;
				if(word.compareTo("SAD")==0)
					num_SAD++;
				if(word.compareTo("SMILEY")==0)
					num_SMILEY++;
				if(word.compareTo("http")==0||word.compareTo("https")==0 ||word.compareTo("url_link")==0)
					num_http++;
				if(word.compareTo("ftp")==0||word.compareTo("ftps")==0)
					num_ftp++;
				if(word.compareTo("#")==0)
					num_hash++;
				if(word.compareTo("$")==0)
					num_currency++;
				if(word.compareTo("%")==0)
					num_percentage++;
				if(word.compareTo("{")==0||word.compareTo("}")==0)
					num_brackets_type1++;
				if(word.compareTo("[")==0||word.compareTo("]")==0)
					num_brackets_type2++;				
			}
			String sentiment=sentence.get(SentimentCoreAnnotations.SentimentClass.class);
			if(sentiment.compareTo("Negative")==0)
				overall_sentiment=-1;
			else if(sentiment.compareTo("Positive")==0)
				overall_sentiment=1;
			else
				overall_sentiment=0;
			//out.println("Overall sentiment: " + overall_sentiment);
		}

		sent_length=text.length();
		//String vector_headers="Verbs, Nouns, Num pos asj, Pos score, Num neg adj, Neg score, Num adj, Length, Code lines, Question marks, Exclamation marks, /, _ , SAD, SMILEY, Http, Ftp, #, $, %, {, [, Assertive, Requestive, Responsive,num_informative_verbs, Sentiment";
		String vector=num_verbs + "," + num_pos_verbs + "," + pos_verbs_score + "," + num_neg_verbs + "," + neg_verbs_score + "," + contain_modal+ "," + modal + "," + num_nouns + "," + num_pos_nouns + "," + pos_nouns_score + "," +  num_neg_nouns + "," + neg_nouns_score+ "," + num_pos_adj + "," + pos_score + "," + num_neg_adj + "," + neg_score + "," + num_adj + "," + sent_length + "," + num_code_lines + "," + num_question_marks + "," + num_exclamation_marks + "," + num_slash + "," + num_underscore+ "," + num_SAD + "," + num_SMILEY + "," + num_http + "," + num_ftp + "," + num_hash + "," + num_currency + "," + num_percentage + "," + num_brackets_type1 + "," + num_brackets_type2 + "," + num_assertive_verbs + "," + num_requestive_verbs + "," + num_responsive_verbs + "," + num_informative_verbs + "," + num_assertive_expressions+ "," + num_requestive_expressions + ","  + num_responsive_expressions + "," + overall_sentiment;

		//System.out.println(vector);
		//IOUtils.closeIgnoringExceptions(out);
		return vector;
	}

	public String executeRemoving(String text){
		String reconstructSent="";

		// Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
		Annotation annotation;

		annotation = new Annotation(text);
		countAssertiveExpressions(text);
		//System.out.println(num_assertive_expressions);
		countRequestiveExpressions(text);
		//System.out.println(num_requestive_expressions);
		countResponsiveExpressions(text);
		//System.out.println(num_responsive_expressions);
		
		// run all the selected Annotators on this text
		pipeline.annotate(annotation);

		// An Annotation is a Map with Class keys for the linguistic analysis types.
		// You can get and use the various analyses individually.
		// For instance, this gets the parse tree of the first sentence in the text.
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && ! sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			//out.println("The first sentence tokens are:");
			
			for (CoreMap token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				System.out.println(token.toShorterString());
				String [] strToken=token.toShorterString().split(" ");
				//String word=strToken[2].split("=")[1];
				String word=strToken[2].substring(strToken[2].indexOf("=")+1,strToken[2].length());
				//System.out.println(word);	
				String partOfSpeech=token.toShorterString().substring(token.toShorterString().indexOf("PartOfSpeech")+13,token.toShorterString().indexOf("Lemma"));
				//System.out.println(partOfSpeech);
				
				
				//System.out.println(partOfSpeech);
				//System.out.println(word);
				if(partOfSpeech.startsWith("PRP") || partOfSpeech.startsWith("VB") || partOfSpeech.startsWith("MD") || partOfSpeech.startsWith("JJ") || partOfSpeech.startsWith("RB") || partOfSpeech.startsWith("IN") || partOfSpeech.startsWith("PDT") || partOfSpeech.startsWith("PP") || partOfSpeech.startsWith("TO") || partOfSpeech.startsWith("DT") || word.startsWith("option") || word.startsWith("feature") || word.startsWith("issue") || word.startsWith("problem") || word.startsWith("bug") || word.startsWith("trouble") || word.startsWith("error") || word.startsWith("exceptions") || word.startsWith("failure") )
				{
					reconstructSent+= word + " ";
					
				}
				
			}
		}
		//System.out.println(vector);
		//IOUtils.closeIgnoringExceptions(out);
		return reconstructSent;
	}

	public String getARFF (String path){
		String CLASSES = "Assertive,Informative,Requestive,Responsive";
		
		String sentence="";
		String classSAct="";

		Set<String> classes = new HashSet<String>();
		
		String vector_header="@relation OpenOffice_features\n\n";
		vector_header+="@attribute num_verbs numeric\n";
		vector_header+="@attribute num_pos_verbs numeric\n";
		vector_header+="@attribute pos_verbs_score real\n";
		vector_header+="@attribute num_neg_verbs numeric\n";
		vector_header+="@attribute neg_verbs_score real\n";
		vector_header+="@attribute contain_modal numeric\n";
		vector_header+="@attribute modal numeric\n";
		vector_header+="@attribute num_nouns numeric\n";
		vector_header+="@attribute num_pos_nouns numeric\n";
		vector_header+="@attribute pos_nouns_score real\n";
		vector_header+="@attribute num_neg_nouns numeric\n";
		vector_header+="@attribute neg_nouns_score real\n";
		vector_header+="@attribute num_pos_adj numeric\n";
		vector_header+="@attribute pos_score real\n";
		vector_header+="@attribute num_neg_adj numeric\n";
		vector_header+="@attribute neg_score real\n";
		vector_header+="@attribute num_adj numeric\n";
		vector_header+="@attribute sent_length numeric\n";
		vector_header+="@attribute num_code_lines numeric\n";
		vector_header+="@attribute num_question_marks numeric\n";
		vector_header+="@attribute num_exclamation_marks numeric\n";
		vector_header+="@attribute num_slash numeric\n";
		vector_header+="@attribute num_underscore numeric\n";
		vector_header+="@attribute num_SAD numeric\n";
		vector_header+="@attribute num_SMILEY numeric\n";
		vector_header+="@attribute num_http numeric\n";
		vector_header+="@attribute num_ftp numeric\n";
		vector_header+="@attribute num_hash numeric\n";
		vector_header+="@attribute num_currency numeric\n";
		vector_header+="@attribute num_percentage numeric\n";
		vector_header+="@attribute num_brackets_type1 numeric\n";
		vector_header+="@attribute num_brackets_type2 numeric\n";
		vector_header+="@attribute num_assertive_verbs numeric\n";
		vector_header+="@attribute num_requestive_verbs numeric\n";
		vector_header+="@attribute num_responsive_verbs numeric\n";
		vector_header+="@attribute num_informative_verbs numeric\n";
		vector_header+="@attribute num_assertive_expressions numeric\n";
		vector_header+="@attribute num_requestive_expressions numeric\n";
		vector_header+="@attribute num_responsive_expressions numeric\n";
		vector_header+="@attribute overall_sentiment real\n";
		
		
		try {
			CSVReader reader = new CSVReader(new FileReader(path));
			String [] nextLine;

			String vector="";
			while ((nextLine = reader.readNext()) != null) {
//			for (UserFeedback feedback : userFeedback){
				sentence= nextLine[0]; //commented for generating the OpenOffice data
				//sentence= nextLine[0]; // commented for generating the OpenOffice data
				classSAct= nextLine[1]; //commented for generating the OpenOffice data
				classes.add(classSAct);
//				classSAct="?";
				//classSAct= nextLine[5];	//Fitsum's file
				clearValues();
				//classSAct="Informative";
				//sentence="Using the ajp protocol would fix this since it uses exactly the request that Apache received with the right hostname and port.";
				// commented for generating the OpenOffice  if(classSAct.compareTo("Informative")==0 || classSAct.compareTo("Assertive")==0 || classSAct.compareTo("Requestive")==0 || classSAct.compareTo("Responsive")==0)
				vector+=execute(sentence) + "," + classSAct + "\n"; // + " % " + nextLine[1]+ "\n";
				//NOTE :vector+=execute(sentence) + "\n";
				//To generate the ARFF files for clustering
//				String comSent=executeRemoving(sentence);
//				if(comSent.trim().compareTo("")!=0)
//					vector+= "'" + comSent.trim() + "', ? \n";
				//To generate the ARFF files for clustering
			}
			reader.close();
			
			vector_header+="@attribute @@class@@ {" + getClasses(classes) + "}\n";

			//To generate the ARFF files for clustering
//			vector_header+="@attribute text string\n";
//			vector_header+="@attribute @@class@@ {Assertive,Informative,Requestive,Responsive}\n";
			//To generate the ARFF files for clustering
			
			vector_header+="\n\n@data\n\n";

			vector_header+=vector;
			//mfiles.writeFile("/Users/itzy_5/Documents/Trento_SUPERSEDE/", "CrowdIntent2featuresExpressions.arff", vector_header);
//			mfiles.writeFile("./", outputFile,vector_header);
//			mfiles.writeFile("/Users/itzy_5/Google Drive/CollaborationSAT/ReportMarch11_2016/AnnotatedData/", "OpenOfficeFeatures.arff",vector_header);
			//System.out.println(vector);
			System.out.println("Done");
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return vector_header;
	}
	
	//Method to generate the ARFF files -WEKA-
	public String getARFF(List<UserFeedback> userFeedback){
//		FileManager mfiles=new FileManager();
		//String csvFile="/Users/itzy_5/Documents/workspace/PrjSWBDtoSActs/dataset/SWBDdataset.csv";
		//String csvFile="/Users/itzy_5/Documents/workspace/PrjSWBDtoSActs/dataset/OOo/File6b.csv";
//		String csvFile="/Users/itzy_5/Google Drive/CollaborationSAT/ReportMarch11_2016/AnnotatedData/AnnotatedOpenOffice15March2016.csv";
	//	String csvFile = "comments_order_0_confirmed_textonly.csv"; //"SENERCON_translated_300_feedback.csv"; // //"AnnotatedOpenOffice15March2016.csv";
//		String outputFile = csvFile + ".arff";
		
		String CLASSES = "Assertive,Informative,Requestive,Responsive";
		
		//String csvFile = "/Users/itzy_5/Documents/Trento_SUPERSEDE/SentsAnnotationsSingle_UniqueClass.csv";
		//String csvFile = "/Users/itzy_5/Documents/Trento_SUPERSEDE/SentsAnnotationsSingle_UniqueClass_Itzel_Fitsum.csv";
		

		String sentence="";
		String classSAct="";

		Set<String> classes = new HashSet<String>();
		
		String vector_header="@relation OpenOffice_features\n\n";
		vector_header+="@attribute num_verbs numeric\n";
		vector_header+="@attribute num_pos_verbs numeric\n";
		vector_header+="@attribute pos_verbs_score real\n";
		vector_header+="@attribute num_neg_verbs numeric\n";
		vector_header+="@attribute neg_verbs_score real\n";
		vector_header+="@attribute contain_modal numeric\n";
		vector_header+="@attribute modal numeric\n";
		vector_header+="@attribute num_nouns numeric\n";
		vector_header+="@attribute num_pos_nouns numeric\n";
		vector_header+="@attribute pos_nouns_score real\n";
		vector_header+="@attribute num_neg_nouns numeric\n";
		vector_header+="@attribute neg_nouns_score real\n";
		vector_header+="@attribute num_pos_adj numeric\n";
		vector_header+="@attribute pos_score real\n";
		vector_header+="@attribute num_neg_adj numeric\n";
		vector_header+="@attribute neg_score real\n";
		vector_header+="@attribute num_adj numeric\n";
		vector_header+="@attribute sent_length numeric\n";
		vector_header+="@attribute num_code_lines numeric\n";
		vector_header+="@attribute num_question_marks numeric\n";
		vector_header+="@attribute num_exclamation_marks numeric\n";
		vector_header+="@attribute num_slash numeric\n";
		vector_header+="@attribute num_underscore numeric\n";
		vector_header+="@attribute num_SAD numeric\n";
		vector_header+="@attribute num_SMILEY numeric\n";
		vector_header+="@attribute num_http numeric\n";
		vector_header+="@attribute num_ftp numeric\n";
		vector_header+="@attribute num_hash numeric\n";
		vector_header+="@attribute num_currency numeric\n";
		vector_header+="@attribute num_percentage numeric\n";
		vector_header+="@attribute num_brackets_type1 numeric\n";
		vector_header+="@attribute num_brackets_type2 numeric\n";
		vector_header+="@attribute num_assertive_verbs numeric\n";
		vector_header+="@attribute num_requestive_verbs numeric\n";
		vector_header+="@attribute num_responsive_verbs numeric\n";
		vector_header+="@attribute num_informative_verbs numeric\n";
		vector_header+="@attribute num_assertive_expressions numeric\n";
		vector_header+="@attribute num_requestive_expressions numeric\n";
		vector_header+="@attribute num_responsive_expressions numeric\n";
		vector_header+="@attribute overall_sentiment real\n";
		
		
//		try {
//			CSVReader reader = new CSVReader(new FileReader(csvFile));
//			String [] nextLine;

			String vector="";
//			while ((nextLine = reader.readNext()) != null) {
			for (UserFeedback feedback : userFeedback){
				sentence= feedback.getFeedbackText(); //nextLine[0]; //commented for generating the OpenOffice data
				//sentence= nextLine[0]; // commented for generating the OpenOffice data
				classSAct= "?"; //nextLine[1]; //commented for generating the OpenOffice data
				classes.add(classSAct);
//				classSAct="?";
				//classSAct= nextLine[5];	//Fitsum's file
				clearValues();
				//classSAct="Informative";
				//sentence="Using the ajp protocol would fix this since it uses exactly the request that Apache received with the right hostname and port.";
				// commented for generating the OpenOffice  if(classSAct.compareTo("Informative")==0 || classSAct.compareTo("Assertive")==0 || classSAct.compareTo("Requestive")==0 || classSAct.compareTo("Responsive")==0)
				vector+=execute(sentence) + "," + classSAct + "\n"; // + " % " + nextLine[1]+ "\n";
				//NOTE :vector+=execute(sentence) + "\n";
				//To generate the ARFF files for clustering
//				String comSent=executeRemoving(sentence);
//				if(comSent.trim().compareTo("")!=0)
//					vector+= "'" + comSent.trim() + "', ? \n";
				//To generate the ARFF files for clustering
			}
//			reader.close();
			
			vector_header+="@attribute @@class@@ {DEFECT,ENHANCEMENT,FEATURE}\n"; //OTHER,SUPPORT,DEFECT,ENHANCEMENT,FEATURE}\n"; // + getClasses(classes) + "}\n";

			//To generate the ARFF files for clustering
//			vector_header+="@attribute text string\n";
//			vector_header+="@attribute @@class@@ {Assertive,Informative,Requestive,Responsive}\n";
			//To generate the ARFF files for clustering
			
			vector_header+="\n\n@data\n\n";

			vector_header+=vector;
			//mfiles.writeFile("/Users/itzy_5/Documents/Trento_SUPERSEDE/", "CrowdIntent2featuresExpressions.arff", vector_header);
//			mfiles.writeFile("./", outputFile,vector_header);
//			mfiles.writeFile("/Users/itzy_5/Google Drive/CollaborationSAT/ReportMarch11_2016/AnnotatedData/", "OpenOfficeFeatures.arff",vector_header);
			//System.out.println(vector);
			System.out.println("Done");
//		}
//		catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		return vector_header;
	}
	
	
	
	
	

	private String getClasses(Set<String> classes) {
		StringBuffer labels = new StringBuffer();
		for (String c : classes){
			labels.append(c + ",");
		}
		return labels.toString().substring(0, labels.toString().lastIndexOf(","));
	}

	public void clearValues(){
		num_verbs=0;
		num_pos_verbs=0;
		pos_verbs_score=0;
		num_neg_verbs=0;
		neg_verbs_score=0; 
		contain_modal=2;
		modal=-1;
		num_nouns=0;
		num_pos_nouns=0;
		pos_nouns_score=0;
		num_neg_nouns=0;
		neg_nouns_score=0;
		num_pos_adj=0;
		pos_score=0.0;
		num_neg_adj=0;
		neg_score=0.0;
		num_adj=0;
		sent_length=0;
		num_question_marks=0;
		num_exclamation_marks=0;
		num_slash=0;
		num_underscore=0;
		num_SAD=0;
		num_SMILEY=0;
		num_http=0;// it can be http, https or url_link
		num_ftp=0;
		num_hash=0; //number of times that # appears
		num_currency=0; //number of times that $ appears
		num_percentage=0; //number of times that $ appears
		num_brackets_type1=0; //{}
		num_brackets_type2=0; //[]
		num_assertive_verbs=0;
		num_requestive_verbs=0;
		num_responsive_verbs=0;
		num_informative_verbs=0;
		num_code_lines=0;
		num_assertive_expressions=0;
		num_requestive_expressions=0;
		num_responsive_expressions=0;
		overall_sentiment=0; 
		
	}
	
	public void countAssertiveExpressions(String text){
		for(int i=0; i< assertive_expressions.size();i++)
		{
			if(text.contains(assertive_expressions.get(i)))
			{
				num_assertive_expressions++;
				//System.out.println("Contains is true");
			}
		}
	}
	
	public void countRequestiveExpressions(String text){
		for(int i=0; i< requestive_expressions.size();i++)
		{
			if(text.contains(requestive_expressions.get(i)))
			{
				num_requestive_expressions++;
				//System.out.println("Contains is true");
			}
		}
	}
	
	public void countResponsiveExpressions(String text){
		for(int i=0; i< responsive_expressions.size();i++)
		{
			if(text.contains(responsive_expressions.get(i)))
			{
				num_responsive_expressions++;
				//System.out.println("Contains is true");
			}
		}
	}
	
	public void test(){
		String text="However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK 21: // _ LOG Getting: 0 21:";
		System.out.println(execute(text));
//		countAssertiveExpressions(text);
//		System.out.println(num_assertive_expressions);
//		countRequestiveExpressions(text);
//		System.out.println(num_requestive_expressions);
//		countResponsiveExpressions(text);
//		System.out.println(num_responsive_expressions);
	}
	
	public static void main(String[] args) throws IOException{
		AttributeExtractor extractF = new AttributeExtractor();
		sentiwordnet = new SentiWord(pathToSWN);
		
		UserFeedback f1 = new UserFeedback();
		f1.setFeedbackText("However since _ XPropertySet ] 21: LOG Execute: getPropertySetInfo 21: Method getPropertySetInfo finished with state OK 21: // _ LOG Getting: 0 21:");
		
		List<UserFeedback> userFeedbacks = new ArrayList<UserFeedback>();
		userFeedbacks.add(f1);
		
		String arff = extractF.getARFF(userFeedbacks);
		
		System.out.println(arff);

		
//		extractF.test();

	}
}
