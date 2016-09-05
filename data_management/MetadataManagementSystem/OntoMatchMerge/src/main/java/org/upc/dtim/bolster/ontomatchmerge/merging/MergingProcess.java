/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.merging;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import static com.hp.hpl.jena.vocabulary.OWL.NS;

import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Connection;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMapping;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.io.SourceMappingXMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.io.SourceMappingXMLReader;

/**
 *
 * @author Rizkallah
 */
public class MergingProcess {

    private URI onto1Uri;
    private URI onto2Uri;
    private String onto1SourceMappingsPath;
    private String onto2SourceMappingsPath;

    private OntModel mergedOntModel;
    private SourceMappingStruct sourceMappings;
    
    public MergingProcess () {
        
    }
    
    public MergingProcess(URI onto1Uri, URI onto2Uri, String onto1SourceMappingsPath, String onto2SourceMappingsPath) {
        this.onto1Uri = onto1Uri;
        this.onto2Uri = onto2Uri;
        this.onto1SourceMappingsPath = onto1SourceMappingsPath;
        this.onto2SourceMappingsPath = onto2SourceMappingsPath;
    }

    public void mergeOntologiesForQuarry(String alignmentsPath) {
        try {
            // 1. Get Base URI of both ontologies
            OntModel onto1Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            onto1Model.read(FileManager.get().open(this.onto1Uri.toString()), NS);
            String baseUri1 = onto1Model.getNsPrefixURI("");

            OntModel onto2Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            onto2Model.read(FileManager.get().open(this.onto2Uri.toString()), NS);
            String baseUri2 = onto2Model.getNsPrefixURI("");
            onto2Model.close();
            
            // 2. Read second ontlogy and its source mappings as file
            BufferedReader file = new BufferedReader(new FileReader(new File(this.onto2Uri)));
            String line;
            String owlInput = "";
            while ((line = file.readLine()) != null) {
                owlInput += line + '\n';
            }   
            file.close();
            
            file = new BufferedReader(new FileReader(new File(this.onto2SourceMappingsPath)));
            String mappingsInput = "";
            while ((line = file.readLine()) != null) {
                mappingsInput += line + '\n';
            }   
            file.close();
            
            // 3. Iterate through alignments and replace class names
            AlignmentParser parser = new AlignmentParser();
            Alignment alignment = parser.parse(new File(alignmentsPath).toURI());
            Enumeration<Cell> alignCells = alignment.getElements();
            while (alignCells.hasMoreElements()) {
                Cell cell = alignCells.nextElement();
                String obj1Uri = cell.getObject1AsURI().toString();
                String obj2Uri = cell.getObject2AsURI().toString();
                if (obj1Uri.equals("http://www.tpc.org/tpc-di#C_ID"))
                    System.out.println("C_ID");
                owlInput = owlInput.replace(obj2Uri, obj1Uri);
                owlInput = owlInput.replace(String.valueOf(Integer.MAX_VALUE), "-1");
                mappingsInput = mappingsInput.replace(obj2Uri, obj1Uri);
            }
            owlInput = owlInput.replace(baseUri2, baseUri1);
            mappingsInput = mappingsInput.replace(baseUri2, baseUri1);

            // 4. Read second ontology again and merge the two ontologies
            onto2Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            onto2Model.read(new ByteArrayInputStream(owlInput.getBytes()), NS);

            mergedOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            mergedOntModel.add(onto1Model);
            mergedOntModel.add(onto2Model);
            mergedOntModel.setNsPrefix("", baseUri1);
            
            // 5. Read both mappings and merge them
            SourceMappingXMLReader sourceMappingsReader = new SourceMappingXMLReader(this.onto1SourceMappingsPath);
            sourceMappingsReader.readXML();
            sourceMappings = sourceMappingsReader.getXml_sm_input();
            
            sourceMappingsReader = new SourceMappingXMLReader(mappingsInput.getBytes());
            sourceMappingsReader.readXML();
            SourceMappingStruct sourceMappings2 = sourceMappingsReader.getXml_sm_input();
            
            if (sourceMappings2.getConnections() != null) {
                for (Connection conn : sourceMappings2.getConnections()) {
                    sourceMappings.addConnection(conn);
                }
            }
            for (SourceMapping sm : sourceMappings2.getS_map()) {
                sourceMappings.addS_map(sm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void mergeOntologies(AlignmentProcess ap) {    
        try {
            // 1. Read ontolgoy models and merge them into one model
            OntModel onto1Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            onto1Model.read(FileManager.get().open(this.onto1Uri.toString()), NS);

            OntModel onto2Model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            onto2Model.read(FileManager.get().open(this.onto2Uri.toString()), NS);

            OntModel mergedModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            mergedModel.add(onto1Model);
            mergedModel.add(onto2Model);
            
            // 2. Read merged ontology as XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            ByteArrayOutputStream mergedOntoByteArray = new ByteArrayOutputStream();
            mergedModel.write(mergedOntoByteArray);
            Document mergedOntoXml = dBuilder.parse(new ByteArrayInputStream(mergedOntoByteArray.toByteArray()));
            mergedOntoXml.getDocumentElement().normalize();
            
            // 3. Read alignment cells as XML
            ByteArrayOutputStream alignCellsByteArray = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(alignCellsByteArray));
            AlignmentVisitor visitor = new OWLAxiomsRendererVisitor(writer);
            ap.render(visitor);
            writer.flush();
            writer.close();
            Document alignCellsXml = dBuilder.parse(new ByteArrayInputStream(alignCellsByteArray.toByteArray()));
            alignCellsXml.getDocumentElement().normalize();
            
            // 4. Iterate through alignment cells
            Element alignCellsRoot = alignCellsXml.getDocumentElement();
            NodeList alignCells = alignCellsRoot.getChildNodes();
            for (int i = 0; i < alignCells.getLength(); i++) {
                // Get resource name from alignment cell
                Node childNode = alignCells.item(i);
                if (childNode.getAttributes() == null 
                        || childNode.getAttributes().getNamedItem("rdf:about") == null 
                            || childNode.getAttributes().getNamedItem("rdf:about").getNodeValue().equals(""))
                    continue;

                String ontoResourceUri = childNode.getAttributes().getNamedItem("rdf:about").getNodeValue();
                
                // Get node to append from alignment cell
                Node alignNodeToImport = childNode.getChildNodes().item(1);
                
                // Get corresponding resource from OWL ontology
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                String xpathQ = "/*/*[@about=\"" + ontoResourceUri + "\"]";
                XPathExpression expr = xpath.compile(xpathQ);
                NodeList nl = (NodeList) expr.evaluate(mergedOntoXml, XPathConstants.NODESET);
                if (nl.getLength() < 1)
                    continue;
                
                // Add child node of alignment cell to OWL ontology
                Node owlResourceNode = nl.item(0);
                Node owlNodeToAppend = mergedOntoXml.importNode(alignNodeToImport, true);
                if (owlResourceNode != null && owlNodeToAppend != null) {
                    owlResourceNode.appendChild(owlNodeToAppend);
                }
            }
            
            // 5. Write resulting model to the merged model
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            ByteArrayOutputStream mergedOntoOut = new ByteArrayOutputStream();
            DOMSource source = new DOMSource(mergedOntoXml);
            StreamResult result = new StreamResult(mergedOntoOut);
            transformer.transform(source, result);
            
            this.mergedOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            this.mergedOntModel.read(new ByteArrayInputStream(mergedOntoOut.toByteArray()), NS);

            // 6. Read both mappings and merge them
            SourceMappingXMLReader sourceMappingsReader = new SourceMappingXMLReader(this.onto1SourceMappingsPath);
            sourceMappingsReader.readXML();
            sourceMappings = sourceMappingsReader.getXml_sm_input();
            
            sourceMappingsReader = new SourceMappingXMLReader(this.onto2SourceMappingsPath);
            sourceMappingsReader.readXML();
            SourceMappingStruct sourceMappings2 = sourceMappingsReader.getXml_sm_input();
            
            if (sourceMappings2.getConnections() != null) {
                for (Connection conn : sourceMappings2.getConnections()) {
                    sourceMappings.addConnection(conn);
                }
            }
            for (SourceMapping sm : sourceMappings2.getS_map()) {
                sourceMappings.addS_map(sm);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }   
    }
    
    public void writeMergedOntologyModel(OutputStream os, String format) throws IOException {
        mergedOntModel.write(os, format);
        os.close();
    }

    public void writeSourceMappings(OutputStream os) throws Exception {
        SourceMappingXMLWriter writer = new SourceMappingXMLWriter(this.sourceMappings, os);
        writer.writeXML();
        os.close();
    }
}
