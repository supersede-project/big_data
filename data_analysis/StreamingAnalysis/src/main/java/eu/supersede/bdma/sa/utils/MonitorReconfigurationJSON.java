package eu.supersede.bdma.sa.utils;

import net.minidev.json.JSONValue;
import org.json.JSONObject;

public class MonitorReconfigurationJSON {

    public static String adaptJSON(String in) {
        JSONObject JSON = (JSONObject) JSONValue.parse(in);
        JSON.getJSONObject("DiskMonitoredData").getJSONArray("DataItems").forEach(e -> {
            JSONObject inner = (JSONObject)e;
            inner.keys().forEachRemaining(key -> {
                if (!key.equals("instruction")) JSON.put(key.replace(" ","_"),inner.getString(key));
            });
        });
        JSON.getJSONObject("DiskMonitoredData").remove("DataItems");
        System.out.println(JSON.toString());
        return JSON.toString();
    }

}
