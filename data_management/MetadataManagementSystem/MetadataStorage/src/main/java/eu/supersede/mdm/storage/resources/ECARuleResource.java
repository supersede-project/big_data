package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.ActionTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.PredicatesTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.generation.BDIOntologyGenerationStrategies;
import eu.supersede.mdm.storage.model.bdi_ontology.generation.Strategy_CopyFromSources;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.Rules;
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
@Path("")
public class ECARuleResource {

    private MongoCollection<Document> getEcaRulesCollection(MongoClient client) {
        return client.getDatabase(context.getInitParameter("system_metadata_db_name")).getCollection("eca_rules");
    }

    @Context
    ServletContext context;

    @GET
    @Path("eca_rule/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_all_ECA_rules() {
        System.out.println("[GET /eca_rule/]");

        MongoClient client = Utils.getMongoDBClient(context);
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

        MongoClient client = Utils.getMongoDBClient(context);
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

        MongoClient client = Utils.getMongoDBClient(context);

        // Store in MongoDB
        objBody.put("eca_ruleID", UUID.randomUUID().toString());
        getEcaRulesCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        // Store in RDF
        Dataset dataset = Utils.getTDBDataset(this.context);
        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getNamedModel(objBody.getAsString("globalLevel"));

        String ECA_RULE_IRI = Rules.ECA_RULE.val()+objBody.getAsString("name");
        // TODO Update for multiple conditions
        String CONDITION_IRI = Rules.CONDITION.val()+objBody.getAsString("name")+"/"+"Condition1";
        String FEATURE_IRI = objBody.getAsString("feature");
        String PREDICATE_IRI = Rules.PREDICATE.val()+objBody.getAsString("predicate");
        String VALUE_IRI = Rules.VALUE.val()+objBody.getAsString("value");
        String ACTION_IRI = Rules.ACTION.val()+objBody.getAsString("action");

        // Instantiate
        RDFUtil.addTriple(model, ECA_RULE_IRI, Namespaces.rdf.val()+"type", Rules.ECA_RULE.val());
        RDFUtil.addTriple(model, CONDITION_IRI, Namespaces.rdf.val()+"type", Rules.CONDITION.val());
        RDFUtil.addTriple(model, PREDICATE_IRI, Namespaces.rdf.val()+"type", Rules.PREDICATE.val());
        RDFUtil.addTriple(model, VALUE_IRI, Namespaces.rdf.val()+"type", Rules.VALUE.val());
        RDFUtil.addTriple(model, ACTION_IRI, Namespaces.rdf.val()+"type", Rules.ACTION.val());

        // Link
        RDFUtil.addTriple(model, ECA_RULE_IRI, Rules.HAS_CONDITION.val(), CONDITION_IRI);
        RDFUtil.addTriple(model, CONDITION_IRI, Rules.HAS_FEATURE.val(), FEATURE_IRI);
        RDFUtil.addTriple(model, CONDITION_IRI, Rules.HAS_PREDICATE.val(), PREDICATE_IRI);
        RDFUtil.addTriple(model, CONDITION_IRI, Rules.HAS_VALUE.val(), VALUE_IRI);
        RDFUtil.addTriple(model, CONDITION_IRI, Rules.HAS_ACTION.val(), ACTION_IRI);

        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();

        client.close();
        return Response.ok(objBody.toJSONString()).build();
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
