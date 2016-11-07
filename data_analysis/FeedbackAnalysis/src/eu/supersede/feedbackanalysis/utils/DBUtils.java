/**
 * 
 */
package eu.supersede.feedbackanalysis.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import eu.supersede.feedbackanalysis.ds.UserFeedback;

/**
 * @author fitsum
 *
 */
public class DBUtils {
	private static DBUtils instance;
	
	private Connection connection;
	
	public static DBUtils getInstance(){
		if (instance == null){
			instance = new DBUtils();
		}
		return instance;
	}
	
	
	/**
	 * 
	 */
	public DBUtils() {
		Properties connectionInfo = new Properties();
		try {
			connectionInfo.load(new FileInputStream("resources/database.properties"));
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
		String url = connectionInfo.getProperty("url"); 
		String username = connectionInfo.getProperty("username"); 
		String password = connectionInfo.getProperty("password"); 
		
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<UserFeedback> getFeedback(){
		List<UserFeedback> userFeedback = new ArrayList<UserFeedback> ();

		String sql = "SELECT * FROM feedback";
		ResultSet rs;
		try {
			Statement stmt = connection.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()){
				String feedbackText = rs.getString(2);
				UserFeedback feedback = new UserFeedback(feedbackText);
				userFeedback.add(feedback);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		return userFeedback;
	}
	
}
