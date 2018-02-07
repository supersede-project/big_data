package eu.supersede.bdma.cep;

import eu.supersede.bdma.cep.utils.Properties;

/**
 * Created by snadal on 20/09/16.
 */
public class Main {

    public static Properties properties;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Usage: [0]=config.properties path");
        }
        properties = new Properties(args[0]);
        if (!validProperties(properties)) {
            throw new Exception("Invalid properties, stopping execution");
        }
        System.out.println("HI!");
    }

    private static boolean validProperties(Properties properties) {
        return !(
            properties.getProperty("BOOTSTRAP_SERVERS_CONFIG") == null ||
            properties.getProperty("KEY_SERIALIZER_CLASS_CONFIG") == null ||
            properties.getProperty("VALUE_SERIALIZER_CLASS_CONFIG") == null ||

	    properties.getProperty("UNIFIED_CEP_TOPIC") == null

            );
    }

}
