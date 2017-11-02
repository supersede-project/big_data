package eu.supersede.mdm.storage.model.bdi_ontology.metamodel;

import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;

/**
 * Created by snadal on 22/11/16.
 */
public enum Rules {

    ECA_RULE(Namespaces.R.val()+"Rule/"),




    CONDITION(Namespaces.R.val()+"Condition/"),
    HAS_CONDITION(Namespaces.R.val()+"hasCondition/"),
    VALUE(Namespaces.R.val()+"Value/"),
    HAS_VALUE(Namespaces.R.val()+"hasValue/"),
    HAS_FEATURE(Namespaces.R.val()+"hasFeature/"),
    PREDICATE(Namespaces.R.val()+"Predicate/"),
    HAS_PREDICATE(Namespaces.R.val()+"hasPredicate/"),
    ACTION(Namespaces.R.val()+"Action/"),
    HAS_ACTION(Namespaces.R.val()+"hasAction/");

    private String element;

    Rules(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}
