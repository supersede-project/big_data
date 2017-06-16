package eu.supersede.mdm.storage.util;

import com.mongodb.MongoClient;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

import javax.servlet.ServletContext;

/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient() {
        return new MongoClient(ConfigManager.getProperty("system_metadata_db_server"));
    }

    public static Dataset getTDBDataset() {
        return TDBFactory.createDataset(ConfigManager.getProperty("metadata_db_file")/*"BolsterMetadataStorage"*/ +
                ConfigManager.getProperty("metadata_db_name")/*"BolsterMetadataStorage"*/);
    }
}
