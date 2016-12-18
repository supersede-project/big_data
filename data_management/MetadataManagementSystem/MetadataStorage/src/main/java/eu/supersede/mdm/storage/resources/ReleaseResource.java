package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.bdi_ontology.Release;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("")
public class ReleaseResource {

    private MongoCollection<Document> getReleasesCollection(MongoClient client) {
        return client.getDatabase(context.getInitParameter("system_metadata_db_name")).getCollection("releases");
    }

    @Context
    ServletContext context;

    @GET
    @Path("release/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_release() {
        System.out.println("[GET /release/]");

        MongoClient client = Utils.getMongoDBClient(context);
        List<String> allReleases = Lists.newArrayList();
        getReleasesCollection(client).find().iterator().forEachRemaining(document -> allReleases.add(document.toJson()));
        client.close();
        return Response.ok(new Gson().toJson(allReleases)).build();
    }

    @GET
    @Path("release/{releaseID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_release(@PathParam("releaseID") String releaseID) {
        System.out.println("[GET /release/]");

        MongoClient client = Utils.getMongoDBClient(context);
        Document query = new Document("releaseID",releaseID);
        Document res = getReleasesCollection(client).find(query).first();
        client.close();

        return Response.ok((res.toJson())).build();
    }


    /**
     * Automatic construction of the ontology for SUPERSEDE
     */
    @POST @Path("release/")
    @Consumes("text/plain")
    public Response POST_release(String body) {
        System.out.println("[POST /release/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient(context);

        JSONObject content = new JSONObject();
        try {
            content = Release.newRelease(objBody.getAsString("event"),objBody.getAsString("schemaVersion"),objBody.getAsString("jsonInstances"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (content.containsKey("kafkaTopic")) {
            objBody.put("kafkaTopic", content.getAsString("kafkaTopic"));
            objBody.put("releaseID", UUID.randomUUID().toString());
            getReleasesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        }

        client.close();
        return Response.ok(content.toJSONString()).build();
    }

}
