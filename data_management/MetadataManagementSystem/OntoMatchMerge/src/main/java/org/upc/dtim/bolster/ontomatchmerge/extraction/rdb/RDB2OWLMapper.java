/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.mysql.MySQLDatabase;

/**
 *
 * @author Rizkallah
 */
public class RDB2OWLMapper { 
    
    private OntModel ontology = null;
    
    // Database connection and query execution
    private String sqlStmt = "";
    private PreparedStatement preparedStmt = null;
    private ResultSet rs = null;

    private AbstractDatabase database;
    private SourceMappingStruct sourceMappings;

    public RDB2OWLMapper(String dbName, String dbServer, String dbType, String dbUser, String dbPassword) {
        try {
            // The type of the database should depend on the "dbType" parameter
            database = new MySQLDatabase(dbName, dbType, dbServer, dbUser, dbPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize ontology
        ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        ontology.setNsPrefix("", RDBNamingUtility.ONTOLOGY_BASE_URI + "/" + dbName + "#");
        
        // Initalize source mappings
        sourceMappings = new SourceMappingStruct();
    }
    
    public void convertToOWL() {
        try {
            database.loadDatabaseSchema();
            ontology = database.convertSchemaToOWL(ontology);
            ontology = database.convertInstancesToOWL(ontology);
            sourceMappings = database.generateSourceMapping(sourceMappings);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public OntModel getOntology() {
        return ontology;
    }

    public SourceMappingStruct getSourceMappings() {
        return this.sourceMappings;
    }
}