package org.upc.dtim.bolster.ontomatchmerge.extraction;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;
import com.hp.hpl.jena.ontology.impl.RestrictionImpl;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import java.util.Set;

/**
 * Created by snadal on 11/06/16.
 */
public class OntologyAdapter {

    private static void addTriple(OntModel model, String s, String p, String o) {
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
    }

    public static OntModel adaptForPhysicalOntology(OntModel model) {
        /**
         * Sergi:
         *
         * For simple JSONs now, the class that is not null is the name of the file.
         * The other classes are the attributes that have to be linked with hasAttribute, we'll define them as new classes
         * and then do the linking.
         */
        String centralURI = null;
        Set<String> attributes = Sets.newHashSet();

        for (OntClass aClass : model.listClasses().toSet()) {
            // The name of the dataset
            if (!aClass.hasSubClass()) {
                centralURI = aClass.getNameSpace().replace("#","") + "/PHYSICAL#"+ aClass.getLocalName();
                //centralURI = aClass.getURI();
            } else {
                if (!((RestrictionImpl) aClass).getOnProperty().getLocalName().equals("textContent")) {
                    OntProperty prop = ((RestrictionImpl) aClass).getOnProperty();

                    attributes.add(prop.getNameSpace().replace("#","") + "/PHYSICAL#"+ prop.getLocalName());
                }
            }
        }

        OntModel newModel = ModelFactory.createOntologyModel();
        addTriple(newModel,  "http://Bolster/hasAttribute", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/2002/07/owl#AnnotationProperty");

        addTriple(newModel, centralURI, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/2002/07/owl#Class");
        for (String a : attributes) {
            addTriple(newModel, a, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/2002/07/owl#Class");
        }

        for (String a : attributes) {
            addTriple(newModel, centralURI, "http://Bolster/hasAttribute", a);
        }

        return newModel;
    }

    public static OntModel adaptForLogicalOntology(OntModel model) {
        /**
         * Sergi:
         *
         * Here we already assume the format provided by adaptForPhysicalOntology
         */
        OntModel newModel = ModelFactory.createOntologyModel();

        addTriple(newModel,  "http://Bolster/mapsTo", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/2002/07/owl#AnnotationProperty");

        for (OntClass aClass : model.listClasses().toSet()) {
            if (aClass.hasSubClass()) {
                if (!((RestrictionImpl) aClass).getOnProperty().getLocalName().equals("textContent")) {
                    OntProperty prop = ((RestrictionImpl) aClass).getOnProperty();
                    addTriple(newModel,prop.getNameSpace().replace("#","") + "/LOGICAL#"+ prop.getLocalName(),
                            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                            "http://www.w3.org/2002/07/owl#Class");

                    addTriple(newModel,prop.getNameSpace().replace("#","") + "/LOGICAL#"+ prop.getLocalName(),
                            "http://Bolster/mapsTo",
                            prop.getNameSpace().replace("#","") + "/PHYSICAL#"+ prop.getLocalName());
                }
            }
        }
        return newModel;
    }

}
