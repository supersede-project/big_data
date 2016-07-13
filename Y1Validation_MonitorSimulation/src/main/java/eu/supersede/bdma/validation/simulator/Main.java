package eu.supersede.bdma.validation.simulator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.supersede.bdma.validation.simulator.siemens.SIEMENS_Simulation;
import eu.supersede.bdma.validation.simulator.util.Properties;

/**
 * @author Sergi Nadal
 */
public class Main {
	
	private final static Logger logger = LogManager.getLogger(Main.class);
	public static Properties properties;
		
	public static void main(String[] args) throws Exception {
		Runtime.getRuntime().addShutdownHook(
				  new Thread("app-shutdown-hook") {
				    @Override 
				    public void run() { 
				      System.out.println("bye"); 
				    }
				});
		
		if (args.length != 2) {
			throw new Exception("Usage: [0]=config.properties path, [1]=use case(atos,siemens,senercon)");
		}
		
		properties = new Properties(args[0]);
		if (!validProperties(properties)) {
			throw new Exception("Invalid properties, stopping execution");
		}
		
		if (args[1].equals("atos")) {
			Simulator atos_simulation = new eu.supersede.bdma.validation.simulator.atos.AtoS_Simulation();
			atos_simulation.start();
		}
		else if (args[1].equals("siemens")) {
			Simulator siemens_simulation = new eu.supersede.bdma.validation.simulator.siemens.SIEMENS_Simulation();
			siemens_simulation.start();
		}
		else if (args[1].equals("senercon")) {
			Simulator senercon_simulation = new eu.supersede.bdma.validation.simulator.senercon.SEnerCon_Simulation();
			senercon_simulation.run();
		}
		else {
			throw new Exception("Invalid use case");
		}
	};

	private static boolean validProperties(Properties properties) {
		if (properties.getProperty("mongodb_server") == null) {
			logger.error("Invalid property \"mongodb_server\" (AtoS MongoDB server URL)");
			return false;
		}
		if (properties.getProperty("mongodb_db") == null) {
			logger.error("Invalid property \"mongodb_db\" (AtoS MongoDB source database name)");
			return false;
		}
		if (properties.getProperty("mongodb_collection") == null) {
			logger.error("Invalid property \"mongodb_collection\" (AtoS MongoDB source collection name)");
			return false;
		}
		if (properties.getProperty("kafka_bootstrap_servers") == null) {
			logger.error("Invalid property \"kafka_bootstrap_servers\")");
			return false;
		}
		if (properties.getProperty("kafka_topic") == null) {
			logger.error("Invalid property \"kafka_topic\" (Kafka target topic)");
			return false;
		}
		if (properties.getProperty("document_limit") == null) {
			logger.error("Invalid property \"document_limit\" (Number of documents to retrieve for sampling. High values affect performance!)");
			return false;
		}
		if (properties.getProperty("ecosys_core_log") == null) {
			logger.error("Invalid property \"ecosys_core_log\" (Path for SIEMENS dataset)");
			return false;
		}
		if (properties.getProperty("emails_folder") == null) {
			logger.error("Invalid property \"emails_folder\" (Path for SEnerCon dataset)");
			return false;
		}
		
		return true;
	};
	
	
}

