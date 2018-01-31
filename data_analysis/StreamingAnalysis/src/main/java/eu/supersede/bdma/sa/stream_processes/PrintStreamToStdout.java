package eu.supersede.bdma.sa.stream_processes;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import scala.Tuple2;

public class PrintStreamToStdout {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        kafkaStream.mapToPair(t -> new Tuple2<String,String>(t.topic(),t.value())).print();
    }

}
