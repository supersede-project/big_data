package eu.supersede.bdma.sa.utils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clearspring.analytics.util.Lists;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;


public class Utils {

    private static void traverseRecursive(JSONObject jsonObj, String path, List<String> out) {
        String currentPathElement = path.split("/")[0];
        System.out.println("Current obj "+jsonObj.toJSONString());
        System.out.println("path "+path);
        System.out.println("currentPathElement "+currentPathElement);

        if (jsonObj.get(currentPathElement).getClass().getName().equals(JSONObject.class.getName())) {
            System.out.println("going to embedded obj");
            traverseRecursive((JSONObject)jsonObj.get(currentPathElement),path.substring(currentPathElement.length()+1),out);
        }
        else if (jsonObj.get(currentPathElement).getClass().getName().equals(JSONArray.class.getName())) {
            System.out.println("going to array");
            JSONArray theArr = (JSONArray)jsonObj.get(currentPathElement);
            for (Object arrElem : theArr) {
                if (arrElem.getClass().getName().equals(JSONObject.class.getName())) {
                    System.out.println("is an embedded object");
                    traverseRecursive((JSONObject)arrElem,path.substring(currentPathElement.length()+1),out);
                } else {
                    System.out.println("is a value 1 and it is "+arrElem.toString());

                    out.add(arrElem.toString());
                }
            }
        }
        else {
            System.out.println("is a value 2 and it is "+jsonObj.getAsString(currentPathElement));
            out.add(jsonObj.getAsString(currentPathElement));
        }
    }

    // TODO Here we must obtain the info from the graph, and as we navigate already know if we have embedded objs, arrays, etc..
    /**
     * This method navigates on a JSON document given the path depicted in the Feature and returns the values
     *      Note the return value is a list, as we might be dealing with Arrays
     * If the path does not match the JSON structure then **null** is returned.
     *
     * Examples of Features:
     *  http://www.BDIOntology.com/global/Feature/Vod
     *  http://www.BDIOntology.com/global/Feature/configurations/mechanisms/parameters/value
     *
     */
    public static List<String> extractFeatures(String JSON, String Feature) {
        List<String> out = Lists.newArrayList();
        // First, extract the namespace until "Feature" using regex
        Pattern p = Pattern.compile(".*Feature\\/(.*)");
        Matcher m = p.matcher(Feature);
        if (m.find()) {
            String path = m.group(1);
            traverseRecursive((JSONObject)JSONValue.parse(JSON),path,out);
            //System.out.println(theObj.toJSONString());
            //for ()

        }
        return out;
    }

    public static JavaInputDStream<ConsumerRecord<String, String>> getKafkaStream(JavaStreamingContext streamContext, Collection<String> topics, Map<String, Object> kafkaParams) throws Exception {
        final JavaInputDStream<ConsumerRecord<String, String>> kafkaStream = KafkaUtils.createDirectStream(
                streamContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams)
        );
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