/**
 * 
 */
package eu.supersede.feedbackanalysis.processing.utils;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import com.mysql.cj.jdbc.MysqlDataSource;

import eu.supersede.feedbackanalysis.classification.FeedbackClassifier;
import eu.supersede.feedbackanalysis.classification.SpeechActBasedClassifier;
import eu.supersede.feedbackanalysis.ds.ClassificationResult;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.utils.DBUtils;

/**
 * @author fitsum
 *
 */
public class DBTest {

	@Test
	public void test() {
	
		Properties connectionInfo = new Properties();
		try {
			connectionInfo.load(new FileInputStream("resources/database.properties"));
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		String url = connectionInfo.getProperty("url"); 
		String username = connectionInfo.getProperty("username"); 
		String password = connectionInfo.getProperty("password"); 

		FeedbackClassifier classifier = new SpeechActBasedClassifier();
		try {
			Connection connection = DriverManager.getConnection(url, username, password);
			String sql = "SELECT * FROM feedback";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			
//			ResultSetMetaData metaData = rs.getMetaData();
//			int columnCount = metaData.getColumnCount();
			while (rs.next()){
				String feedbackText = rs.getString(2);
				UserFeedback feedback = new UserFeedback(feedbackText);
				
				List<UserFeedback> userFeedback = new ArrayList<UserFeedback> ();
				userFeedback.add(feedback);
				assertFalse(userFeedback.isEmpty());
				
				List<ClassificationResult> classificationResult = classifier.classify(userFeedback);
				
				if (classificationResult.get(0).getLabel().equalsIgnoreCase(rs.getString(3))){
					System.err.println("correct");
				}else{
					System.err.println("wrong");
				}
				
				break;
			}
			
			
			
			rs.close();
			stmt.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

	@Test
	public void connectionTest(){
		
		DBUtils dbUtils = DBUtils.getInstance();
		List<UserFeedback> feedback = dbUtils.getFeedback();
		assertFalse (feedback.isEmpty());
	}
	
}
