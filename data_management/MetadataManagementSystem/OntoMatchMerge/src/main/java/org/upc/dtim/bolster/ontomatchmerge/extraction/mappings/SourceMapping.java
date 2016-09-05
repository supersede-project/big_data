package org.upc.dtim.bolster.ontomatchmerge.extraction.mappings;

/**
 *
 * @author Petar
 *
 * This class represents individual source mapping of the particular ontology concept.
 *
 */
public class SourceMapping {

    private String ontology_type; // concept(class)/property
    private String ontology_id;
  
    private Mapping mapping; // mapping of the above concept over the sources

    /**
     * @return the ontology_id
     */
    public String getOntology_id() {
        return ontology_id;
    }

    /**
     * @param ontology_id the ontology_id to set
     */
    public void setOntology_id(String ontology_id) {
        if (this.ontology_id != null) {
            this.ontology_id += ontology_id;
        } else {
            this.ontology_id = ontology_id;
        }
    }

    /**
     * @return the ontology_type
     */
    public String getOntology_type() {
        return ontology_type;
    }

    /**
     * @param ontology_type the ontology_type to set
     */
    public void setOntology_type(String ontology_type) {
        this.ontology_type = ontology_type;
    }

    /**
     * @return the mapping
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    public void addMapping(Mapping mapping) {
        this.mapping.addMapping(mapping);
    }


    @Override
    public SourceMapping clone (){
        SourceMapping sm = new SourceMapping();
        sm.setOntology_id(ontology_id);
        sm.setOntology_type(ontology_type);

        Mapping m = new Mapping();
        for (int i = 0 ; i<mapping.getMappings().size() ; i++) m.addMapping(mapping.getMappings().get(i));
        for (int i = 0 ; i<mapping.getProjections_attr().size() ; i++) m.addProjections_attr(mapping.getProjections_attr().get(i));
        for (int i = 0 ; i<mapping.getSelections().size() ; i++) m.addSelection(mapping.getSelections().get(i));
        for (int i = 0 ; i<mapping.getOperators().size() ; i++) m.addOperator(mapping.getOperators().get(i));
        m.setTablename(mapping.getTablename());

        return sm;

    }

}
