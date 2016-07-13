package org.upc.dtim.bolster.ontomatchmerge;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.upc.dtim.bolster.ontomatchmerge.util.Properties;
import org.upc.dtim.bolster.ontomatchmerge.util.PropertiesEnum;

import java.io.IOException;
import java.net.URI;

/**
 * Created by snadal on 29/04/16.
 */
public class Main {

    public static final String BASE_URI = Properties.getProperty(PropertiesEnum.ONTO_MATCH_MERGE_URI.getValue());

    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers

        final ResourceConfig rc = new ResourceConfig().packages("org.upc.dtim.bolster.ontomatchmerge.resources")
                .register(MultiPartFeature.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("OntoMatchMerge started with WADL available at "
                + "%sapplication.wadl\n", BASE_URI));
        try {
            Object lock = new Object();
            synchronized (lock) {
                while (true) {
                    lock.wait();
                }
            }
        } catch (InterruptedException ex) {
        }
    }
}
