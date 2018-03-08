package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.MongoClient;
import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.clustering.FeedbackSimilarity;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.FeedbackUtils;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by snadal on 17/05/16.
 */
@Path("metadataStorage")
public class FeedbackResource {

    @POST @Path("classification/feedback")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_classify_feedback(String JSON_feedback) {
        System.out.println("[POST /classification/feedback/");

        JSONObject objBody = (JSONObject) JSONValue.parse(JSON_feedback);
        String feedback = objBody.getAsString("feedback");

        FeedbackClassifier feedbackClassifier = new SpeechActBasedClassifier();
        String pathToClassificationModel = ConfigManager.getProperty("resources_path");// Thread.currentThread().getContextClassLoader().getResource("rf.model").toString().replace("file:","");
        JSONObject out = new JSONObject();

        if (!feedback.trim().isEmpty()) {
            ClassificationResult classification = null;
            try {
                classification = feedbackClassifier.classify(pathToClassificationModel, new UserFeedback(feedback));
                out.put("classification",classification.getLabel());
                out.put("accuracy",classification.getAccuracy());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Response.ok(out.toJSONString()).build();
    }

    @POST @Path("clustering/feedback")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response POST_instant_feedback_clustering(String JSON_feedback) throws Exception {
        System.out.println("[POST /clustering/feedback/");

        JSONObject objBody = (JSONObject) JSONValue.parse(JSON_feedback);
        String feedback = objBody.getAsString("feedback");
        int N = objBody.getAsNumber("N").intValue();
        String tenant = objBody.getAsString("tenant");
        String pathToOntology = null;
        String pathToFeedbacks = null;
        if (tenant.equals("atos")) {
            pathToOntology = "ATOS_ontology.ttl";
            pathToFeedbacks = ConfigManager.getProperty("resources_path")+"6e1cc9e2-5bd1-4fd4-8509-75b3c4e40e1c.txt";
        }
        else if (tenant.equals("senercon")) {
            pathToOntology = "SDO_ontology.ttl";
            pathToFeedbacks = ConfigManager.getProperty("resources_path")+"5ff7d393-e2a5-49fd-a4de-f4e1f7480bf4.txt";
        }
        List<UserFeedback> allFeedbacks = FeedbackUtils.getAllFeedbacks(pathToFeedbacks);

        FeedbackSimilarity sim = new FeedbackSimilarity(pathToOntology);
        Map<UserFeedback,Double> clusters = sim.getSimilarFeedback(allFeedbacks,new UserFeedback(feedback),N);

        return Response.ok(clusters.toString()).build();
    }


}