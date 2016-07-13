/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.RDBIOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.extraction.xml.XMLExtractionProcess;
import org.upc.dtim.bolster.ontomatchmerge.main.OntologyConstructionCoordinator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.RDBExtractionProcess;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.XMLIOArtifact;

/**
 *
 * @author Rizkallah
 */
public abstract class OntologyExtractionCoordinator {

    public OntologyExtractionCoordinator() {
    }
    
    public static void extractOntology(IOArtifact source, Properties params) {
        try {
            ExtractionProcess ep = null;
            if (source instanceof XMLIOArtifact)
                ep = new XMLExtractionProcess();
            else if (source instanceof RDBIOArtifact)
                ep = new RDBExtractionProcess();
            
            // Run the extraction process
            ep.init(params);
            ep.extractOwl(source);
            source.setOutputOntologyUri(ep.getOntModel().getNsPrefixURI(""));
            ep.writeOntologyModel(new FileOutputStream(source.getOutputOntologyPath()), 
                                        OntologyConstructionCoordinator.ONTOLOGY_SERIALIZATION_FORMAT);

            if (source.getSourceMappingsPath() != null && !source.getSourceMappingsPath().isEmpty()) {
                ep.writeSourceMappings(new FileOutputStream(source.getSourceMappingsPath()));
            }

        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * This method adapts the extraction output and creates another version of the extracted ontology
     * that is compatible with the input expected by the matcher
     */
    public static void adaptExtractorOutput(String owlFilePath) {
        // String outputFilePath = "test\\matching\\cda_ontology_auto.owl";
        try {
            /**
             * 1. Replace non-standard characters (i.e. &lt; &gt;)
             */
            BufferedReader file = new BufferedReader(new FileReader(owlFilePath));
            String line;
            String input = "";
            
            while ((line = file.readLine()) != null) {
                input += line + '\n';
            }
            file.close();
            input = input.replace("&lt;", "_");
            input = input.replace("&gt;", "_");
            
            FileOutputStream fileOut = new FileOutputStream(owlFilePath);
            fileOut.write(input.getBytes());
            fileOut.close();
        
            /*************/
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(owlFilePath);
            
            Node rdfNode = doc.getFirstChild();
            Element rdfElem = (Element) rdfNode;

            /**
             * 2. Add <xml:base> attribute in the <rdf:RDF> element 
             */
            NamedNodeMap rdfAttrs = rdfNode.getAttributes();
            String xmlnsVal = "";
            if (rdfAttrs.getNamedItem("xml:base") == null && rdfAttrs.getNamedItem("xmlns") != null) {
                xmlnsVal = rdfAttrs.getNamedItem("xmlns").getTextContent();
                rdfElem.setAttribute("xml:base", xmlnsVal);
            }
            
            /**
             * 3. Add an empty <owl:ontology /> element as a child of the <rdf:RDF> element
             */
            Element owlElem = doc.createElement("owl:Ontology");
            owlElem.setAttribute("rdf:about", xmlnsVal);
            rdfElem.insertBefore(owlElem, rdfElem.getFirstChild());
            
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(owlFilePath));
            transformer.transform(source, result);
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
