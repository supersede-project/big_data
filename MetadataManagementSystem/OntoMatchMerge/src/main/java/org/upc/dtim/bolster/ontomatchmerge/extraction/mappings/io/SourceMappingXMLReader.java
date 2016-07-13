package org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.io;


import java.io.ByteArrayInputStream;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * The class for preparing and invoking SAX parser for reading an input XML file with source mapping
 * @author Petar
 *
 */
public class SourceMappingXMLReader {
    
    private SourceMappingStruct xml_sm_input;
    
    /**
     * @return the xml_sm_input
     */
    public SourceMappingStruct getXml_sm_input() {
        return xml_sm_input;
    }

    /**
     * @param xml_sm_input the xml_sm_input to set
     */
    public void setXml_sm_input(SourceMappingStruct xml_sm_input) {
        this.xml_sm_input = xml_sm_input;
    }
    
    private String path;

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param filename the filename to set
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    private InputStream inputStream;
 
    public SourceMappingXMLReader(String path){
        try {
            xml_sm_input = new SourceMappingStruct();
            this.path = path;
            this.inputStream = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public SourceMappingXMLReader(byte[] inputByteArray) {
        xml_sm_input = new SourceMappingStruct();
        this.path = "";
        this.inputStream = new ByteArrayInputStream(inputByteArray);
    }

    /**
     * Reading XML source mappings from local file system. 
     * @throws ParserConfigurationException
     * @throws FactoryConfigurationError
     * @throws Exception 
     */
    public void readXML()throws ParserConfigurationException, FactoryConfigurationError, Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        factory.setNamespaceAware(false);
        SourceMappingXMLHandler sm_handler = new SourceMappingXMLHandler();
        sm_handler.setXml_sm_input(getXml_sm_input());

        SAXParser parser = factory.newSAXParser();
        parser.parse(this.inputStream, sm_handler);
    }
}

