/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Rizkallah
 */
public class XMLIOArtifact extends IOArtifact {
    private String xmlSchemaPath;
    private ArrayList<String> xmlInstancesPath;
    
    public XMLIOArtifact(String xmlSchemaPath) {
        super(new File(xmlSchemaPath).getName());
        this.xmlSchemaPath = xmlSchemaPath;
        this.xmlInstancesPath = new ArrayList();
    }
    
    public XMLIOArtifact(String xmlSchemaPath, String outputOntologyPath, String sourceMappingsPath) {
        super(outputOntologyPath, sourceMappingsPath);
        this.xmlSchemaPath = xmlSchemaPath;
        this.xmlInstancesPath = new ArrayList();
    }

    public String getXmlSchemaPath() {
        return xmlSchemaPath;
    }

    public void setXmlSchemaPath(String xmlSchemaPath) {
        this.xmlSchemaPath = xmlSchemaPath;
    }

    public ArrayList<String> getXmlInstancesPath() {
        return xmlInstancesPath;
    }

    public void addXmlInstancesPath(String xmlInstancePath) {
        this.xmlInstancesPath.add(xmlInstancePath);
    }
    
    @Override
    public String toString() {
        return getSourceName() + " [XML]: " + xmlSchemaPath;
    }
}
