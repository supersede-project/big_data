package eu.supersede.bdma.sa.stream_processes;

import eu.supersede.bdma.sa.Main;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import scala.Tuple2;

import java.time.LocalDateTime;

public class PrintStreamToStdout {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
        kafkaStream
            //.filter(t -> t.timestamp()>=(System.currentTimeMillis()-Long.parseLong(Main.properties.getProperty("MICROBATCH_PERIOD"))))
            .map(t -> new Tuple2<String,String>(t.topic().toString(),t.value().toString())).foreachRDD(rdd -> {
            System.out.println("-----------------------------------");
            System.out.println(LocalDateTime.now().toString());
            System.out.println("-----------------------------------");
            rdd/*.takeSample(false,Integer.parseInt(Main.properties.getProperty("SAMPLE_SIZE")))*/.foreach(element -> {
                System.out.println(element._1 + " -- " + element._2);
            });
        });
    }

}
