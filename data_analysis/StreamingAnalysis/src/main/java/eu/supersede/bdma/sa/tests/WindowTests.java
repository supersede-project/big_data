package eu.supersede.bdma.sa.tests;

import eu.supersede.bdma.sa.eca_rules.SerializableECA_Rule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.dstream.DStream;
import scala.Tuple2;

import java.util.List;

/**
 * Created by snadal on 22/06/17.
 */
public class WindowTests {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream, List<SerializableECA_Rule> rules) {
        JavaPairDStream<String,String> w1 = kafkaStream.mapToPair(f -> new Tuple2<>("5 sec",f.value())).window(new Duration(5000),new Duration(5000));
        JavaPairDStream<String,String> w2 = kafkaStream.mapToPair(f -> new Tuple2<>("15 sec",f.value())).window(new Duration(15000),new Duration(5000));

        /*w1.print();
        w2.print();*/
        w1.transformWithToPair(w2,(v1, v2, v3) -> {
            return v1.union(v2);
        }).print();
    }
}
