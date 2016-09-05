/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb;

import com.hp.hpl.jena.ontology.OntModel;

import java.sql.Connection;
import java.sql.SQLException;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;

/**
 *
 * @author Rizkallah
 */
public abstract class AbstractDatabase {
    
    private String name;
    private String type;
    private String server;
    private String username;
    private String password;
    private Connection conn;

    public AbstractDatabase() {
        this.name = "";
        this.type = "";
        this.server = "";
        this.username = "";
        this.password = "";
    }
    
    public AbstractDatabase(String name, String type, String server, String username, String password) {
        this.name = name;
        this.type = type;
        this.server = server;
        this.username = username;
        this.password = password;
    }

    public abstract void getDatabaseConnection();
    public abstract void loadDatabaseSchema() throws SQLException;
    public abstract OntModel convertSchemaToOWL(OntModel ontModel);
    public abstract OntModel convertInstancesToOWL(OntModel ontModel) throws SQLException;
    public abstract SourceMappingStruct generateSourceMapping(SourceMappingStruct sourceMappings);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getServer() {
        return server;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Connection getConn() {
        return conn;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }
}
