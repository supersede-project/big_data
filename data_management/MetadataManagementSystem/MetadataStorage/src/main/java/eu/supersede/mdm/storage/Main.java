package eu.supersede.mdm.storage;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import eu.supersede.mdm.storage.util.Properties;
import eu.supersede.mdm.storage.util.PropertiesEnum;

import java.io.IOException;
import java.net.URI;

/**
 * Created by snadal on 29/04/16.
 */
public class Main {

    public static final String BASE_URI = Properties.getProperty(PropertiesEnum.METADATA_DATA_STORAGE_URI.getValue());

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("eu.supersede.mdm.storage.resources")
                .register(MultiPartFeature.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("MetadataStorage started with WADL available at "
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

