package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.mdm.storage.cep.RDF_Model.Operators.*;
import eu.supersede.mdm.storage.cep.RDF_Model.Rule;
import eu.supersede.mdm.storage.cep.RDF_Model.action.Action;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.ComplexPredicate;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.Condition;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.LiteralOperand;
import eu.supersede.mdm.storage.cep.RDF_Model.condition.SimpleClause;
import eu.supersede.mdm.storage.cep.RDF_Model.event.*;
import eu.supersede.mdm.storage.cep.RDF_Model.window.Window;
import eu.supersede.mdm.storage.cep.RDF_Model.window.WindowType;
import eu.supersede.mdm.storage.cep.manager.FlumeCollector;
import eu.supersede.mdm.storage.cep.manager.Manager;
import eu.supersede.mdm.storage.cep.sm4cep.Sm4cepParser;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.ActionTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.OperatorTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.eca_rules.PredicatesTypes;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.Rules;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.RDFUtil;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.bson.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @GET
    @Path("eca_rule/{ruleName}/generate_config_file")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_ECA_rule_config_file(@PathParam("ruleName") String ruleName) {
        System.out.println("[GET /eca_rule/{ruleName}/generate_config_file]");
        try {
            Sm4cepParser sm4cepparser = new Sm4cepParser();
            sm4cepparser.getAllEventSchemata();
            Rule r = sm4cepparser.getRule(ruleName);

            Manager m = new Manager();
            String s = m.CreateConfiguration("SergiAgent",Lists.newArrayList(sm4cepparser.getEventSchemata().values()), Lists.newArrayList(r),"localhost:9092","stream_type",false,"");

            File f = File.createTempFile(UUID.randomUUID().toString(), ".config", new File("/home/alba/SUPERSEDE/tmpFiles/"));
            BufferedWriter bw = new BufferedWriter(new FileWriter(s));
            bw.write(ruleName);
            bw.close();
            System.out.println(f.getAbsolutePath());
            //return Response.ok(new Gson().toJson(f)).build();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return Response.ok(new Gson().toJson(ruleName)).build();
    }

    //Load metamodel sm4cep
    @GET @Path("eca_rule/load_sm4cep")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_load_sm4cep() {
        System.out.println("[GET /eca_rule/load_sm4cep/");
        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel();
        model.add(FileManager.get().readModel(ontModel, "sm4cep_metamodel.ttl"));
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
        return Response.ok("OK").build();
    }

    @POST @Path("cer_rule/directGeneration")
    @Consumes("text/plain")
    public Response directGeneration(String body) throws FileNotFoundException {
        List<EventSchema> events = Lists.newArrayList();
        JSONObject objBody = (JSONObject) JSONValue.parse(body);
        MongoClient client = Utils.getMongoDBClient();

        // Sequence
        Sequence sequence = new Sequence();
        TemporalPattern sequenceEvent = new TemporalPattern();
        sequenceEvent.setTemporalOperator(sequence);

        ((JSONArray)objBody.get("pattern")).stream().forEach(event -> {
            String eventId = (String)event;
            Document eventDocument = getEventsCollection(client).find(new Document("eventID",eventId)).first();
            EventSchema schema = new EventSchema(/*eventDocument.getString("event")*/eventId,Lists.newArrayList());
            schema.setIRI(eventDocument.getString("event"));
            EventResource.getAttributesForEvent(eventDocument.getString("graph")).forEach(attribute -> {
                JSONObject objAttribute = (JSONObject)JSONValue.parse(attribute.toString());
                schema.getAttributes().add(new Attribute(objAttribute.getAsString("name"), AttributeType.TYPE_STRING,schema));
            });
            schema.setTopicName(eventDocument.getString("kafkaTopic"));

            events.add(schema);

            Event ev = new Event();
            ev.setEventSchema(schema);
            sequenceEvent.addEvents(ev);
        });
        Rule R = new Rule();
        R.setIRI(objBody.getAsString("ruleName"));

        // Process conditions (filters)
        ComplexPredicate whereCondition = new ComplexPredicate();
        whereCondition.setOperator(new LogicOperator(LogicOperatorEnum.Conjunction));
        ((JSONArray)objBody.get("filters")).stream().forEach(filter -> {
            JSONObject objFilter = (JSONObject)JSONValue.parse(filter.toString());
            LiteralOperand l = new LiteralOperand(AttributeType.TYPE_STRING,objFilter.getAsString("rightOperand"));
            SimpleClause c = new SimpleClause();

            c.setOperand1(events.stream()
                    .filter(es -> es.getEventName().equals(objFilter.getAsString("event"))).collect(Collectors.toList()).get(0)
                    .getAttributes().stream()
                    .filter(a -> a.getName().equals(objFilter.getAsString("leftOperand"))).collect(Collectors.toList()).get(0));
            c.setOperator(new ComparasionOperator(ComparasionOperatorEnum.EQ));
            c.setOperand2(l);

            whereCondition.getConditions().add(c);
        });

        Within within = new Within();
        within.setOffset(Integer.parseInt(objBody.getAsString("windowTime")));
        within.setTimeUnit(TimeUnit.second);

        TemporalPattern withinEvent = new TemporalPattern();
        withinEvent.setTemporalOperator(within);
        withinEvent.addEvents(sequenceEvent);

        //Window
        Window window = new Window();
        window.setTimeUnit(TimeUnit.second);
        window.setWindowType(WindowType.TUMBLING_WINDOW);
        window.setWithin(Integer.parseInt(objBody.getAsString("windowSize")));

        //Action
        Action action = new Action();
        ((JSONArray)objBody.get("actionParameters")).stream().forEach(actionParameter -> {
            String actionEvent = ((JSONObject)actionParameter).getAsString("event");
            String actionAttribute = ((JSONObject)actionParameter).getAsString("attribute");

            action.addActionAttribute(
                    events.stream().filter(es -> es.getEventName().equals(actionEvent)).collect(Collectors.toList()).get(0)
                    .getAttributes().stream().filter(a -> a.getName().equals(actionAttribute)).collect(Collectors.toList()).get(0)
            );
        });

        R.setCondition(whereCondition);
        R.setCEPElement(withinEvent);
        R.setWindow(window);
        R.setAction(action);

        System.out.println("######################################################################");
        System.out.println("######################################################################");
        System.out.println("######################################################################");

        Manager m = new Manager();
        String conf = "fail";
        try {
            conf = m.CreateConfiguration(objBody.getAsString("ruleName"),events,Lists.newArrayList(R),
                    "localhost:9092","json",false,"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(conf);

        System.out.println("######################################################################");
        System.out.println("######################################################################");
        System.out.println("######################################################################");

        FlumeCollector flumeCollector = new FlumeCollector();
        try {
            String collector = flumeCollector.interpret("collector", events, Lists.newArrayList(R), "localhost:9092", "json", false, "");
            System.out.println("collector");
            System.out.println(collector);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        System.out.println("######################################################################");
        System.out.println("######################################################################");
        System.out.println("######################################################################");

/*
        Manager m = new Manager();
        String conf = "fail";
        try {
            conf = m.CreateConfiguration(objBody.getAsString("ruleName"),events,Lists.newArrayList(R),
                    "localhost:9092","json",false,"");
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(conf);
*/
        return Response.ok("ciao").build();
    }

}
