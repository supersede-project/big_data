package org.upc.dtim.bolster.metadatadatalayer.parsers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.upc.dtim.bolster.metadatadatalayer.util.Namespaces;
import scala.Tuple3;

import java.util.List;
import java.util.Map;

/**
 * Created by snadal on 2/06/16.
 */
public class OWLtoD3 {

    public static String parse(List<Tuple3<Resource,Property,Resource>> triples) {
        Namespaces ns = new Namespaces();

        List<Tuple3<Resource,Property,Resource>> elementsToShow = Lists.newArrayList();

        triples.iterator().forEachRemaining(triple -> {
            // Check that not (s,p,o) are from external namespaces at the same time
            // Check that s is not in one of the ignored namespaces
            if ((!ns.getNamespaces().contains(triple._1().getNameSpace()) ||
                !ns.getNamespaces().contains(triple._2().getNameSpace()) ||
                !ns.getNamespaces().contains(triple._3().getNameSpace())) &&
                !ns.getIgnoredNamespaces().contains(triple._1().getNameSpace())) {

                elementsToShow.add(triple);
            }
        });

        Map<String,Integer> nodesMap = Maps.newHashMap();
        Integer i = 0;
        JSONArray d3Nodes = new JSONArray();
        // Add classes as nodes
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (triple._3().getLocalName().equals("Class") && !nodesMap.containsKey(triple._1().getLocalName())) {
                nodesMap.put(triple._1().getLocalName(),i);
                ++i;
                JSONObject d3Node = new JSONObject();
                d3Node.put("name",triple._1().getLocalName());

                d3Nodes.add(d3Node);
            }
        }
        JSONArray d3Links = new JSONArray();
        // Add links
        for (Tuple3<Resource,Property,Resource> triple : elementsToShow) {
            if (!ns.getIgnoredNamespaces().contains(triple._2().getNameSpace())) {
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


}
