/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.mysql;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMapping;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.RDBNamingUtility;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;

import java.sql.Connection;
import java.sql.SQLException;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Mapping;

/**
 *
 * @author Rizkallah
 */
public class MySQLColumn {
 
    private String name;
    private String type;
    private MySQLTable table;
    
    public MySQLColumn () {
        this.name = "";
        this.type = "";
        this.table = null;
    }
    
    public MySQLColumn(String name, String type, MySQLTable tableName) {
        this.name = name;
        this.type = type;
        this.table = tableName;
    }

    
    public void loadDatabaseSchema(Connection conn) throws SQLException {
        
    }
    
    
    public OntModel convertSchemaToOWL(OntModel ontModel) {
        DatatypeProperty colProp = ontModel.createDatatypeProperty(RDBNamingUtility.getDTPropURI(table.getDatabase().getName(), name));
        colProp.setDomain(ontModel.createResource(RDBNamingUtility.getClassURI(table.getDatabase().getName(), table.getName())));
        colProp.setRange(ontModel.createResource(RDBNamingUtility.getXsdDataType(type).getURI()));
        
        return ontModel;
    }
    
    
    public OntModel convertInstancesToOWL(Connection conn, OntModel ontModel) throws SQLException {
        return ontModel;
    }
    
    
    public SourceMappingStruct generateSourceMapping(SourceMappingStruct sourceMappings) {
        Mapping mapping = new Mapping();
        mapping.setSource_kind("relational");
        mapping.setConnection_name(table.getDatabase().getName());
        mapping.setTablename(table.getName());

        if (table.getPrimaryKeyColumn(name) == null)
            mapping.addProjections_attr(name);
        for (MySQLColumn pkCol : table.getPrimaryKeyColumns()) {
            mapping.addProjections_attr(pkCol.getName());
        }

        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.setOntology_id(RDBNamingUtility.getDTPropURI(table.getDatabase().getName(), name));
        sourceMapping.setOntology_type("property");
        sourceMapping.setMapping(mapping);
        sourceMappings.addS_map(sourceMapping);
        return sourceMappings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MySQLTable getTable() {
        return table;
    }

}
