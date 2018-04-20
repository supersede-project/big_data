package eu.supersede.bdma.sa.utils;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;
import net.minidev.json.JSONObject;

public class MonitorReconfigurationJSON {

    public static JSONObject adaptJSON(String in) {
        JSONObject out = new JSONObject();

        JSONObject JSON = (JSONObject) JSONValue.parse(in);
        ((JSONArray)((JSONObject)JSON.get("DiskMonitoredData")).get("DataItems")).forEach(e -> {
            JSONObject inner = (JSONObject)e;
            inner.keySet().forEach(key -> {
                if (!key.equals("instruction")) out.put(key.replace(" ","_"),inner.getAsString(key));
            });
        });
        return out;
    }

}
