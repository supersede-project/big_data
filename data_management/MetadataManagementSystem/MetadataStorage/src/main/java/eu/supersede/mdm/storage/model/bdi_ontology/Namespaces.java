package eu.supersede.mdm.storage.model.bdi_ontology;

/**
 * Created by snadal on 22/11/16.
 */
public enum Namespaces {

    S("http://www.BDIOntology.com/source/"),
    G("http://www.BDIOntology.com/global/"),
    M("http://www.BDIOntology.com/mappings/"),
    R("http://www.BDIOntology.com/eca_rule/"),

    owl("http://www.w3.org/2002/07/owl#"),
    rdf("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
    rdfs("http://www.w3.org/2000/01/rdf-schema#"),
    dct("http://purl.org/dc/terms/"),
    dcat("http://www.w3.org/ns/dcat#");

    private String element;

    Namespaces(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}