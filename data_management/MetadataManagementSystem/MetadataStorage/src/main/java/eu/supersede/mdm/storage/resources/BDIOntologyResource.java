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
public class BDIOntologyResource {

    private MongoCollection<Document> getReleasesCollection(MongoClient client) {
        return client.getDatabase(context.getInitParameter("system_metadata_db_name")).getCollection("releases");
    }

    private MongoCollection<Document> getBDIOntologyCollection(MongoClient client) {
        return client.getDatabase(context.getInitParameter("system_metadata_db_name")).getCollection("bdi_ontologies");
    }

    @Context
    ServletContext context;

    @GET
    @Path("bdi_ontology/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_BDI_ontology() {
        System.out.println("[GET /bdi_ontology/]");

        MongoClient client = Utils.getMongoDBClient(context);
        // Non-complete ontologies means the ones where the release data is not populated
        List<String> nonCompleteOntologies = Lists.newArrayList();
        List<String> completeOntologies = Lists.newArrayList();
        getBDIOntologyCollection(client).find().iterator().forEachRemaining(document -> nonCompleteOntologies.add(document.toJson()));

        nonCompleteOntologies.forEach(strOntology -> {
            JSONObject objOntology = (JSONObject) JSONValue.parse(strOntology);
            JSONArray arrayReleases = (JSONArray) objOntology.get("releases");

            JSONArray releases = new JSONArray();
            arrayReleases.forEach(releaseID -> {
                Document query = new Document("releaseID",releaseID);
                Document res = getReleasesCollection(client).find(query).first();
                releases.add((JSONObject)JSONValue.parse(res.toJson()));
            });

            objOntology.put("releasesData",releases);
            completeOntologies.add(objOntology.toJSONString());
        });

        client.close();
        return Response.ok(new Gson().toJson(completeOntologies)).build();
    }

    @GET
    @Path("bdi_ontology/{bdi_ontologyID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_BDI_ontology(@PathParam("bdi_ontologyID") String bdi_ontologyID) {
        System.out.println("[GET /bdi_ontology/] bdi_ontologyID = "+bdi_ontologyID);

        MongoClient client = Utils.getMongoDBClient(context);
        Document query = new Document("bdi_ontologyID",bdi_ontologyID);
        Document res = getBDIOntologyCollection(client).find(query).first();
        client.close();

        return Response.ok((res.toJson())).build();
    }


    /**
     * POST a BDI Ontology
     */
    @POST @Path("bdi_ontology/")
    @Consumes("text/plain")
    public Response POST_BDI_ontology(String body) {
        System.out.println("[POST /release/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient(context);

        objBody.put("bdi_ontologyID", UUID.randomUUID().toString());
        getBDIOntologyCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

}
