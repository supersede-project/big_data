package eu.supersede.bdma.sa.utils;

import com.goebl.david.Webb;
import net.minidev.json.JSONObject;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Sockets {

    public static void sendMessageToSocket(String topic, String message) {
        JSONObject out = new JSONObject();
        out.put("topic",topic);
        out.put("message",message);
        try {
            // TODO use IF to send this message
            Sockets.sendSocketAlert(out.toString(),"raw_data");
        } catch (Exception e) {
            System.out.println("Socket is offline");
            //e.printStackTrace();
        }
    }


    public static void sendSocketAlert(String msg, String path) throws Exception {
        System.out.println(msg);

        //URL url = new URL("http://supersede.es.atos.net:3001/"+path);
        URL url = new URL("http://localhost:3000/"+path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("content-type", "application/json");
        conn.setDoOutput(true);


        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os,"UTF-8");


        osw.write(msg);
        osw.flush();
        osw.close();
    /*
        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(msg);
            writer.flush();
            writer.close();
        }*/
        conn.getResponseCode();
        conn.disconnect();
    }
}

