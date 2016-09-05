package org.upc.dtim.bolster.ontomatchmerge.util;

import com.google.common.io.Files;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by snadal on 8/06/16.
 */
public class TempFiles {

    public static String storeInTempFile(String content) {
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
            Files.write(content.getBytes(),tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

}
