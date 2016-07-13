package org.upc.dtim.bolster.metadatadatalayer.util;

/**
 * Created by snadal on 17/05/16.
 */
public enum PropertiesEnum {
    SYSTEM_METADATA_DB_SERVER("system_metadata_db_server"),
    SYSTEM_METADATA_DB_NAME("system_metadata_db_name"),

    METADATA_DB_SERVER("metadata_db_server"),
    METADATA_DB_PATH("metadata_db_path"),
    METADATA_DB_FILE("metadata_db_file"),
    METADATA_DB_NAME("metadata_db_name"),

    METADATA_DATA_LAYER_URI("metadata_data_layer_uri"),
    METADATA_DATA_LAYER_URL("metadata_data_layer_url");

    private String code;

    private PropertiesEnum(String s) {
        code = s;
    }

    public String getValue() {
        return code;
    }
}