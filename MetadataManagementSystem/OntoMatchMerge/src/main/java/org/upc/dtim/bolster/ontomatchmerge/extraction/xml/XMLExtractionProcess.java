/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.xml;

import java.io.File;
import java.util.Properties;

import com.hp.hpl.jena.ontology.OntModel;

import org.upc.dtim.bolster.ontomatchmerge.extraction.OntologyAdapter;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import tr.com.srdc.ontmalizer.XSD2OWLMapper;

import org.upc.dtim.bolster.ontomatchmerge.extraction.ExtractionProcess;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.XMLIOArtifact;

/**
 *
 * @author Rizkallah
 */

/**
 * Restriction on input to XMLExtractionProcess:
 - XSD does not contain two elements with the same name but with different types
 - All XML instances must be valid to the schema (XMLExtractionProcess does not check for validity)
 */
public class XMLExtractionProcess extends ExtractionProcess {


    public XMLExtractionProcess () {

    }
    
    @Override
    public void init (Properties params) {
        super.init(params);

        /*this.xmlSchemaPath = params.getProperty("xmlSchemaPath");
        this.xmlInstancesPaths = new ArrayList();
        int xmlInstanceCount = Integer.valueOf(params.getProperty("xmlInstanceCount"));
        for (int i = 0; i < xmlInstanceCount; i++) {
            String propName = "xmlInstancePath_" + i;
            this.xmlInstancesPaths.add(params.getProperty(propName));
        }*/
    }

    @Override
    public void extractOwl (IOArtifact source) {
        XMLIOArtifact xmlSource = (XMLIOArtifact)source;
        // Resulting ontology model
        OntModel ontModel;
        
        // 1. Convert XSD Schema to OWL
        XSD2OWLMapper mapping = new XSD2OWLMapper(new File(xmlSource.getXmlSchemaPath()));
        //mapping.setObjectPropPrefix("");
        mapping.setDataTypePropPrefix("");
        mapping.convertXSD2OWL();
        ontModel = mapping.getOntology();

        if (super.getOntologyType().equals("PHYSICAL_ONTOLOGY"))  {
            ontModel = OntologyAdapter.adaptForPhysicalOntology(ontModel);
        }
        else if (super.getOntologyType().equals("LOGICAL_ONTOLOGY"))  {
            ontModel = OntologyAdapter.adaptForLogicalOntology(ontModel);
        }

        /**
         * Sergi: removed this for TBox extraction only
         */
        // 2. Convert XML Instances to OWL/RDF
        /*for (String xmlInstancePath : xmlSource.getXmlInstancesPath()) {
            XML2OWLMapper generator = new XML2OWLMapper(new File(xmlInstancePath), mapping);
            generator.convertXML2OWL();
        
            ontModel.add(generator.getModel());
            setSourceMappings(generator.getSourceMappings());
        }*/
        
        setOntModel(ontModel);
    }
}
