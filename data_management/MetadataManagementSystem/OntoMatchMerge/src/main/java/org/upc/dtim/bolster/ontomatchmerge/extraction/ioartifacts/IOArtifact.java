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
public abstract class IOArtifact {
    
    private String sourceName;
    private String outputOntologyPath;
    private String sourceMappingsPath;
    private String outputOntologyUri;

    public IOArtifact() {
        this.sourceName = "";
        this.outputOntologyPath = "";
        this.sourceMappingsPath = "";
    }
    
    public IOArtifact(String sourceName) {
        if (sourceName.lastIndexOf(".") > 0) {
            sourceName = sourceName.substring(0, sourceName.lastIndexOf("."));
        }
        this.sourceName = sourceName;
    }

    public IOArtifact(String outputOntologyPath, String sourceMappingsPath) {
        this.outputOntologyPath = outputOntologyPath;
        this.sourceMappingsPath = sourceMappingsPath;
        this.sourceName = "";
    }

    public String getOutputOntologyPath() {
        return outputOntologyPath;
    }

    public void setOutputOntologyPath(String outputOntologyPath) {
        this.outputOntologyPath = outputOntologyPath;
    }

    public String getSourceMappingsPath() {
        return sourceMappingsPath;
    }

    public void setSourceMappingsPath(String sourceMappingsPath) {
        this.sourceMappingsPath = sourceMappingsPath;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    @Override
    public String toString() {
        return sourceName;
    }

    public String getOutputOntologyUri() {
        return outputOntologyUri;
    }

    public void setOutputOntologyUri(String outputOntologyUri) {
        this.outputOntologyUri = outputOntologyUri;
    }
}
