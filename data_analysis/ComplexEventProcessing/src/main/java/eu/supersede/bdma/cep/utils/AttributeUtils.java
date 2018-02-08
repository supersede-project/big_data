package eu.supersede.bdma.cep.utils;

import com.google.common.collect.Lists;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeUtils {

        private static void traverseRecursive(JSONObject jsonObj, String path, List<String> out) {
            String currentPathElement = path.split("/")[0];
            if (jsonObj.get(currentPathElement) == null) {
                out = null;
            } else {
                if (jsonObj.get(currentPathElement).getClass().getName().equals(JSONObject.class.getName())) {
                    traverseRecursive((JSONObject) jsonObj.get(currentPathElement), path.substring(currentPathElement.length() + 1), out);
                } else if (jsonObj.get(currentPathElement).getClass().getName().equals(JSONArray.class.getName())) {
                    JSONArray theArr = (JSONArray) jsonObj.get(currentPathElement);
                    for (Object arrElem : theArr) {
                        if (arrElem.getClass().getName().equals(JSONObject.class.getName())) {
                            traverseRecursive((JSONObject) arrElem, path.substring(currentPathElement.length() + 1), out);
                        } else {
                            out.add(arrElem.toString());
                        }
                    }
                } else {
                    out.add(jsonObj.getAsString(currentPathElement));
                }
            }
        }

        public static List<String> extractAttribute(String JSON, String Feature) {
            List<String> out = Lists.newArrayList();
            // First, extract the namespace until "Feature" using regex
            Pattern p = Pattern.compile(".*Attributes\\/(.*)");
            Matcher m = p.matcher(Feature);
            if (m.find()) {
                String path = m.group(1);
                try {
                    traverseRecursive((JSONObject) JSONValue.parse(JSON), path, out);
                } catch (Exception e) {
                    System.out.println("Crash for input JSON "+JSON);
                    return null;
                }
            }
            return out;
        }

    }
