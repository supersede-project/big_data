/**
 * Created by snadal on 21/12/16.
 */

const Namespaces = {
    S: "http://www.BDIOntology.com/source/",
    G: "http://www.BDIOntology.com/global/",
    owl: "http://www.w3.org/2002/07/owl#",
    rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
    rdfs:"http://www.w3.org/2000/01/rdf-schema#",
    dct: "http://purl.org/dc/terms/",
    dcat: "http://www.w3.org/ns/dcat#"
};

const Global = {
    CONCEPT: Namespaces.G+"Concept",
    FEATURE: Namespaces.G+"Feature",
    HAS_FEATURE: Namespaces.G+"hasFeature",
    INTEGRITY_CONSTRAINT: Namespaces.G+"IntegrityConstraint",
    HAS_INTEGRITY_CONSTRAINT: Namespaces.G+"hasConstraint",
    DATATYPE: Namespaces.rdfs+"Datatype",
    HAS_DATATYPE: Namespaces.G+"hasDatatype"
}

function getGlobalEdge(namespaceOrigin, namespaceDest) {
    if (namespaceOrigin == Global.CONCEPT && namespaceDest == Global.FEATURE) return Global.HAS_FEATURE;
    if (namespaceOrigin == Global.FEATURE && namespaceDest == Global.INTEGRITY_CONSTRAINT) return Global.HAS_INTEGRITY_CONSTRAINT;
    if (namespaceOrigin == Global.FEATURE && namespaceDest == Global.DATATYPE) return Global.HAS_DATATYPE;

    return null;
}