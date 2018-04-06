package eu.supersede.mdm.storage.resources;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.integration.api.mdm.types.OperatorTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.ActionTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.PredicatesTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.PredicatesTypes;
import eu.supersede.mdm.storage.util.ConfigManager;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class ECARuleResource {

    private MongoCollection<Document> getEcaRulesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("eca_rules");
    }

    private MongoCollection<Document> getEventsCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("events");
    }

    @GET
    @Path("eca_rule/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_all_ECA_rules() {
        System.out.println("[GET /eca_rule/]");

        MongoClient client = Utils.getMongoDBClient();
        JSONArray arr = new JSONArray();
        getEcaRulesCollection(client).find().iterator().forEachRemaining(document -> {
            document.put("event",getEventsCollection(client).find(new Document("eventID",document.getString("eventID"))).first());
            document.remove("eventID");
            arr.add(document);
        });

        client.close();
        return Response.ok(new Gson().toJson(arr)).build();
    }

    @GET
    @Path("eca_rule/{eca_ruleID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_eca_rule(@PathParam("eca_ruleID") String eca_ruleID) {
        System.out.println("[GET /eca_rule/"+eca_ruleID+"]");

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("eca_ruleID",eca_ruleID);
        Document res = getEcaRulesCollection(client).find(query).first();
        res.put("event",getEventsCollection(client).find(new Document("eventID",res.getString("eventID"))).first());
        res.remove("eventID");

        client.close();

        return Response.ok((res.toJson())).build();
    }

    /**
     * POST an ECA RULE
     */
    @POST @Path("eca_rule/")
    @Consumes("text/plain")
    public Response POST_ECA_rule(String body) throws FileNotFoundException {
        System.out.println("[POST /eca_rule/] body = "+body);
        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient();

        // Store in MongoDB
        objBody.put("eca_ruleID", UUID.randomUUID().toString());
        getEcaRulesCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @GET
    @Path("eca_rule_operator_types")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_operator_types() {
        System.out.println("[GET /eca_rule_operator_types/]");
        JSONArray out = new JSONArray();
        for (OperatorTypes t : OperatorTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }


    @GET
    @Path("eca_rule_predicate_types")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_predicate_types() {
        System.out.println("[GET /eca_rule_predicate_types/]");
        JSONArray out = new JSONArray();
        for (PredicatesTypes t : PredicatesTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }

    @GET
    @Path("eca_rule_action_types")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_action_types() {
        System.out.println("[GET /eca_rule_action_types/]");
        JSONArray out = new JSONArray();
        for (ActionTypes t : ActionTypes.values()) {
            JSONObject inner = new JSONObject();
            inner.put("key",t.name());
            inner.put("val",t.val());
            out.add(inner);
        }
        return Response.ok(new Gson().toJson(out)).build();
    }

}
