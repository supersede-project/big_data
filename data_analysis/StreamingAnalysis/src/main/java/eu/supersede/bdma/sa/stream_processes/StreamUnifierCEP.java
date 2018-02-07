package eu.supersede.bdma.sa.stream_processes;

import com.google.common.io.Files;
import eu.supersede.bdma.sa.Main;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 * Created by snadal on 28/05/17.
 */
public class StreamUnifierCEP {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        final Properties KAFKA_CONFIG;
        KAFKA_CONFIG = new Properties();
        KAFKA_CONFIG.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Main.properties.getProperty("BOOTSTRAP_SERVERS_CONFIG"));
        KAFKA_CONFIG.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Main.properties.getProperty("KEY_SERIALIZER_CLASS_CONFIG"));
        KAFKA_CONFIG.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Main.properties.getProperty("VALUE_SERIALIZER_CLASS_CONFIG"));

        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(KAFKA_CONFIG);

        kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    ProducerRecord<String, String> message =
                            new ProducerRecord<String, String>(Main.properties.getProperty("UNIFIED_CEP_TOPIC"), record.key(), record.value());
                    producer.send(message);
                });
            });
        });
    }
}
