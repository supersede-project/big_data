package eu.supersede.mdm.storage.resources;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class CERRuleResource {

    private MongoCollection<Document> getCerRulesCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("cer_rules");
    }
    private MongoCollection<Document> getEventsCollection(MongoClient client) {
        return client.getDatabase(ConfigManager.getProperty("system_metadata_db_name")).getCollection("events");
    }

    @GET
    @Path("cer_rule/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_all_CER_rules() {
        System.out.println("[GET /cer_rule/]");

        MongoClient client = Utils.getMongoDBClient();
        JSONArray arr = new JSONArray();
        getCerRulesCollection(client).find().iterator().forEachRemaining(document -> {
            JSONArray pattern = new JSONArray();
            ((JSONArray)(JSONValue.parse(document.get("pattern").toString()))).forEach(event -> {
                pattern.add(getEventsCollection(client).find(new Document("eventID",event)).first());
            });
            document.remove("pattern");
            document.put("pattern",pattern);

            JSONArray filters = new JSONArray();
            ((List)document.get("filters")).forEach(strFilter -> {
                Document filter = (Document)(strFilter);
                String theEvent = filter.getString("event");
                filter.remove("event");
                filter.put("event",getEventsCollection(client).find(new Document("eventID",theEvent)).first());
                filters.add(filter);
            });
            document.remove("filters");
            document.put("filters",filters);

            JSONArray actionParameters = new JSONArray();
            ((List)document.get("actionParameters")).forEach(strActionParameter -> {
                Document actionParameter = (Document)(strActionParameter);
                String theEvent = actionParameter.getString("event");
                actionParameter.remove("event");
                actionParameter.put("event",getEventsCollection(client).find(new Document("eventID",theEvent)).first());
                actionParameters.add(actionParameter);
            });
            document.remove("actionParameters");
            document.put("actionParameters",actionParameters);

            arr.add(document);
        });
        client.close();
        return Response.ok(new Gson().toJson(arr)).build();
    }

    @GET
    @Path("cer_rule/{cer_ruleID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_CER_rule(@PathParam("cer_ruleID") String cer_ruleID) {
        System.out.println("[GET /cer_rule/"+cer_ruleID+"]");

        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("cer_ruleID",cer_ruleID);
        Document res = getCerRulesCollection(client).find(query).first();

        JSONArray pattern = new JSONArray();
        ((JSONArray)(JSONValue.parse(res.get("pattern").toString()))).forEach(event -> {
            pattern.add(getEventsCollection(client).find(new Document("eventID",event)).first());
        });
        res.remove("pattern");
        res.put("pattern",pattern);

        JSONArray filters = new JSONArray();
        ((List)res.get("filters")).forEach(strFilter -> {
            Document filter = (Document)(strFilter);
            String theEvent = filter.getString("event");
            filter.remove("event");
            filter.put("event",getEventsCollection(client).find(new Document("eventID",theEvent)).first());
            filters.add(filter);
        });
        res.remove("filters");
        res.put("filters",filters);

        JSONArray actionParameters = new JSONArray();
        ((List)res.get("actionParameters")).forEach(strActionParameter -> {
            Document actionParameter = (Document)(strActionParameter);
            String theEvent = actionParameter.getString("event");
            actionParameter.remove("event");
            actionParameter.put("event",getEventsCollection(client).find(new Document("eventID",theEvent)).first());
            actionParameters.add(actionParameter);
        });
        res.remove("actionParameters");
        res.put("actionParameters",actionParameters);

        client.close();

        return Response.ok((res.toJson())).build();
    }

    /**
     * POST a CER RULE
     */
    @POST @Path("cer_rule/")
    @Consumes("text/plain")
    public Response POST_CER_rule(String body) throws FileNotFoundException {
        System.out.println("[POST /cer_rule/] body = "+body);

        JSONObject objBody = (JSONObject) JSONValue.parse(body);

        MongoClient client = Utils.getMongoDBClient();

        /** Store it in MongoDB **/
        objBody.put("cer_ruleID", UUID.randomUUID().toString());
        getCerRulesCollection(client).insertOne(Document.parse(objBody.toJSONString()));

        /**
        // Store it in triplestore
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getNamedModel(objBody.getAsString("graph"));

        // RULE
        System.out.println("# Rule definition");
        String RULE_IRI = Namespaces.sup.val()+objBody.getAsString("ruleName");
        RDFUtil.addTriple(model, RULE_IRI, Namespaces.rdf.val()+"type", Rules.RULE.val());
        System.out.println();

        // WINDOW
        System.out.println("# Window definition");
        String WINDOW_SIZE_IRI = RULE_IRI+"/"+objBody.getAsString("windowSize");
        RDFUtil.addTriple(model, WINDOW_SIZE_IRI, Namespaces.rdf.val()+"type", Rules.WINDOW.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_WINDOW.val(), WINDOW_SIZE_IRI);
        System.out.println();

        // EVENT SCHEMA
        System.out.println("# Event schema definition");
        JSONArray arrayEvents = (JSONArray) objBody.get("filters");
        String EVENT_IRI = RULE_IRI+"/Event/";
        for (Object e : arrayEvents) {
            JSONObject obj = (JSONObject) e;
            String name = EVENT_IRI + obj.getAsString("event") + "Schema";
            RDFUtil.addTriple(model, name, Namespaces.rdf.val() + "type", Rules.EVENT_SCHEMA.val());
            RDFUtil.addTriple(model, name, Rules.HAS_EVENT_ATTRIBUTE.val(), obj.getAsString("leftOperand"));
            System.out.println();
        }

        // EVENT ATTRIBUTES
        System.out.println("# Event Attribute definitions");
        for (Object e : arrayEvents) {
            JSONObject obj = (JSONObject) e;
            RDFUtil.addTriple(model, obj.getAsString("leftOperand"), Namespaces.rdf.val() + "type", Rules.EVENT_ATTRIBUTE.val());
        }
        System.out.println();

        // EVENTS
        System.out.println("# Event definitions");
        for (Object ev : arrayEvents) {
            JSONObject obj = (JSONObject) ev;
            RDFUtil.addTriple(model, EVENT_IRI+"/"+obj.getAsString("event"), Namespaces.rdf.val() + "type", Rules.EVENT.val());
            RDFUtil.addTriple(model, EVENT_IRI+"/"+obj.getAsString("event"), Rules.HAS_EVENT_SCHEMA.val(), EVENT_IRI+"/"+obj.getAsString("event")+"Schema");
            System.out.println();
        }


        // PATTERN RELEASES
        System.out.println("# Pattern definition");
        String PATTERN_IRI = RULE_IRI+"/Pattern";
        RDFUtil.addTriple(model, PATTERN_IRI, Namespaces.rdf.val()+"type", Rules.PATTERN.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_CEP_ELEMENT.val(), PATTERN_IRI);
        System.out.println();
        JSONArray arrayPattern = (JSONArray) objBody.get("pattern");
        int patternElementOrder = 1;
        for (Object obj : arrayPattern) {
          //  RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Namespaces.rdf.val()+"type", Rules.INCLUDED_ELEMENT.val());
           // RDFUtil.addTriple(model, EVENT_IRI+"/"+obj.toString(), Namespaces.rdf.val()+"type", Rules.EVENT.val());
            //   RDFUtil.addTriple(model, Integer.toString(patternElementOrder), Namespaces.rdf.val()+"type", Rules.POSITIVE_INTEGER.val());
            RDFUtil.addTriple(model, PATTERN_IRI, Rules.CONTAINS_ELEMENT.val(), PATTERN_IRI+"/"+obj.toString());
            RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Rules.HAS_ELEMENT_ORDER.val(), Integer.toString(patternElementOrder));
            JSONArray ev = (JSONArray) objBody.get("filters");
            RDFUtil.addTriple(model, PATTERN_IRI+"/"+obj.toString(), Rules.REPRESENTS_ELEMENT.val(),EVENT_IRI+"/"+obj.toString());
            System.out.println();
            ++patternElementOrder;
        }

        // CONDITIONS
        System.out.println("# Condition definitions");
        String CONDITION_IRI = RULE_IRI+"/"+objBody.getAsString("condition");
        RDFUtil.addTriple(model, CONDITION_IRI, Namespaces.rdf.val()+"type", Rules.CONDITION.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_CONDITION.val(), CONDITION_IRI);
        System.out.println();

        // ACTION
        System.out.println("# Action definition");
        JSONObject action = (JSONObject) objBody.get("action");
        String ACTION_IRI = RULE_IRI+"/"+action.getAsString("name");
        RDFUtil.addTriple(model, ACTION_IRI, Namespaces.rdf.val()+"type", Rules.ACTION.val());
        RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_ACTION.val(), ACTION_IRI);
        System.out.println();
        JSONArray parameters = (JSONArray) action.get("parameters");
        int parametersOrder = 1;
        for (Object p : parameters) {
            // Instantiate
            RDFUtil.addTriple(model, ACTION_IRI+"/"+p.toString(), Namespaces.rdf.val()+"type", Rules.ACTION_PARAMETER.val());
            // Link
            RDFUtil.addTriple(model, ACTION_IRI, Rules.HAS_ACTION_PARAMETER.val(), ACTION_IRI+"/"+p.toString());
            RDFUtil.addTriple(model, ACTION_IRI+"/"+p.toString(), Rules.HAS_ACTION_PARAMETER_ORDER.val(), Integer.toString(parametersOrder));

            ++parametersOrder;
        }
        System.out.println();

        // FILTERS
        System.out.println("# Filters definitions");
        JSONArray arrayFilters = (JSONArray) objBody.get("filters");
        for (Object f : arrayFilters) {
            JSONObject obj = (JSONObject) f;
            String FILTER_IRI = RULE_IRI+"/Filter/"+obj.getAsString("name");
            System.out.println("# "+obj.getAsString("name"));
            //  RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_FILTER.val(), "Filter"+filterNum);
            RDFUtil.addTriple(model, FILTER_IRI+"/", Namespaces.rdf.val()+"type", Rules.SIMPLE_CLAUSE.val());
            RDFUtil.addTriple(model, RULE_IRI, Rules.HAS_FILTER.val(), FILTER_IRI);
            //RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("leftOperand"), Namespaces.rdf.val()+"type", Rules.USED_ATTRIBUTE.val());
            //  RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("event"), Namespaces.rdf.val()+"type", Rules.EVENT.val());

           // RDFUtil.addTriple(model, FILTER_IRI+"/"+obj.getAsString("leftOperand"), Rules.FOR_EVENT.val(), EVENT_IRI+"/"+obj.getAsString("event"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_LEFT_OPERAND.val(), FILTER_IRI+"/"+obj.getAsString("leftOperand"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_COMPARISON_OPERATOR.val(), FILTER_IRI+"/"+obj.getAsString("comparator"));
            RDFUtil.addTriple(model, FILTER_IRI, Rules.HAS_RIGHT_OPERAND.val(), FILTER_IRI+"/"+obj.getAsString("rightOperand"));
        }

        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
        **/
        client.close();
        return Response.ok(objBody.toJSONString()).build();
    }

    @DELETE
    @Path("cer_rule/{cer_ruleID}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response DELETE_CER_rule(@PathParam("cer_ruleID") String cer_ruleID) {
        System.out.println("[DELETE /cer_rule/]");
        MongoClient client = Utils.getMongoDBClient();
        Document query = new Document("cer_ruleID",cer_ruleID);
        DeleteResult res = getCerRulesCollection(client).deleteOne(query);
        client.close();
        return Response.ok((res.wasAcknowledged())).build();
    }

}
