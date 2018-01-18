package eu.supersede.mdm.storage.model.bdi_ontology.metamodel;

import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;

/**
 * Created by snadal on 6/06/17.
 */
public enum GlobalOntology {

    CONCEPT(Namespaces.G.val()+"Concept"),
    FEATURE(Namespaces.G.val()+"Feature"),
    HAS_FEATURE(Namespaces.G.val()+"hasFeature"),
    INTEGRITY_CONSTRAINT(Namespaces.G.val()+"IntegrityConstraint"),
    HAS_INTEGRITY_CONSTRAINT(Namespaces.G.val()+"hasConstraint"),
    DATATYPE(Namespaces.rdfs.val()+"Datatype"),
    HAS_DATATYPE(Namespaces.G.val()+"hasDatatype");

    private String element;

    GlobalOntology(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
