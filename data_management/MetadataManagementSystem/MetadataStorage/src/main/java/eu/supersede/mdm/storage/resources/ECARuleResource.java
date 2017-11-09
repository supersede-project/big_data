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
import org.apache.jena.base.Sys;
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

        String RULE_IRI = Namespaces.ex.val()+objBody.getAsString("ruleName");
        String CONDITION_IRI = RULE_IRI+"/"+objBody.getAsString("condition");
        String WINDOW_TIME_IRI = RULE_IRI+"/"+objBody.getAsString("windowTime");
        String WINDOW_SIZE_IRI = RULE_IRI+"/"+objBody.getAsString("windowSize");
        String PATTERN_IRI = RULE_IRI+"/Pattern";


        // Instantiate
        RDFUtil.addTriple(model, RULE_IRI, Namespaces.rdf.val()+"type", Rules.RULE.val());
        RDFUtil.addTriple(model, PATTERN_IRI, Namespaces.rdf.val()+"type", Rules.PATTERN.val());
        RDFUtil.addTriple(model, CONDITION_IRI, Namespaces.rdf.val()+"type", Rules.CONDITION.val());
        RDFUtil.addTriple(model, WINDOW_SIZE_IRI, Namespaces.rdf.val()+"type", Rules.WINDOW.val());

        // Link
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_CEP_ELEMENT.val(), PATTERN_IRI);
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_CONDITION.val(), CONDITION_IRI);
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_WINDOW.val(), WINDOW_SIZE_IRI);

        // PATTERN RELEASES
        System.out.println("----PATTERN----");
        JSONArray arrayPattern = (JSONArray) objBody.get("pattern");
        int patternElementOrder = 1;
        for (Object obj : arrayPattern) {
            // Instantiate
            RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Namespaces.rdf.val()+"type", Rules.INCLUDED_ELEMENT.val());
            //   RDFUtil.addTriple(model, Integer.toString(patternElementOrder), Namespaces.rdf.val()+"type", Rules.POSITIVE_INTEGER.val());
            // Link
            RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Rules.HAS_ELEMENT_ORDER.val(), Integer.toString(patternElementOrder));
            RDFUtil.addTriple(model, PATTERN_IRI, Rules.CONTAINS_ELEMENT.val(), PATTERN_IRI+"/"+obj.toString());
            ++patternElementOrder;
        }

        // FILTERS
        JSONArray arrayFilters = (JSONArray) objBody.get("filters");
        for (Object f : arrayFilters) {
            JSONObject obj = (JSONObject) f;
            System.out.println("----FILTER " + obj.getAsString("name") + "----");
            String FILTER_IRI = RULE_IRI+"/Filter"+obj.getAsString("name");
            //  RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_FILTER.val(), "Filter"+filterNum);
            // Instantiate
            RDFUtil.addTriple(model, FILTER_IRI+"/", Namespaces.rdf.val()+"type", Rules.SIMPLE_CLAUSE.val());
            RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("leftOperand"), Namespaces.rdf.val()+"type", Rules.USED_ATTRIBUTE.val());
          //  RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("event"), Namespaces.rdf.val()+"type", Rules.EVENT.val());
            // Link
           // RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("leftOperand"), Rules.FOR_EVENT.val(), FILTER_IRI+"/"+obj.getAsString("event"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_RIGHT_OPERAND.val(), FILTER_IRI+"/"+obj.getAsString("rightOperand"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_LEFT_OPERAND.val(), FILTER_IRI+"/"+obj.getAsString("leftOperand"));
            RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_FILTER.val(), FILTER_IRI);
        }

        // ACTION
        System.out.println("----ACTION----");
        JSONObject action = (JSONObject) objBody.get("action");
        String ACTION_IRI = RULE_IRI+"/"+action.getAsString("name");
        RDFUtil.addTriple(model, ACTION_IRI, Namespaces.rdf.val()+"type", Rules.ACTION.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_ACTION.val(), ACTION_IRI);
        JSONArray parameters = (JSONArray) action.get("parameters");
        int parametersOrder = 1;
        for (Object p : parameters) {
            // Instantiate
            RDFUtil.addTriple(model, ACTION_IRI+"/"+p.toString(), Namespaces.rdf.val()+"type", Rules.ACTION_PARAMETER.val());
            // Link
            RDFUtil.addTriple(model, ACTION_IRI+"/"+p.toString(), Rules.HAS_ACTION_PARAMETER_ORDER.val(), Integer.toString(parametersOrder));
            RDFUtil.addTriple(model, ACTION_IRI, Rules.HAS_ACTION_PARAMETER.val(), ACTION_IRI+"/"+p.toString());
            ++parametersOrder;
        }

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
