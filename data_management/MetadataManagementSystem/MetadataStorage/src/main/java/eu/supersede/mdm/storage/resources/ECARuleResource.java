package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.ActionTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.OperatorTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.PredicatesTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.generation.BDIOntologyGenerationStrategies;
import eu.supersede.mdm.storage.model.bdi_ontology.generation.Strategy_CopyFromSources;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.Rules;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
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
@Path("metadataStorage")
public class ECARuleResource {

    private MongoCollection<Document> getEcaRulesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("eca_rules");
    }

    @GET
    @Path("eca_rule/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_all_ECA_rules() {
        System.out.println("[GET /eca_rule/]");

        MongoClient client = Utils.getMongoDBClient();
        JSONArray arr = new JSONArray();
        getEcaRulesCollection(client).find().iterator().forEachRemaining(document -> arr.add(document));
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
        client.close();

        return Response.ok((res.toJson())).build();
    }

    /**
     * POST a BDI Ontology
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

        // Store in RDF
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getNamedModel(objBody.getAsString("graph"));

        String ECA_RULE_IRI = Rules.RULE.val()+objBody.getAsString("ruleName");
        // TODO Update for multiple conditions
        JSONArray arrayPattern = (JSONArray) objBody.get("pattern");

        String PATTERN_IRI;
        for (Object p : arrayPattern) {
            System.out.println(p);
        }

        String CONDITION_IRI = Rules.CONDITION.val()+objBody.getAsString("condition");

        JSONArray arrayFilters = (JSONArray) objBody.get("filters");
        System.out.println(arrayFilters);
        for (Object f : arrayFilters) {
            RDFUtil.addTriple(model, f.leftOperator, Namespaces.rdf.val()+"type", Rules.OPERAND.val());
        }

        String ACTION_IRI = Rules.ACTION.val()+objBody.getAsString("action");
        String WINDOW_IRI = Rules.WINDOW.val()+objBody.getAsString("windowTime");

        // Instantiate
        RDFUtil.addTriple(model, ECA_RULE_IRI, Namespaces.rdf.val()+"type", Rules.RULE.val());
       // RDFUtil.addTriple(model, PATTERN_IRI, Namespaces.rdf.val()+"type", Rules.PATTERN.val());
        RDFUtil.addTriple(model, CONDITION_IRI, Namespaces.rdf.val()+"type", Rules.CONDITION.val());
        RDFUtil.addTriple(model, ACTION_IRI, Namespaces.rdf.val()+"type", Rules.ACTION.val());
        RDFUtil.addTriple(model, WINDOW_IRI, Namespaces.rdf.val()+"type", Rules.WINDOW.val());

        // Link
      //  RDFUtil.addTriple(model, ECA_RULE_IRI, Rules.HAS_CEP_ELEMENT.val(), PATTERN_IRI);
        RDFUtil.addTriple(model, ECA_RULE_IRI, Rules.HAS_CONDITION.val(), CONDITION_IRI);
        RDFUtil.addTriple(model, ECA_RULE_IRI, Rules.HAS_WINDOW.val(), WINDOW_IRI);

        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();

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
