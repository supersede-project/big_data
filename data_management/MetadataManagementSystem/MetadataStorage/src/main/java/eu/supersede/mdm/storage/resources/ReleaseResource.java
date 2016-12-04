package eu.supersede.mdm.storage.resources;

import com.mongodb.MongoClient;
import eu.supersede.mdm.storage.bdi_ontology.Release;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;

/**
 * Created by snadal on 22/11/16.
 */
@Path("")
public class ReleaseResource {

    @Context
    ServletContext context;

    /**
     * Automatic construction of the ontology for SUPERSEDE
     */
    @POST @Path("release/")
    @Consumes("text/plain")
    public Response POST_release(String body) {
        System.out.println("[POST /release/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient(context);

        String content = "";
        try {
            content = Release.newRelease(objBody.getAsString("event"),objBody.getAsString("schemaVersion"),objBody.getAsString("jsonInstances"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //getArtifactsCollection(client).insertOne(Document.parse(JSON_artifact));
        client.close();

        return Response.ok(content).build();
    }

}
