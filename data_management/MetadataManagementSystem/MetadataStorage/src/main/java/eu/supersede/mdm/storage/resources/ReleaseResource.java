package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
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
import java.io.File;
import java.io.IOException;
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
        JSONArray arr = new JSONArray();
        //getReleasesCollection(client).find().iterator().forEachRemaining(document -> allReleases.add(document.toJson()));
        getReleasesCollection(client).find().iterator().forEachRemaining(document -> arr.add(document));
        client.close();
        return Response.ok(new Gson().toJson(arr)).build();
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
     * Generation of the Source Level based on a sample dataset
     * For details see:
     *      Nadal, S., Romero, O., Abelló, A., Vassiliadis, P., Vansummeren, S.
     *      An Integration-Oriented Ontology to Govern Evolution in Big Data Ecosystems.
     *      DOLAP 2017
     */
    @POST @Path("release/")
    @Consumes("text/plain")
    public Response POST_release(String body) throws IOException {
        System.out.println("[POST /release/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient(context);

        JSONObject content = new JSONObject();
        try {
            content = eu.supersede.mdm.storage.model.bdi_ontology.Release.newRelease(objBody.getAsString("event"),objBody.getAsString("schemaVersion"),objBody.getAsString("jsonInstances"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (content.containsKey("kafkaTopic")) {
            objBody.put("kafkaTopic", content.getAsString("kafkaTopic"));
            objBody.put("releaseID", UUID.randomUUID().toString());

            // If we have to dispatch to the Data Lake, generate a random path (.txt for now)
            if (Boolean.parseBoolean(objBody.getAsString("dispatch"))) {
                // TODO replace with path to HDFS
                String dispatcherPath = "/home/snadal/Bolster/DispatcherData/"+UUID.randomUUID().toString()+".txt";
                //Files.touch(new File(dispatcherPath));
                objBody.put("dispatcherPath", dispatcherPath);
            } else {
                objBody.put("dispatcherPath", "");
            }

            getReleasesCollection(client).insertOne(Document.parse(objBody.toJSONString()));
        }

        client.close();
        return Response.ok(content.toJSONString()).build();
    }

}
