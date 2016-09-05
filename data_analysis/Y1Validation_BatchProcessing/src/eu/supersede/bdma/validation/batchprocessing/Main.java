package eu.supersede.bdma.validation.batchprocessing;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import eu.supersede.bdma.validation.batchprocessing.util.Properties;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8081/";
    final static Logger logger = LogManager.getLogger(Main.class);
	static String HADOOP_COMMON_PATH = "C:\\Users\\Sergi Nadal\\Downloads\\winutils";
	public static String emails_path;
	
	public static String cache_statistics = "";
	public static String cache_statistics_csv = "";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages("eu.supersede.bdma.validation.batchprocessing");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
    	System.setProperty("hadoop.home.dir", HADOOP_COMMON_PATH);
    	if (args.length != 1) {
			throw new Exception("Usage: [0]=config.properties path");
		}
    	emails_path = args[0];

    	final HttpServer server = startServer();
        System.out.println(String.format("Batch processing started with WADL available at "
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
    
    private static boolean validProperties(Properties properties) {
		if (properties.getProperty("emails_folder") == null) {
			logger.error("Senercon emails folder");
			return false;
		}
		return true;
    }
}

