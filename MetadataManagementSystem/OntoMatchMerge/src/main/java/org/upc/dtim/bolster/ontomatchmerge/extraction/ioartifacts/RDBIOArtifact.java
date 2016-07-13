/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts;

/**
 *
 * @author Rizkallah
 */
public class RDBIOArtifact extends IOArtifact {
    private String dbName;
    private String dbServer;
    private String dbType;
    private String dbUsername;
    private String dbPassword;
    
    public RDBIOArtifact(String dbName, String dbServer, String dbType, String dbUsername, String dbPassword) {
        super(dbName);
        this.dbName = dbName;
        this.dbServer = dbServer;
        this.dbType = dbType;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }
    
    public RDBIOArtifact(String dbName, String dbServer, String dbType, String dbUsername, String dbPassword,
                                        String outputOntologyPath, String sourceMappingsPath) {
        super(outputOntologyPath, sourceMappingsPath);
        this.dbName = dbName;
        this.dbServer = dbServer;
        this.dbType = dbType;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbServer() {
        return dbServer;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }
    
    @Override
    public String toString() {
        return getSourceName() + " [RDB " + dbType +"]: " 
                + dbName + "@" + dbServer 
                + ":" + dbUsername + "/" + dbPassword;
    }
}
