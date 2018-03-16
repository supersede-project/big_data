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
import java.util.Arrays;
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

    @GET
    @Path("supersede/tenants")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_tenants() {
        System.out.println("[GET /supersede/tenants/]");
        List<String> tenants = Lists.newArrayList(Arrays.stream(Tenant.values()).map(t -> t.getId()).collect(Collectors.toList()));
        return Response.ok(new Gson().toJson(tenants)).build();
    }

    @POST
    @Path("supersede/feedbackReconfiguration")
    @Consumes("text/plain")
    public Response feedbackReconfiguration(String body) throws Exception {
        System.out.println("[GET /supersede/feedbackReconfiguration/]");
        return Response.ok("success!").build();
    }
}