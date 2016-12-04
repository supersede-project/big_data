package eu.supersede.mdm.storage.resources;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;
import org.bson.Document;
import eu.supersede.mdm.storage.parsers.OWLtoD3;
import eu.supersede.mdm.storage.util.Utils;
import scala.Tuple3;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by snadal on 17/05/16.
 */
@Path("")
public class ArtifactResource {

    @Context
    ServletContext context;

    private MongoCollection<Document> getArtifactsCollection(MongoClient client) {
        return client.getDatabase(context.getInitParameter("system_metadata_db_name")).getCollection("artifacts");
    }

    /** System Metadata **/
    @GET @Path("artifacts/{artifactType}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifacts(@PathParam("artifactType") String artifactType) {
        System.out.println("[GET /artifacts/"+artifactType);

        MongoClient client = Utils.getMongoDBClient(this.context);

        List<String> allArtifacts = Lists.newArrayList();
        //Document query = new Document("user",username);
        //query.put("type",artifactType);
        Document query = new Document("type",artifactType);
        getArtifactsCollection(client).find(query).iterator().forEachRemaining(document -> allArtifacts.add(document.toJson()));
        client.close();

        return Response.ok((new Gson().toJson(allArtifacts))).build();
    }

    @POST @Path("artifacts/")
    @Consumes("text/plain")
    public Response POST_artifacts(String JSON_artifact) {
        System.out.println("[POST /artifacts/] JSON_artifact = "+JSON_artifact);

        MongoClient client = Utils.getMongoDBClient(this.context);
        getArtifactsCollection(client).insertOne(Document.parse(JSON_artifact));
        client.close();
        return Response.ok().build();
    }

    /**
     * Get the metadata of the artifact, e.g. name, type, ...
     */
    @GET @Path("artifacts/{artifactType}/{graph}/")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[GET /artifact/"+artifactType+"/"+graph);

        MongoClient client = Utils.getMongoDBClient(this.context);
        Document query = new Document("graph",graph);
        query.put("type",artifactType);
        Document res = getArtifactsCollection(client).find(query).first();
        client.close();

        return Response.ok((res.toJson())).build();
    }

    /**
     * Get the content of the artifact, i.e. the triples
     */
    @GET @Path("artifacts/{artifactType}/{graph}/content")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact_content(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[GET /artifacts/"+artifactType+"/"+graph+"/content");

        Dataset dataset = Utils.getTDBDataset(this.context);
        dataset.begin(ReadWrite.READ);
        String out = "";
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT ?s ?p ?o ?g WHERE { GRAPH <"+graph+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();
            out = ResultSetFormatter.asXMLString(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }
        dataset.end();
        dataset.close();
        return Response.ok((out)).build();
    }

    /**
     * Get the graphical representation of the artifact
     */
    @GET @Path("artifacts/{artifactType}/{graph}/graphical")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response GET_artifact_content_graphical(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[GET /artifacts/"+artifactType+"/"+graph+"/graphical");

        Dataset dataset = Utils.getTDBDataset(this.context);
        dataset.begin(ReadWrite.READ);
        List<Tuple3<Resource,Property,Resource>> triples = Lists.newArrayList();
        String out = "";
        OntModel theModel = ModelFactory.createOntologyModel();
        try(QueryExecution qExec = QueryExecutionFactory.create("SELECT * WHERE { GRAPH <"+graph+"> {?s ?p ?o} }",  dataset)) {
            ResultSet rs = qExec.execSelect();

            rs.forEachRemaining(triple -> {
                triples.add(new Tuple3<Resource,Property,Resource>(new ResourceImpl(triple.get("s").toString()),
                        new PropertyImpl(triple.get("p").toString()),new ResourceImpl(triple.get("o").toString())));
            });

        } catch (Exception e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }

        String JSON = OWLtoD3.parse(artifactType, triples);

        dataset.end();
        dataset.close();
        return Response.ok((JSON)).build();
    }

    @POST @Path("artifacts/{graph}/{username}")
    @Consumes("text/plain")
    public Response POST_artifacts(@PathParam("graph") String graph, String RDF) {
        System.out.println("[POST /artifacts/"+graph);

        Dataset dataset = Utils.getTDBDataset(this.context);
        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getNamedModel(graph);
        OntModel ontModel = ModelFactory.createOntologyModel();

        /* Store RDF into a temporal file */
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
            System.out.println("artifact temp stored in "+filePath);
            Files.write(RDF.getBytes(),tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.ok("Error: "+e.toString()).build();
        }

        model.add(FileManager.get().readModel(ontModel, filePath));
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
        return Response.ok().build();
    }

    @DELETE @Path("artifacts/{artifactType}/{graph}/{username}")
    @Consumes("text/plain")
    public Response DELETE_artifacts(@PathParam("artifactType") String artifactType, @PathParam("graph") String graph) {
        System.out.println("[DELETE /artifacts/"+artifactType+"/"+graph);

        Dataset dataset = Utils.getTDBDataset(this.context);
        dataset.begin(ReadWrite.WRITE);

        dataset.removeNamedModel(graph);

        dataset.commit();
        dataset.end();
        dataset.close();

        MongoClient client = Utils.getMongoDBClient(this.context);
        getArtifactsCollection(client).deleteOne(new Document("graph",graph));
        client.close();

        return Response.ok().build();
    }

}