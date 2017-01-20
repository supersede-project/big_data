package eu.supersede.mdm.storage.model.bdi_ontology.eca_rules;

/**
 * Created by snadal on 20/01/17.
 */
public enum PredicatesTypes {
    GT(">"),
    EQ("="),
    LT("<");

    private String element;

    PredicatesTypes(String element) {
            this.element = element;
        }

    public String val() {
            return element;
        }
}