package eu.supersede.mdm.storage.omq;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.GlobalOntology;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitorBase;
import org.apache.jena.sparql.algebra.OpWalker;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.algebra.optimize.VariableUsagePopper;
import org.apache.jena.sparql.algebra.optimize.VariableUsageTracker;
import org.apache.jena.sparql.algebra.optimize.VariableUsageVisitor;
import org.apache.jena.sparql.core.BasicPattern;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryRewriting {

    private Dataset T;

    private Set<String> PI;

    // We keep two representations of Q_G.\varphi to simplify its manipulation
    //  1) As a pattern to easily access its triples as a list
    //  2) As an ontology so it can be queried via SPARQL
    private BasicPattern PHI_p; //PHI_pattern
    private OntModel PHI_o; // PHI_ontology

    public QueryRewriting(String SPARQL, Dataset d) {
        T = d;

        // Compile the SPARQL using ARQ and generate its <pi,phi> representation
        Query q = QueryFactory.create(SPARQL);
        Op ARQ = Algebra.compile(q);

        PI = Sets.newHashSet();
        ((OpTable)((OpJoin)((OpProject)ARQ).getSubOp()).getLeft()).getTable().rows().forEachRemaining(r -> {
            r.vars().forEachRemaining(v -> PI.add(r.get(v).getURI()));
        });

        this.PHI_p = ((OpBGP)((OpJoin)((OpProject)ARQ).getSubOp()).getRight()).getPattern();
        PHI_o = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
            RDFUtil.addTriple(PHI_o, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );
    }

    public String rewrite() {
        /**
         * Query expansion
         */
        /** 1 Identify query-related concepts **/
        List<String> concepts = Lists.newArrayList();
        // First, create a graph of the pattern in order to obtain its topological sort
        DirectedAcyclicGraph<String,String> conceptsGraph = new DirectedAcyclicGraph<String, String>(String.class);
        PHI_p.getList().forEach(t -> {
            // Add only concepts so its easier to populate later the list of concepts
            if (!t.getPredicate().getURI().equals(GlobalOntology.HAS_FEATURE.val())) {
                try {
                    conceptsGraph.addVertex(t.getSubject().getURI());
                    conceptsGraph.addVertex(t.getObject().getURI());
                    conceptsGraph.addDagEdge(t.getSubject().getURI(), t.getObject().getURI(), t.getPredicate().getURI());
                } catch (DirectedAcyclicGraph.CycleFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        // Now, iterate using a topological sort adding the concepts to the list of concepts
        conceptsGraph.iterator().forEachRemaining(vertex -> concepts.add(vertex));

        /** 2 Expand Q_G with IDs **/
        concepts.forEach(c -> {
            ResultSet IDs = RDFUtil.runAQuery("SELECT ?t " +
                    "FROM <"+Namespaces.sup.val()+"BDIOntology> "+
                    "WHERE { " +
                    "<"+c+"> <"+GlobalOntology.HAS_FEATURE.val()+"> ?t . " +
                    "?t <"+ Namespaces.rdfs.val()+"subClassOf> <"+ Namespaces.sc.val()+"identifier> " +
                    "}", T);
            IDs.forEachRemaining(id -> {
                if (!PHI_p.getList().contains(Triple.create(NodeFactory.createURI(c),
                        NodeFactory.createURI(GlobalOntology.HAS_FEATURE.val()),id.get("t").asNode()))) {
                    PHI_p.add(Triple.create(NodeFactory.createURI(c),
                            NodeFactory.createURI(GlobalOntology.HAS_FEATURE.val()), id.get("t").asNode()));
                    RDFUtil.addTriple(PHI_o, c, GlobalOntology.HAS_FEATURE.val(), id.get("t").asResource().getURI());
                }
            });
        });

        /**
         * Intra-concept generation
         */
        // 3 Identify queried features
        concepts.forEach(c -> {
            Map PartialWalksPerWrapper = Maps.newHashMap();
            System.out.println(c);
            ResultSet features = RDFUtil.runAQuery("SELECT ?f " +
                    "WHERE {<"+c+"> <"+GlobalOntology.HAS_FEATURE.val()+"> ?f }",PHI_o);
            features.forEachRemaining(fRes -> {
                String f = fRes.get("f").asResource().getURI();
                System.out.println("    "+f);
                ResultSet wrappers = RDFUtil.runAQuery("SELECT ?g " +
                        "WHERE { GRAPH ?g { <"+c+"> <"+GlobalOntology.HAS_FEATURE.val()+"> <"+f+"> } }",T);
                wrappers.forEachRemaining(wRes -> {
                    //String w = fRes.get("g").asResource().getURI();
                    System.out.println("        "+wRes);
                });
                System.out.println(f);
            });
        });

        // 4 Unfold LAV mappings

        // 5 Find attributes in S

        // 6 Prune output

        /**
         * Inter-concept generation
         */
        // 7 Compute cartesian product

        // 8 Merge walks

        // 9 Discover join wrappers

        // 10 Discover join attribute

        return "";
    }

}
