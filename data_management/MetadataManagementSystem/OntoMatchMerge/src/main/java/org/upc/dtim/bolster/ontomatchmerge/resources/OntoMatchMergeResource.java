package org.upc.dtim.bolster.ontomatchmerge.resources;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import com.hp.hpl.jena.ontology.OntModel;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.json.XML;
import org.upc.dtim.bolster.ontomatchmerge.extraction.OntologyExtractionCoordinator;
import org.upc.dtim.bolster.ontomatchmerge.extraction.OntologyAdapter;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.XMLIOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.util.OntologyFile;
import org.upc.dtim.bolster.ontomatchmerge.util.TempFiles;
import org.wiztools.xsdgen.ParseException;
import org.wiztools.xsdgen.XsdGen;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Properties;

/**
 * Created by snadal on 8/06/16.
 */
@Path("ontoMatchMerge")
public class OntoMatchMergeResource {
    /**
     * Sergi:
     * Rizkallah's tool works with files, to avoid changing the IO code lets store the parameters on temp files
     */


    @POST @Path("extraction/XML")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_ontology_extraction_xml(String JSON_dataset_XML) {

        JSONObject dataset = (JSONObject)JSONValue.parse(JSON_dataset_XML);

        String xsdPath = TempFiles.storeInTempFile((String)dataset.get("xmlSchema"));
        String xmlPath = TempFiles.storeInTempFile((String)dataset.get("xmlInstances"));

        XMLIOArtifact xmlSource = new XMLIOArtifact(xsdPath);
        xmlSource.addXmlInstancesPath(xmlPath);
        xmlSource.setOutputOntologyPath(TempFiles.storeInTempFile(""));

        OntologyExtractionCoordinator.extractOntology(xmlSource, new Properties());

        String content = "";
        try {
            content = new String(Files.readAllBytes(new File(xmlSource.getOutputOntologyPath()).toPath()),"UTF-8");
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return Response.ok(content).build();
    }

    @POST @Path("extraction/JSON/{ontologyType}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_ontology_extraction_json(@PathParam("ontologyType") String ontologyType, String JSON_dataset_JSON) {
        JSONObject dataset = (JSONObject)JSONValue.parse(JSON_dataset_JSON);
        org.json.JSONObject theJSONDataset = new org.json.JSONObject();
        theJSONDataset.put(((String)dataset.get("name")).replace(" ",""),new org.json.JSONObject((String)dataset.get("jsonInstances")));
        String xmlPath = TempFiles.storeInTempFile(XML.toString(theJSONDataset));
        String xsdPath = TempFiles.storeInTempFile("");
        XsdGen gen = new XsdGen();
        try {
            gen.parse(new File(xmlPath));
            gen.write(new FileOutputStream(new File(xsdPath)));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        XMLIOArtifact xmlSource = new XMLIOArtifact(xsdPath);
        xmlSource.addXmlInstancesPath(xmlPath);
        xmlSource.setOutputOntologyPath(TempFiles.storeInTempFile(""));

        Properties properties = new Properties();
        properties.put("ontologyType",ontologyType);

        OntologyExtractionCoordinator.extractOntology(xmlSource, properties);


        // Adapt the extractor output to the input required by the matcher
        OntologyExtractionCoordinator.adaptExtractorOutput(xmlSource.getOutputOntologyPath());

        String content = "";
        try {
            content = new String(Files.readAllBytes(new File(xmlSource.getOutputOntologyPath()).toPath()),"UTF-8");
        } catch (IOException exc) {
            exc.printStackTrace();
        }

        return Response.ok(content).build();
    }

}
