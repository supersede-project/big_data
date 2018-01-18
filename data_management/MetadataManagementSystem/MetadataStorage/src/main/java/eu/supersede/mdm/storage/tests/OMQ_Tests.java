package eu.supersede.mdm.storage.tests;

import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.GlobalOntology;
import eu.supersede.mdm.storage.omq.QueryRewriting;
import eu.supersede.mdm.storage.omq.model.Walk;
import eu.supersede.mdm.storage.util.RDFUtil;
import jdk.nashorn.internal.objects.Global;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;

import java.util.Set;

/**
 * Created by snadal on 20/06/17.
 */
public class OMQ_Tests {

    public static Dataset getInfSystRunningExample() {
        Dataset dataset = TDBFactory.createDataset();
        Model m = ModelFactory.createDefaultModel();
        m.read("/home/snadal/UPC/Sergi/Papers/InformationSystems2017/BDI_ontology_nodomainrange.ttl");
        dataset.addNamedModel(Namespaces.T.val(),m);

        OntModel mappings_wrapper_1 = ModelFactory.createOntologyModel();
        RDFUtil.addTriple(mappings_wrapper_1, Namespaces.sup.val()+"InfoMonitor", GlobalOntology.HAS_FEATURE.val(), Namespaces.sup.val()+"lagRatio");
        //RDFUtil.addTriple(mappings_wrapper_1, Namespaces.sup.val()+"Monitor", Namespaces.sup.val()+"generatesQoS", Namespaces.sup.val()+"InfoMonitor");
        RDFUtil.addTriple(mappings_wrapper_1, Namespaces.sup.val()+"InfoMonitor", Namespaces.sup.val()+"generatesQoS", Namespaces.sup.val()+"Monitor");
        RDFUtil.addTriple(mappings_wrapper_1, Namespaces.sup.val()+"Monitor", GlobalOntology.HAS_FEATURE.val(), Namespaces.sup.val()+"monitorId");
        dataset.addNamedModel(Namespaces.sup.val()+"W1",mappings_wrapper_1);

        OntModel mappings_wrapper_3 = ModelFactory.createOntologyModel();
        //RDFUtil.addTriple(mappings_wrapper_3,Namespaces.sc.val()+"SoftwareApplication", Namespaces.sup.val()+"hasMonitor", Namespaces.sup.val()+"Monitor");
        RDFUtil.addTriple(mappings_wrapper_3,Namespaces.sup.val()+"Monitor", Namespaces.sup.val()+"hasMonitor", Namespaces.sc.val()+"SoftwareApplication");
        RDFUtil.addTriple(mappings_wrapper_3,Namespaces.sc.val()+"SoftwareApplication", Namespaces.sup.val()+"hasFG", Namespaces.sup.val()+"FeedbackGathering");
        RDFUtil.addTriple(mappings_wrapper_3,Namespaces.sc.val()+"SoftwareApplication", GlobalOntology.HAS_FEATURE.val(), Namespaces.sup.val()+"applicationId");
        RDFUtil.addTriple(mappings_wrapper_3,Namespaces.sup.val()+"Monitor", GlobalOntology.HAS_FEATURE.val(), Namespaces.sup.val()+"monitorId");
        RDFUtil.addTriple(mappings_wrapper_3,Namespaces.sup.val()+"FeedbackGathering", GlobalOntology.HAS_FEATURE.val(), Namespaces.sup.val()+"feedbackGatheringId");
        dataset.addNamedModel(Namespaces.sup.val()+"W3",mappings_wrapper_3);

        OntModel mappings_wrapper_2 = ModelFactory.createOntologyModel();
        RDFUtil.addTriple(mappings_wrapper_2, Namespaces.sup.val()+"FeedbackGathering", Namespaces.sup.val()+"generatesOpinion", Namespaces.duv.val()+"UserFeedback");
        RDFUtil.addTriple(mappings_wrapper_2, Namespaces.sup.val()+"FeedbackGathering", GlobalOntology.HAS_FEATURE.val(), Namespaces.sup.val()+"feedbackGatheringId");
        RDFUtil.addTriple(mappings_wrapper_2, Namespaces.duv.val()+"UserFeedback", GlobalOntology.HAS_FEATURE.val(), Namespaces.dct.val()+"description");
        dataset.addNamedModel(Namespaces.sup.val()+"W2",mappings_wrapper_2);

        return dataset;
    }

    public static void main(String[] args) throws Exception {
        Dataset d = getInfSystRunningExample();

        String prefixes =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX sup: <http://www.supersede.eu/> " +
                        "PREFIX G: <http://www.essi.upc.edu/~snadal/BDIOntology/Global/> " +
                        "PREFIX sc: <http://schema.org/> ";

        String SPARQL =
                prefixes + "\n" +
                "SELECT ?x ?y\n" +
                "WHERE {\n" +
                "VALUES ( ?x ?y ) { ( sup:applicationId sup:lagRatio ) }\n" +
                "sc:SoftwareApplication G:hasFeature sup:applicationId .\n" +
                //"sc:SoftwareApplication sup:hasMonitor sup:Monitor .\n" +
                //"sup:Monitor sup:generatesQoS sup:InfoMonitor .\n" +
                "sup:Monitor sup:hasMonitor sc:SoftwareApplication .\n" +
                "sup:InfoMonitor sup:generatesQoS sup:Monitor .\n" +
                "sup:InfoMonitor G:hasFeature sup:lagRatio\n" +
                "}";

        QueryRewriting qr = new QueryRewriting(SPARQL,d);
        Set<Walk> walks = qr.rewrite();

        System.out.println("############################");
        System.out.println("Output of rewriting");
        walks.forEach(w -> {
            System.out.println(w);
        });
        //String relational_algebra = qr.rewrite();
        //System.out.println(relational_algebra);
    }

}
