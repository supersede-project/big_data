package eu.supersede.bdma.cep.utils;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Sergi Nadal - 19/02/2015
 *
 * Utility to handle the properties file
 */

public class Properties {

    public Properties(String path) {
        this.path=path;
    }

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProperty(String property) {
        java.util.Properties prop = new java.util.Properties();
        try {
            prop.load(new FileInputStream(path));
            return prop.getProperty(property);
        } catch (IOException ex) {
            return null;
        }
    }

}

