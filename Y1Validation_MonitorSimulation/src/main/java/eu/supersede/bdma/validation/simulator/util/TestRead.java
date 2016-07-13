package eu.supersede.bdma.validation.simulator.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;
import org.json.JSONObject;

import com.google.common.primitives.Bytes;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import eu.supersede.bdma.validation.simulator.Main;

public class TestRead {

	private static RandomAccessFile logFile;
	private static MongoClient MongoClient;
	private static MongoDatabase MongoDB;
	private static MongoCollection<Document> MongoCollection;
	
	private static int documentLimit;
	private static MongoCursor<Document> sequentialCursor;
	
	private static String nextLine;
		
	public static void main(String[] args) throws Exception {
		Properties properties = new Properties("C:\\UPC\\PhD SVN\\SUPERSEDE\\Development\\Y1Validation_MonitorSimulation\\config.properties"); 
		
		MongoClient = new MongoClient(properties.getProperty("mongodb_server"));
		MongoDB = MongoClient.getDatabase(properties.getProperty("mongodb_db"));
		MongoCollection = MongoDB.getCollection(properties.getProperty("mongodb_collection"));
		logFile = new RandomAccessFile(properties.getProperty("ecosys_core_log"), "r");
		documentLimit = Integer.parseInt(properties.getProperty("document_limit"));		
		//Cursor for sequential tuples
		sequentialCursor = MongoCollection.find().noCursorTimeout(true).iterator();
		
		
		for (int i = 0; i < 10; ++i) {
			String currentLine = i == 0 ? getNextTuple() : nextLine;
			String next = getNextTuple();
			
			System.out.println("current "+currentLine.split("\\|")[0].trim()+" - next "+next.split("\\|")[0].trim() + " --> "+getProcessingTime(currentLine, next));
		}
	}
	
	public static String getNextTuple() throws Exception {
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

	public static String getNextTupleAndReset() throws Exception {
		return getNextTuple();
	}

	public static long getProcessingTime(String now, String next) throws Exception {
		JSONObject objNow = new JSONObject(now);
		JSONObject objNext = new JSONObject(next);
		
		String nowStrDate = objNow.getString("timestamp");
		String nextStrDate = objNext.getString("timestamp");
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		Date nowDate = dateFormat.parse(nowStrDate.substring(0, nowStrDate.length()-3));
		Date newDate = dateFormat.parse(nextStrDate.substring(0, nextStrDate.length()-3));
				
		if (newDate.getTime() < nowDate.getTime()) return 0;
		return (newDate.getTime() - nowDate.getTime()) >= 3000 ? 3000 : newDate.getTime() - nowDate.getTime();
	}


}
