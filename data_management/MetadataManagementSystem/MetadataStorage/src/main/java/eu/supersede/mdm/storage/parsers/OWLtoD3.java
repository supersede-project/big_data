package eu.supersede.mdm.storage.parsers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.supersede.mdm.storage.bdi_ontology.metamodel.BolsterMetamodel;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.jgrapht.graph.DefaultEdge;
import eu.supersede.mdm.storage.util.NamespaceFiles;
import scala.Tuple3;

import java.util.List;
import java.util.Map;

/**
 * Created by snadal on 2/06/16.
 */
public class OWLtoD3 {

    public static String parse(String artifactType, List<Tuple3<Resource,Property,Resource>> triples) {
        NamespaceFiles ns = new NamespaceFiles();

        List<Tuple3<Resource,Property,Resource>> elementsToShow = Lists.newArrayList();

        triples.iterator().forEachRemaining(triple -> {
            // Check that not (s,p,o) are from external namespaces at the same time
            // Check that s is not in one of the ignored namespaces
            // Check that s is not part of the BDI ontology
            if ((!ns.getNamespaces().contains(triple._1().getNameSpace()) ||
                    !ns.getNamespaces().contains(triple._2().getNameSpace()) ||
                    !ns.getNamespaces().contains(triple._3().getNameSpace())) &&
                    !ns.getIgnoredNamespaces().contains(triple._1().getNameSpace()) &&
                    !BolsterMetamodel.contains(artifactType,triple._1().getURI())) {

                elementsToShow.add(triple);
            }
        });

        Map<String,Integer> nodesMap = Maps.newHashMap();
        Integer i = 0;
        JSONArray d3Nodes = new JSONArray();
        // Add classes as nodes
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!nodesMap.containsKey(triple._1().getURI())) {
                nodesMap.put(triple._1().getURI(), i);
                ++i;
                JSONObject d3Node = new JSONObject();
                d3Node.put("name", triple._1().getURI());

                d3Nodes.add(d3Node);
            }
        }
        JSONArray d3Links = new JSONArray();
        // Add links
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!ns.getIgnoredNamespaces().contains(triple._2().getNameSpace())/* &&
                    BolsterMetamodel.metamodel.get(artifactType).contains(triple._2().toString())*/) {

                JSONObject d3Link = new JSONObject();
                d3Link.put("source",nodesMap.get(triple._1().getURI()));
                d3Link.put("target",nodesMap.get(triple._3().getURI()));
                d3Link.put("name", triple._2().getLocalName());

                d3Links.add(d3Link);
            }
        }

        JSONObject d3 = new JSONObject();
        d3.put("nodes",d3Nodes);
        d3.put("links",d3Links);

        return d3.toJSONString();
    }
/*
    private static String generateD3ForDomainOntology(List<Tuple3<Resource,Property,Resource>> elementsToShow) {
        Map<String,Integer> nodesMap = Maps.newHashMap();
        Integer i = 0;
        JSONArray d3Nodes = new JSONArray();
        DirectedAcyclicGraph<Integer,DefaultEdge> G = new DirectedAcyclicGraph<Integer, DefaultEdge>(DefaultEdge.class);

        // Add classes as nodes
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (triple._3().toString().equals("http://www.w3.org/2002/07/owl#Class")) {
                nodesMap.put(triple._1().getLocalName(),i);
                G.addVertex(i);
                ++i;
                JSONObject d3Node = new JSONObject();
                d3Node.put("name",triple._1().getLocalName());
                d3Nodes.add(d3Node);
            }
        }

        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!new NamespaceFiles().getIgnoredNamespaces().contains(triple._2().getNameSpace())
                    && nodesMap.get(triple._1().getLocalName()) != nodesMap.get(triple._3().getLocalName())) {
                G.addEdge(nodesMap.get(triple._1().getLocalName()),nodesMap.get(triple._3().getLocalName()));
            }
        }

        eu.supersede.mdm.storage.util.TransitiveReduction.prune(G);

        JSONArray d3Links = new JSONArray();
        // Add links
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!new NamespaceFiles().getIgnoredNamespaces().contains(triple._2().getNameSpace())
                    && G.containsEdge(nodesMap.get(triple._1().getLocalName()),nodesMap.get(triple._3().getLocalName()))) {

                JSONObject d3Link = new JSONObject();
                d3Link.put("source",nodesMap.get(triple._1().getLocalName()));
                d3Link.put("target",nodesMap.get(triple._3().getLocalName()));
                d3Link.put("name", triple._2().getLocalName());

                d3Links.add(d3Link);
            }
        }

        JSONObject d3 = new JSONObject();
        d3.put("nodes",d3Nodes);
        d3.put("links",d3Links);

        return d3.toJSONString();

    }
*/
}
