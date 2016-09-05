/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb;

import java.util.Properties;

import org.upc.dtim.bolster.ontomatchmerge.extraction.ExtractionProcess;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.RDBIOArtifact;

/**
 *
 * @author Rizkallah
 */
public class RDBExtractionProcess extends ExtractionProcess {
    
    public RDBExtractionProcess() {
        
    }
    
    @Override
    public void init (Properties params) {
        /*this.dbName = params.getProperty("dbName");
        this.dbServer = params.getProperty("dbServer");
        this.dbUser = params.getProperty("dbUser");
        this.dbPassword = params.getProperty("dbPassword");*/
    }

    @Override
    public void extractOwl(IOArtifact source) {
        RDBIOArtifact rdbSource = (RDBIOArtifact)source;
        try {
            RDB2OWLMapper extractor = new RDB2OWLMapper(rdbSource.getDbName(), rdbSource.getDbServer(), rdbSource.getDbType(),
                                                        rdbSource.getDbUsername(), rdbSource.getDbPassword());
            extractor.convertToOWL();
            
            setOntModel(extractor.getOntology());
            setSourceMappings(extractor.getSourceMappings());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}