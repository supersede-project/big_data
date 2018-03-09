package eu.supersede.mdm.storage.util;

import com.google.common.collect.Lists;
import eu.supersede.feedbackanalysis.ds.UserFeedback;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;
import net.minidev.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FeedbackUtils {

    public static List<UserFeedback> getAllFeedbacks(String path) throws Exception {
        List<UserFeedback> res = Lists.newArrayList();

        List<String> allJsons = Lists.newArrayList();
        String json = "";
        for (String l : Files.lines(new File(path).toPath()).collect(Collectors.toList())) {
            json += (l.replace("\n",""));
            try {
                JSONObject a = (JSONObject) JSONValue.parse(json);
                if (a != null) {
                    allJsons.add(json);
                    json = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        allJsons.forEach(aJSON -> {
            String feedback = "";
            for (String feedbackPiece : extractFeatures(aJSON,"Attributes/textFeedbacks/text")) {
                if (!feedbackPiece.contains("@")) feedback += " " + feedbackPiece;
            }
            feedback = feedback.replace("\n","");
            if (!feedback.isEmpty()) {
                res.add(new UserFeedback(feedback));
            }
        });

        return res;
    }



    private static void traverseRecursive(net.minidev.json.JSONObject jsonObj, String path, List<String> out) {
        String currentPathElement = path.split("/")[0];
        if (jsonObj.get(currentPathElement) == null) {
            out = null;
        } else {
            if (jsonObj.get(currentPathElement).getClass().getName().equals(net.minidev.json.JSONObject.class.getName())) {
                traverseRecursive((net.minidev.json.JSONObject) jsonObj.get(currentPathElement), path.substring(currentPathElement.length() + 1), out);
            } else if (jsonObj.get(currentPathElement).getClass().getName().equals(JSONArray.class.getName())) {
                JSONArray theArr = (JSONArray) jsonObj.get(currentPathElement);
                for (Object arrElem : theArr) {
                    if (arrElem.getClass().getName().equals(net.minidev.json.JSONObject.class.getName())) {
                        traverseRecursive((net.minidev.json.JSONObject) arrElem, path.substring(currentPathElement.length() + 1), out);
                    } else {
                        out.add(arrElem.toString());
                    }
                }
            } else {
                out.add(jsonObj.getAsString(currentPathElement));
            }
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
        Pattern p = Pattern.compile(".*Attributes\\/(.*)");
        Matcher m = p.matcher(Feature);
        if (m.find()) {
            String path = m.group(1);
            try {
                traverseRecursive((net.minidev.json.JSONObject) JSONValue.parse(JSON), path, out);
            } catch (Exception e) {
                System.out.println("Crash for input JSON "+JSON);
                return null;
            }
        }
        return out;
    }

}
