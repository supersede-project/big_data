package eu.supersede.bdma.validation.simulator.atos;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import eu.supersede.bdma.validation.simulator.Main;
import eu.supersede.bdma.validation.simulator.Simulator;
import eu.supersede.bdma.validation.simulator.Wp2KafkaProducer;

public class AtoS_Simulation extends Simulator {

	private final static Logger logger = LogManager.getLogger(AtoS_Simulation.class);
	private static MongoClient MongoClient;
	private static MongoDatabase MongoDB;
	private static MongoCollection<Document> MongoCollection;
	
	private static int documentLimit;
	private static MongoCursor<Document> sequentialCursor;
	
	private static String nextLine;
	
	public AtoS_Simulation() {
		logger.info("Initializing connection to MongoDB...");
		MongoClient = new MongoClient(Main.properties.getProperty("mongodb_server"));
		MongoDB = MongoClient.getDatabase(Main.properties.getProperty("mongodb_db"));
		MongoCollection = MongoDB.getCollection(Main.properties.getProperty("mongodb_collection"));
		logger.info("Selected MongoDB server ["+MongoClient.getConnectPoint()+"]");
		logger.info("Selected MongoDB database ["+MongoDB.getName()+"]");
		logger.info("Selected MongoDB collection ["+MongoCollection.getNamespace().getCollectionName()+"]");
		
		documentLimit = Integer.parseInt(Main.properties.getProperty("document_limit"));
		logger.info("Selected documentLimit for random tuples ["+documentLimit+"]");
		
		//Cursor for sequential tuples
		sequentialCursor = MongoCollection.find().noCursorTimeout(true).iterator();
		
		try {
			nextLine = getNextTuple();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/*	
	@Override
	public String getRandomTuple() throws Exception {
		//List<String> result = Lists.newArrayList();
		AggregateIterable<Document> allSamples = MongoCollection.aggregate( 
				Lists.newArrayList( 
						new Document("$limit",documentLimit),
						new Document("$sample", new Document("size",1))
					) 
			);
		//for (Document sample : allSamples) {
		//	result.add(sample.toJson());
		//}
		return allSamples.first().toJson();
	}
*/
	@Override
	public String getKafkaTopicName() {
		return "atos";
	}
	
	@Override
    public void run() {
		Wp2KafkaProducer kafkaProducer = new Wp2KafkaProducer(getKafkaTopicName());
		
		while (true) {
			String currentLine = null;

			try {
				currentLine = nextLine;
				nextLine = getNextTuple();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				kafkaProducer.writeToKafka(getKafkaTopicName(), currentLine);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				System.out.println("Sleeping "+getProcessingTime(currentLine, nextLine)+" ms");
				Thread.sleep(getProcessingTime(currentLine, nextLine));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

	@Override
	public String getNextTuple() throws Exception {
		Document doc = sequentialCursor.tryNext();
		/*
		try {
			sequentialCursor.tryNext();
		} catch (com.mongodb.MongoCursorNotFoundException e) {
			sequentialCursor = MongoCollection.find().noCursorTimeout(true).iterator();
		}*/
		if (doc == null) {
			// Reset if at the end
			sequentialCursor = MongoCollection.find().noCursorTimeout(true).iterator();
			doc = sequentialCursor.tryNext();
		}
		return doc.toJson();
	}
	
	@Override
	public String getNextTupleAndReset() throws Exception {
		return getNextTuple();
	}
	
	@Override
	public long getProcessingTime(String now, String next) throws Exception {
		JSONObject objNow = new JSONObject(now);
		JSONObject objNext = new JSONObject(next);
		
		String nowStrDate = objNow.getString("timestamp");
		String nextStrDate = objNext.getString("timestamp");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		Date nowDate = dateFormat.parse(nowStrDate.substring(0, nowStrDate.length()-3));
		Date newDate = dateFormat.parse(nextStrDate.substring(0, nextStrDate.length()-3));
				
		if (newDate.getTime() < nowDate.getTime()) return 0;
		return (newDate.getTime() - nowDate.getTime()) >= 1000 ? 1000 : newDate.getTime() - nowDate.getTime();
	}
	
}
