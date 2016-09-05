package eu.supersede.bdma.validation.streamprocessing.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;


public class Utils {
	
	public static JavaPairReceiverInputDStream<String, String> getKafkaStream(JavaStreamingContext streamContext, String zk_quorum, String groupId, String commaSeparatedTopics) throws Exception {
		Map<String, Integer> topics = new HashMap<String, Integer>();
		for (String topic : commaSeparatedTopics.split(",")) {
			topics.put(topic, 1);
		}
		JavaPairReceiverInputDStream<String, String> kafkaStream = KafkaUtils.createStream(streamContext, zk_quorum, groupId, topics);
		return kafkaStream;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setEnv(Map<String, String> newenv) {
		try {
	        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	        theEnvironmentField.setAccessible(true);
	        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	        env.putAll(newenv);
	        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	        theCaseInsensitiveEnvironmentField.setAccessible(true);
	        Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
	        cienv.putAll(newenv);
	    }
	    catch (NoSuchFieldException e) {
	      try {
	        Class[] classes = Collections.class.getDeclaredClasses();
	        Map<String, String> env = System.getenv();
	        for(Class cl : classes) {
	            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	                Field field = cl.getDeclaredField("m");
	                field.setAccessible(true);
	                Object obj = field.get(env);
	                Map<String, String> map = (Map<String, String>) obj;
	                map.clear();
	                map.putAll(newenv);
	            }
	        }
	      } catch (Exception e2) {
	        e2.printStackTrace();
	      }
	    } catch (Exception e1) {
	        e1.printStackTrace();
	    } 
	}
	
	public static String cutString(String S) {
		return S.length() < 30 ? S.substring(0,S.length()-1) : S.substring(0,30);
	}

}