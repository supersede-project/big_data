package eu.supersede.bdma.cep;

import com.google.common.collect.Lists;
import eu.supersede.bdma.cep.utils.AttributeUtils;
import eu.supersede.bdma.cep.utils.Properties;
import eu.supersede.integration.api.mdm.proxies.IMetadataManagement;
import eu.supersede.integration.api.mdm.proxies.MetadataManagementProxy;
import eu.supersede.integration.api.mdm.types.CER_Rule;
import eu.supersede.integration.api.mdm.types.Event;
import eu.supersede.integration.api.mdm.types.Filter;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by snadal on 08/02/17.
 */
public class Main {

    private static boolean validProperties(Properties properties) {
        return !(
                properties.getProperty("BOOTSTRAP_SERVERS_CONFIG") == null ||
                        properties.getProperty("KEY_SERIALIZER_CLASS_CONFIG") == null ||
                        properties.getProperty("VALUE_SERIALIZER_CLASS_CONFIG") == null ||
                        properties.getProperty("UNIFIED_CEP_TOPIC") == null
        );
    }

    public static Properties properties;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new Exception("Usage: [0]=config.properties path");
        }
        properties = new Properties(args[0]);
        if (!validProperties(properties)) {
            throw new Exception("Invalid properties, stopping execution");
        }
        java.util.Properties kafkaProperties = new java.util.Properties();
        kafkaProperties.setProperty("bootstrap.servers", Main.properties.getProperty("BOOTSTRAP_SERVERS_CONFIG"));
        kafkaProperties.setProperty("zookeeper.connect", Main.properties.getProperty("ZOOKEEPER_CONNECT"));
        kafkaProperties.setProperty("group.id", Main.properties.getProperty("GROUP_ID"));
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        FlinkKafkaConsumer010 consumer = new FlinkKafkaConsumer010(Main.properties.getProperty("UNIFIED_CEP_TOPIC"),
                new SimpleStringSchema(), kafkaProperties);

        List<Pattern<String,String>> patterns = Lists.newArrayList();
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();
        int i = 1;
        for (CER_Rule r : proxy.getAllCERRules()) {
            int j = 1;
            Pattern<String,String> pattern = null;
            for (Event e : r.getPatterns()) {
                pattern = (j == 1) ? Pattern.<String>begin("Pattern #"+i+", step #"+j) : pattern.followedBy("Pattern #"+i+", step #"+j);
                ++j;
                pattern=pattern.where(new SimpleCondition<String>() {
                    @Override
                    public boolean filter(String value) throws Exception {
                        return e.getKafkaTopic().equals(((JSONObject)JSONValue.parse(value)).getAsString("topic"));
                    }
                });
                for (Filter f : r.getFilters().stream().filter(f -> f.getEvent().getKafkaTopic().equals(e.getKafkaTopic())).collect(Collectors.toList())) {
                    pattern = pattern.where(new SimpleCondition<String>() {
                        @Override
                        public boolean filter(String value) throws Exception {
                            return AttributeUtils.extractAttribute(((JSONObject)JSONValue.parse(value)).getAsString("tuple")
                                    ,f.getLeftOperand()).equals(f.getRightOperand());
                        }
                    });
                }
            }
            patterns.add(pattern.within(Time.of(r.getWindowTime(), TimeUnit.SECONDS)));
            ++i;
        }
        DataStream<String> stream = env.addSource(consumer);
        for (Pattern p : patterns) {
            PatternStream<String> patternStream = CEP.pattern(stream, p);
            patternStream.select(new PatternSelectFunction<String, String>() {
                public String select(Map<String, List<String>> pattern) throws Exception {
                    System.out.println(pattern.toString());
                    return "ok pattern "+pattern;
                }
            });
        };
        stream.print();
        env.execute();
    }
}
