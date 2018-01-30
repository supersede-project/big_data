package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import eu.supersede.integration.api.adaptation.types.*;
import eu.supersede.integration.api.datastore.integration.types.SupersedePlatform;
import eu.supersede.integration.api.feedback.proxies.FeedbackOrchestratorProxy;
import eu.supersede.integration.api.pubsub.adaptation.AdaptationPublisher;
import eu.supersede.integration.federation.SupersedeFederation;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import eu.supersede.integration.api.feedback.orchestrator.types.Configuration;

/**
 * Created by snadal on 22/11/16.
 */
@Path("metadataStorage")
public class SUPERSEDEResource {

    @GET
    @Path("supersede/platforms")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_federated_platforms() {
        System.out.println("[GET /supersede/platforms/]");

        SupersedeFederation federation = new SupersedeFederation();
        List<SupersedePlatform> platforms = federation.getFederatedSupersedePlatforms();

        List<String> strPlatforms = Lists.newArrayList(
                platforms.stream().map(p -> p.getPlatform()).collect(Collectors.toList())
        );

        return Response.ok(new Gson().toJson(strPlatforms)).build();
    }

    @POST
    @Path("supersede/feedbackReconfiguration")
    @Consumes("text/plain")
    public Response feedbackReconfiguration(String body) throws Exception {
        System.out.println("[GET /supersede/feedbackReconfiguration/]");

        JSONObject objBody = (JSONObject)JSONValue.parse(body);
        FeedbackOrchestratorProxy proxy = new FeedbackOrchestratorProxy(System.getProperty("is.admin.user"),
                System.getProperty("is.admin.password"));
        Configuration C = proxy.getConfiguration(Long.parseLong(objBody.getAsString("applicationId")),
                Long.parseLong(objBody.getAsString("configurationId")));

        Alert alert = new Alert();
        alert.setId("id"+ System.currentTimeMillis());
        alert.setApplicationId(objBody.getAsString("applicationId"));
        alert.setTimestamp(System.currentTimeMillis());
        alert.setTenant(ModelSystem.valueOf(objBody.getAsString("tenant")));
        List<Condition> conditions = Lists.newArrayList();
        conditions.add(new Condition(new DataID("FGTool", "category"), Operator.GT, 1.0)); //feature category        alert.setConditions(conditions);

        List<ActionOnAttribute> actions = Lists.newArrayList();
        C.getGeneralConfiguration().getParameters().forEach(p -> {
        });
        actions.add(new ActionOnAttribute("category.id1.order", AttributeAction.update, 2));
        actions.add(new ActionOnAttribute("category.id2.order", AttributeAction.update, 1));
        actions.add(new ActionOnAttribute("category.id3.order", AttributeAction.update, 3));
        actions.add(new ActionOnAttribute("category.id4.order", AttributeAction.update, 4));
        actions.add(new ActionOnAttribute("category.id5.order", AttributeAction.update, 6));
        actions.add(new ActionOnAttribute("category.id6.order", AttributeAction.update, 5));
        alert.setActionAttributes(actions);

        AdaptationPublisher publisher = new AdaptationPublisher(true,
                new SupersedeFederation().getLocalFederatedSupersedePlatform().getPlatform());
        publisher.publishAdaptationAlertMesssage(alert);

        return Response.ok("success!").build();
    }
}