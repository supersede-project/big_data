package eu.supersede.bdma.sa.utils;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Sockets {

    public static void sendSocketAlert(String msg, String path) throws Exception {
        URL url = new URL("http://localhost:3000/"+path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream())) {
            writer.write(msg);
            writer.flush();
        }
        conn.getResponseCode();
    }
}

