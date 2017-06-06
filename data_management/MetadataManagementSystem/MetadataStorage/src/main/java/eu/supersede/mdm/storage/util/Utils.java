package eu.supersede.mdm.storage.util;

import com.mongodb.MongoClient;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

import javax.servlet.ServletContext;

/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient(ServletContext context) {
        return new MongoClient(/*context.getInitParameter("system_metadata_db_server")*/"localhost");
    }

    public static Dataset getTDBDataset(ServletContext context) {
        return TDBFactory.createDataset(/*context.getInitParameter("metadata_db_file")*/"BolsterMetadataStorage" +
                /*context.getInitParameter("metadata_db_name")*/"BolsterMetadataStorage");
    }
}
