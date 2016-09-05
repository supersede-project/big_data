package eu.supersede.bdma.validation.streamprocessing;

import org.apache.spark.streaming.api.java.JavaStreamingContext;

public abstract class StreamProcessing {
	
	protected static String topicName;
	protected static String consumerGroup;
	protected static String hbaseTable;
}