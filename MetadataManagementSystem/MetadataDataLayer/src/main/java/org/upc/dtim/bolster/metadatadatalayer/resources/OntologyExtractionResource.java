package org.upc.dtim.bolster.metadatadatalayer.resources;

import com.google.common.io.Files;
import org.upc.dtim.bolster.metadatadatalayer.util.HTTPRequests;
import org.upc.dtim.bolster.metadatadatalayer.util.Properties;
import org.upc.dtim.bolster.metadatadatalayer.util.PropertiesEnum;

import javax.ws.rs.*;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by snadal on 7/06/16.
 */
@Path("metadataDataLayer")
public class OntologyExtractionResource {

    @POST @Path("ontologyExtraction/fromDatasetToPhysical/{datasetID}/{username}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_fromDatasetToPhysicalOntology(@PathParam("datasetID") String datasetID, @PathParam("username") String username) {
        System.out.println("[POST /ontologyExtraction/fromDatasetToPhysical/"+datasetID+"/"+username+"]");

        if (!UserResource.userExists(username)) {
            return Response.status(404).entity("User "+username+" not found").build();
        }

        String URL = "/datasets/"+datasetID+"/"+username;
        WebTarget target = HTTPRequests.request(Properties.getProperty(PropertiesEnum.METADATA_DATA_LAYER_URL.getValue())+URL);
        String response = target.request().get(String.class);

        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
            Files.write(response.getBytes(),tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }

        System.out.println("a");

        return Response.ok().build();
    }

}
