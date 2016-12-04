package eu.supersede.mdm.storage;

import org.glassfish.jersey.server.ResourceConfig;
/**
 * Created by snadal on 29/04/16.
 */
public class Main extends ResourceConfig {

    public Main() {
        System.out.println("MetadataStorage started");
        packages("eu.supersede.mdm.storage.resources");
    }

}

