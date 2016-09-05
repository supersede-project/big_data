package eu.supersede.bdma.validation.simulator;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wp2KafkaProducer {
	
	private final static Logger logger = LogManager.getLogger(Wp2KafkaProducer.class);
	private KafkaProducer<String, String> producer;
	
	public Wp2KafkaProducer(String topic) {
		logger.info("Initializing connection to Kafka...");

		java.util.Properties props = new java.util.Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Main.properties.getProperty("kafka_bootstrap_servers"));
		props.put("client.id", "SimuladorMonitor");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
		props.put(ProducerConfig.SEND_BUFFER_CONFIG, 1024*100);
		props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
		props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 5*60*1000L);
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60*1000L);
		props.put(ProducerConfig.ACKS_CONFIG, "0");
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1500);
		props.put(ProducerConfig.RETRIES_CONFIG, 1);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 32 * 1024 * 1024L);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16 * 1024);

		/*java.util.Properties props = new java.util.Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, );
		props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
		props.put(ProducerConfig.SEND_BUFFER_CONFIG, 1024*100);
		props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 100);
		props.put(ProducerConfig.METADATA_MAX_AGE_CONFIG, 5*60*1000L);
		props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60*1000L);
		props.put(ProducerConfig.ACKS_CONFIG, "0");
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 1500);
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		props.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 32 * 1024 * 1024L);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16 * 1024);
		props.put(ProducerConfig.CLIENT_ID_CONFIG, "SimuladorMonitor");
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");*/

/*
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put("producer.type", "async");
		props.put("request.required.acks", 1);
		props.put(ProducerConfig.BATCH_SIZE_CONFIG, 200);
		props.put("message-send-max-retries",3);
		props.put("retry-backoff-ms",100);
		props.put("timeout",1000);
		props.put("queue-size",10000);
		props.put("queue-enqueuetimeout-ms",Integer.MAX_VALUE);
		props.put("request-required-acks",0);
		props.put("request-timeout-ms",1500);
		props.put("metadata-expiry-ms",5*60*1000L);
		props.put("max-block-ms",60*1000L);
		props.put("max-memory-bytes",32 * 1024 * 1024L);
		props.put("max-partition-memory-bytes",16 * 1024L);
		props.put("socket-buffer-size",1024*100);
*/

		this.producer = new KafkaProducer<>(props);

		logger.info("Selected Kafka server ["+Main.properties.getProperty("kafka_bootstrap_servers")+"]");
		logger.info("Selected Kafka topic ["+topic+"]");
	}
	
	public void writeToKafka(String topic, String content) {
		logger.debug("Writing to kafkaTopic ["+topic+"]");
		logger.debug("Content ["+eu.supersede.bdma.validation.simulator.util.Util.cutString(content)+"]");
		ProducerRecord<String,String> data = new ProducerRecord<String, String>(topic, content);
		logger.debug("Defined new ProducerRecord");
		long X = System.currentTimeMillis();
		this.producer.send(data, null);
		long Y = System.currentTimeMillis();
		logger.debug("Time to send "+(Y-X)+" ms");
		logger.debug("New event streamed to Kafka");
	}
}
