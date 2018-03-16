package eu.supersede.bdma.sa.eca_rules.conditions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.GermanFeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.clustering.FeedbackAnnotator;
import eu.supersede.feedbackanalysis.ds.SentimentAnalysisResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.sentiment.GermanSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.MLSentimentAnalyzer;
import eu.supersede.feedbackanalysis.sentiment.SentimentAnalyzer;
import eu.supersede.integration.api.mdm.types.Parameter;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import java.util.*;
import java.util.stream.Collectors;

public class ConditionEvaluator {

    private static Map<String, String> operators = ImmutableMap.<String,String>builder()
        .put("EQUAL", "==")
        .put("NOT_EQUAL","!=")
        .put("GREATER_THAN",">")
        .put("GREATER_OR_EQUAL",">=")
        .put("LESS_THAN","<")
        .put("LESS_OR_EQUAL","<=")
            .build();

    public static KnowledgePackage compilePkgDescr(PackageDescr pkg ) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newDescrResource( pkg ),
                ResourceType.DESCR );
        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        return kpkgs.iterator().next();
    }

    public static int evaluateNumericRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateNumericRule("+operator+","+ruleValue+","+ Arrays.toString(values));
        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("numericRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();
        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        for (String strNum : values) {
            ksession.insert(new DoubleCondition(Double.parseDouble(strNum)));
        }
        int nRules = ksession.fireAllRules();
        return nRules;
    }

    public static int evaluateTextualRule(String operator, String ruleValue, String[] values) {
        if (operator.equals("CONTAINS")) return values.length;

        operator = operators.get(operator);
        System.out.println("evaluateTextualRule("+operator+","+ruleValue+","+ Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("textualRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.TextCondition").constraint("x "+operator+" \""+ruleValue+"\"").end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();
        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        for (String str : values) {
            ksession.insert(new TextCondition(str));
        }
        int nRules = ksession.fireAllRules();
        return nRules;
        /*
        int nRules = 0;
        // TODO: compare strings using Drools
        for (String val : values) {
            if (val.equals(ruleValue)) ++nRules;
        }
        return nRules;
        */
    }

    public static int evaluateEnglishFeedbackClassifierRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateEnglishFeedbackClassifierRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("FeedbackClassifierRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.TextCondition").constraint("x "+operator+" \""+ruleValue+"\"").end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String path = Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        for (String str : values) {
            String label = null;
            try {
                label = feedbackClassifier.classify(path, new UserFeedback(str)).getLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Extracted value ["+str+"]");
            //Sockets.sendMessageToSocket("analysis","Extracted value: "+str);
            System.out.println("Classified as ["+label+"]");
            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new TextCondition(label));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateEnglishOverallSentimentRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateEnglishOverallSentimentRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("OverallSentimentRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        String path = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
        for (String str : values) {
            SentimentAnalyzer sa = new MLSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(path,new UserFeedback(str));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new DoubleCondition(saRes.getOverallSentiment()));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateEnglishPositiveSentimentRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateEnglishPositiveSentimentRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("OverallSentimentRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        String path = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
        for (String str : values) {
            SentimentAnalyzer sa = new MLSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(path,new UserFeedback(str));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new DoubleCondition(saRes.getPositiveSentiment()));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateEnglishNegativeSentimentRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateEnglishNegativeSentimentRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("OverallSentimentRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        String path = Thread.currentThread().getContextClassLoader().getResource("sentiment_classifier.model").toString().replace("file:","");
        for (String str : values) {
            SentimentAnalyzer sa = new MLSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(path,new UserFeedback(str));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new DoubleCondition(saRes.getNegativeSentiment()));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateGermanFeedbackClassifierRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateGermanFeedbackClassifierRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("GermanFeedbackClassifierRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.TextCondition").constraint("x "+operator+" \""+ruleValue+"\"").end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        FeedbackClassifier feedbackClassifier = new GermanFeedbackClassifier();
        String path = Thread.currentThread().getContextClassLoader().getResource("german_classify.model").toString().replace("file:","");
        for (String str : values) {
            String label = null;
            try {
                label = feedbackClassifier.classify(path, new UserFeedback(str)).getLabel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Extracted value ["+str+"]");
            //Sockets.sendMessageToSocket("analysis","Extracted value: "+str);
            System.out.println("Classified as ["+label+"]");
            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new TextCondition(label));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateGermanOverallSentimentRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateGermanOverallSentimentRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("OverallGermanSentimentRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        String path = Thread.currentThread().getContextClassLoader().getResource("german_sentiment.model").toString().replace("file:","");
        for (String str : values) {
            SentimentAnalyzer sa = new GermanSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(path,new UserFeedback(str));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new DoubleCondition(saRes.getOverallSentiment()));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateGermanPositiveSentimentRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateGermanPositiveSentimentRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("PositiveGermanSentimentRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        String path = Thread.currentThread().getContextClassLoader().getResource("german_sentiment.model").toString().replace("file:","");
        for (String str : values) {
            SentimentAnalyzer sa = new GermanSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(path,new UserFeedback(str));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new DoubleCondition(saRes.getPositiveSentiment()));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateGermanNegativeSentimentRule(String operator, String ruleValue, String[] values) {
        operator = operators.get(operator);
        System.out.println("evaluateGermanNegativeSentimentRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("NegativeGermanSentiment")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        String path = Thread.currentThread().getContextClassLoader().getResource("german_sentiment.model").toString().replace("file:","");
        for (String str : values) {
            SentimentAnalyzer sa = new GermanSentimentAnalyzer();
            SentimentAnalysisResult saRes = null;
            try {
                saRes = sa.classify(path,new UserFeedback(str));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new DoubleCondition(saRes.getNegativeSentiment()));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }

    public static int evaluateOntologicalDistanceRule(String operator, String ruleValue, String[] values, List<Parameter> parameters, String tenant) {
        operator = operators.get(operator);
        System.out.println("evaluateOntologicalDistanceRule("+operator+","+ruleValue+","+Arrays.toString(values)+", "+parameters);

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("OntologicalDistance")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.DoubleCondition").constraint("x "+operator+" "+Double.parseDouble(ruleValue)).end()
                        .end()
                        .rhs("System.out.println(\"\");")
                        .end()
                        .getDescr();

        KnowledgePackage kpkg = compilePkgDescr(pkg);
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        Set<String> keywords = Sets.newHashSet(parameters.stream().filter(kv -> kv.getKey().equals("keyword"))
                .map(kv -> kv.getValue()).collect(Collectors.toList()));

        String ontologyFile = Thread.currentThread().getContextClassLoader().getResource(Utils.getOntologyPath(tenant))
                .toString().replace("file:","");;
        boolean classLabelsOnly = false;
        boolean direct = true;
        String language = "en";
        String wordnetDbPath = Main.properties.getProperty("WORDNET_DB_PATH");
        FeedbackAnnotator feedbackAnnotator = new FeedbackAnnotator(ontologyFile, wordnetDbPath, language, classLabelsOnly, direct);
        for (String str : values) {
            double ontologicalDistance = feedbackAnnotator.ontologicalDistance(new UserFeedback(str), keywords);
            ksession.insert(ontologicalDistance);
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }


}
