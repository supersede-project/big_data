package eu.supersede.bdma.sa.tests;

import eu.supersede.bdma.sa.StreamProcessing;
import eu.supersede.bdma.sa.eca_rules.conditions.TextCondition;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieRepository;
import org.kie.api.definition.KieDescr;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.definition.KnowledgeDescr;
import org.kie.internal.definition.KnowledgePackage;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.runtime.StatelessKnowledgeSession;
import scala.Tuple1;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by snadal on 17/01/17.
 */
public class RulesTests {

    private static KnowledgePackage compilePkgDescr( PackageDescr pkg ) {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newDescrResource( pkg ),
                ResourceType.DESCR );
        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        return kpkgs.iterator().next();
    }

    public static void main(String[] args) {
        PackageDescr pkg =
                DescrFactory.newPackage()
                .name("testPkg")
                .newRule().name("testRule")
                .lhs()
                .pattern("eu.supersede.bdma.sa.eca_rules.conditions.TextCondition").constraint("x == \"ahi\"").end()
                .end()
                .rhs("System.out.println(\"rule ok\");")
                .end()
                .getDescr();

        /*KieServices kieServices = KieServices.Factory.get();
        KieResources kieResources = kieServices.getResources();
        KieRepository kieRepository = kieServices.getRepository();

        Resource resource = kieResources. newDescrResource(pkg);
        kieRepository.addKieModule(resource);

        KieContainer kContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
        StatelessKieSession ksession = kContainer.newStatelessKieSession();*/

        KnowledgePackage kpkg = compilePkgDescr( pkg );
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(Collections.singleton(kpkg));
        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();

        eu.supersede.bdma.sa.eca_rules.conditions.TextCondition x = new TextCondition("hi");

        ksession.execute(x);

    }
}
