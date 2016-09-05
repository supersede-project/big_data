package org.upc.dtim.bolster.ontomatchmerge.util;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Sergi Nadal - 19/02/2015
 *
 * Utility to handle the properties file
 */

public class Properties {

    public static String getProperty(String property) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream("config.properties"));
            return prop.getProperty(property);
        } catch (IOException ex) {
            return null;
        }
    }

}
