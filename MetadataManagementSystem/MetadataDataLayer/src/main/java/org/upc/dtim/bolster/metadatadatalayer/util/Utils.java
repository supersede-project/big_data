package org.upc.dtim.bolster.metadatadatalayer.util;

import com.mongodb.MongoClient;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;

/**
 * Created by snadal on 17/05/16.
 */
public class Utils {

    public static MongoClient getMongoDBClient() {
        return new MongoClient(Properties.getProperty(PropertiesEnum.SYSTEM_METADATA_DB_SERVER.getValue()));
    }

    public static Dataset getTDBDataset() {
        return TDBFactory.createDataset(Properties.getProperty(PropertiesEnum.METADATA_DB_FILE.getValue())+
                Properties.getProperty(PropertiesEnum.METADATA_DB_NAME.getValue()));
    }
}
