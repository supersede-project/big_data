package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import eu.supersede.integration.api.datastore.integration.types.SupersedePlatform;
import eu.supersede.integration.federation.SupersedeFederation;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.util.ConfigManager;
import eu.supersede.mdm.storage.util.Utils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.bson.Document;
import scala.Tuple3;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        List<String> allPlatforms = Lists.newArrayList();

        SupersedeFederation federation = new SupersedeFederation();
        List<SupersedePlatform> platforms = federation.getFederatedSupersedePlatforms();

        List<String> strPlatforms = Lists.newArrayList(
                platforms.stream().map(p -> p.getPlatform()).collect(Collectors.toList())
        );

        return Response.ok(new Gson().toJson(strPlatforms)).build();
    }

}
