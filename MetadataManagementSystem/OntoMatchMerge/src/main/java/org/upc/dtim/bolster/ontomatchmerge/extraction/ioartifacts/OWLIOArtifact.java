/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts;

import java.io.File;

/**
 *
 * @author Rizkallah
 */
public class OWLIOArtifact extends IOArtifact {

    private String inputOntologyPath;
    
    public OWLIOArtifact(String owlFilePath) {
        super(new File(owlFilePath).getName());
        this.inputOntologyPath = owlFilePath;
    }
    
    public OWLIOArtifact(String outputOntologyPath, String sourceMappingsPath) {
        super(outputOntologyPath, sourceMappingsPath);
        this.inputOntologyPath = "";
    }

    public String getInputOntologyPath() {
        return inputOntologyPath;
    }

    public void setInputOntologyPath(String inputOntologyPath) {
        this.inputOntologyPath = inputOntologyPath;
    }
    
    @Override
    public String toString() {
        return getSourceName() + " [OWL]: " + getOutputOntologyPath();
    }
}
