package eu.supersede.mdm.storage.model.bdi_ontology.metamodel;

import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;

/**
 * Created by snadal on 22/11/16.
 */
public enum EventOntology {

    ATTRIBUTE(Namespaces.E.val()+"Attribute"),
    HAS_ATTRIBUTE(Namespaces.E.val()+"hasAttribute"),
    EMBEDDED_OBJECT(Namespaces.E.val()+"EmbeddedObject"),
    HAS_EMBEDDED_OBJECT(Namespaces.E.val()+"hasEmbeddedObject"),
    ARRAY(Namespaces.E.val()+"Array"),
    HAS_ARRAY(Namespaces.E.val()+"hasArray"),
    EVENT(Namespaces.E.val()+"Event"),
    MEDIA_TYPE(Namespaces.dcat.val()+"mediaType"),
    FORMAT(Namespaces.dct.val()+"format"),

    KAFKA_TOPIC(Namespaces.E.val()+"KafkaTopic"),
    HAS_KAFKA_TOPIC(Namespaces.E.val()+"hasKafkaTopic");

    private String element;

    EventOntology(String element) {
        this.element = element;
    }

    public String val() {
        return element;
    }
}