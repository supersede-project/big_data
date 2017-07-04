package eu.supersede.bdma.sa.stream_processes;

import com.google.common.io.Files;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.TaskContext;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.OffsetRange;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by snadal on 28/05/17.
 */
public class Dispatcher {

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream) {
            kafkaStream.foreachRDD(records -> {
            final OffsetRange[] offsetRanges = ((HasOffsetRanges) records.rdd()).offsetRanges();
            records.foreachPartition(consumerRecords -> {
                OffsetRange o = offsetRanges[TaskContext.get().partitionId()];
                consumerRecords.forEachRemaining(record -> {
                    // Dispatch always (For validation)
                    //if (broadcastReleases.value().get(o.topic())._1()) { //isDispatch?
                        // TODO Warning, using local FS methods. Must change for HDFS
                        try {
                            Files.append(record.value()+"\n", new File("/home/supersede/Bolster/DispatcherData/"+o.topic()+".txt"), Charset.defaultCharset());
                            //Files.append(record.value()+"\n", new File(broadcastReleases.value().get(o.topic())._2()), Charset.defaultCharset());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    //}
                });
            });
        });
    }

}
