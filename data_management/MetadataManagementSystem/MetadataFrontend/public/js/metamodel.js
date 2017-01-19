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
    CONCEPT: {
        iri: Namespaces.G+"Concept",
        name: "Concept",
        color: "#33CCCC"
    },
    FEATURE: {
        iri: Namespaces.G+"Feature",
        name: "Feature",
        color: "#D7DF01"
    },
    HAS_FEATURE: {
        iri: Namespaces.G+"hasFeature",
        name: "hasFeature",
        color: "#D7DF01"
    },
    INTEGRITY_CONSTRAINT: {
        iri: Namespaces.G+"IntegrityConstraint",
        name: "IntegrityConstraint",
        color: "#CC99FF"
    },
    HAS_INTEGRITY_CONSTRAINT: {
        iri: Namespaces.G+"hasConstraint",
        name: "hasConstraint",
        color: "#CC99FF"
    },
    DATATYPE: {
        iri: Namespaces.rdfs+"Datatype",
        name: "Datatype",
        color: "#FF6600"
    },
    HAS_DATATYPE: {
        iri: Namespaces.G+"hasDatatype",
        name: "hasDatatype",
        color: "#FF6600"
    }
}

function getGlobalEdge(namespaceOrigin, namespaceDest) {
    if (namespaceOrigin == Global.CONCEPT.iri && namespaceDest == Global.FEATURE.iri) return Global.HAS_FEATURE.iri;
    if (namespaceOrigin == Global.FEATURE.iri && namespaceDest == Global.INTEGRITY_CONSTRAINT.iri) return Global.HAS_INTEGRITY_CONSTRAINT.iri;
    if (namespaceOrigin == Global.FEATURE.iri && namespaceDest == Global.DATATYPE.iri) return Global.HAS_DATATYPE.iri;

    return null;
}