package org.upc.dtim.bolster.ontomatchmerge.extraction.mappings;

import java.util.ArrayList;

/**
 *
 * @author Petar
 *
 * This class represents the mapping over the sources
 *
 */
public class Mapping {
    // if the mapping is derived form the multiple mappings and operators among them
    private ArrayList<Mapping> mappings;
    private ArrayList<String> operators;

    // direct mappings
    private String tablename = "";
    private String connection_name = "";
    private ArrayList<String> projections_attr;
    private ArrayList<Selection> selections;

    private String source_kind;

     public Mapping (){}

    public Mapping (String tablename, String proj_attr, String connection_name){
        this.tablename = tablename;
        this.connection_name = connection_name;
        this.addProjections_attr(proj_attr);
    }      
    public Mapping (String tablename, String proj_attr, String connection_name, String source_kind){
        this.tablename = tablename;
        this.connection_name = connection_name;
        this.addProjections_attr(proj_attr);
        this.source_kind = source_kind;
    }

     public Mapping (ArrayList<String> tablenames, ArrayList<String> proj_attrs, ArrayList<String> operators, String connection_name, String source_kind){
        this.mappings = new ArrayList<Mapping>();
        this.operators = new ArrayList<String>();
        this.operators.addAll(operators);
        for (int i=0 ; i<tablenames.size() ; i++){
            this.mappings.add(new Mapping(tablenames.get(i), proj_attrs.get(i), connection_name));
        }
        this.source_kind = source_kind;
    }

    /**
     * @return the tablename
     */
    public String getTablename() {
        return tablename;
    }

    /**
     * @param tablename the tablename to set
     */
    public void setTablename(String tablename) {
        this.tablename += tablename;
     
    }

    /**
     * @return the projections_attr
     */
    public ArrayList<String> getProjections_attr() {
        return projections_attr;
    }

    /**
     * @param projections_attr the projections_attr to set
     */
    public void setProjections_attr(ArrayList<String> projections_attr) {
        this.projections_attr = projections_attr;
    }

    public void addProjections_attr(String projections_att) {
        if (projections_attr == null)
            projections_attr = new ArrayList<String>();
        projections_attr.add(projections_att);       
    }

    /**
     * @return the selections
     */
    public ArrayList<Selection> getSelections() {
        return selections;
    }

    /**
     * @param selections the selections to set
     */
    public void addSelection(Selection selection) {
        if (selections == null)
            selections = new ArrayList<Selection>();
        selections.add(selection);
    }

    /**
     * @return the operators
     */
    public ArrayList<String> getOperators() {
        return operators;
    }

    /**
     * @param operator the operator to add
     */
    public void addOperator(String operator) {
        if (this.operators == null) this.operators = new ArrayList<String>();

        this.operators.add(operator);
    }

    /**
     * @param operators the operators to set
     */
    public void setOperators(ArrayList<String> operators) {
        this.operators = operators;
    }

    /**
     * @return the mappings
     */
    public ArrayList<Mapping> getMappings() {
        return mappings;
    }

    /**
     * @param mappings the mappings to set
     */
    public void setMappings(ArrayList<Mapping> mappings) {
        this.mappings = mappings;
    }

    /**
     * @param mapping the mapping to add
     */
    public void addMapping(Mapping mapping) {
        if (this.mappings == null) this.mappings = new ArrayList<Mapping>();
        this.mappings.add(mapping);
    }

    /**
     * @return the connection_name
     */
    public String getConnection_name() {
        return connection_name;
    }

    /**
     * @param connection_name the connection_name to set
     */
    public void setConnection_name(String connection_name) {
        this.connection_name += connection_name;
    }

    /**
     * @return the source_kind
     */
    public String getSource_kind() {
        return source_kind;
    }

    /**
     * @param source_kind the source_kind to set
     */
    public void setSource_kind(String source_kind) {
        this.source_kind = source_kind;
    }


}
