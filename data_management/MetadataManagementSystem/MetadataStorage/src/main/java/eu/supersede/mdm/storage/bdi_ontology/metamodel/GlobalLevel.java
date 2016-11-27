package eu.supersede.mdm.storage.bdi_ontology.metamodel;

import eu.supersede.mdm.storage.bdi_ontology.Namespaces;

/**
 * Created by snadal on 22/11/16.
 */
public enum GlobalLevel {

    CONCEPT(Namespaces.G+"Concept"),
    FEATURE(Namespaces.G+"Feature"),
    HAS_FEATURE(Namespaces.G+"hasFeature"),
    INTEGRITY_CONSTRAINT(Namespaces.G+"IntegrityConstraint"),
    HAS_INTEGRITY_CONSTRAINT(Namespaces.G+"hasConstraint"),
    DATATYPE(Namespaces.rdfs+"Datatype"),
    HAS_DATATYPE(Namespaces.G+"hasDatatype");

    private String element;

    GlobalLevel(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
