package eu.supersede.bdma.sa.eca_rules.conditions;

import com.google.common.collect.ImmutableMap;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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
        operator = operators.get(operator);
        System.out.println("evaluateTextualRule("+operator+","+ruleValue+","+ Arrays.toString(values));
        /*
        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("textualRule")
                        .lhs()
                        .pattern("eu.supersede.bdma.sa.eca_rules.conditions.TextCondition")
                            .constraint("java.util.Objects.equals(x,"+ruleValue+")").end()
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
        return nRules;*/
        int nRules = 0;
        // TODO: compare strings using Drools
        for (String val : values) {
            if (val.equals(ruleValue)) ++nRules;
        }
        return nRules;
    }

    public static int evaluateFeedbackRule(String operator, String ruleValue, String[] values) throws Exception {
        if (operator.equals("=")) operator = "==";
        System.out.println("evaluateFeedbackRule("+operator+","+ruleValue+","+Arrays.toString(values));

        PackageDescr pkg =
                DescrFactory.newPackage()
                        .name("sa.pkg")
                        .newRule().name("numericRule")
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
            String label = feedbackClassifier.classify(path, new UserFeedback(str)).getLabel();
            System.out.println("Extracted value ["+str+"]");
            Sockets.sendMessageToSocket("analysis","Extracted value: "+str);
            System.out.println("Classified as ["+label+"]");
            Sockets.sendMessageToSocket("analysis","Classified as: "+label);
            ksession.insert(new TextCondition(label));
        }
        int nRules = ksession.fireAllRules();
        System.out.println(nRules + " satisfy the condition");
        return nRules;
    }


}
