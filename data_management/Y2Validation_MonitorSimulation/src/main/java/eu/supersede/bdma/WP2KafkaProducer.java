package eu.supersede.bdma;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by snadal on 22/01/17.
 */
public class WP2KafkaProducer {

    public static void writeToKafka(String data, String topic) {
        final Properties KAFKA_CONFIG;
        KAFKA_CONFIG = new Properties();
        KAFKA_CONFIG.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        KAFKA_CONFIG.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        KAFKA_CONFIG.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String,String> p = new KafkaProducer<String, String>(KAFKA_CONFIG);
        ProducerRecord<String, String> message =
                new ProducerRecord<String, String>(topic, "id", data);
        p.send(message);

    }

}
