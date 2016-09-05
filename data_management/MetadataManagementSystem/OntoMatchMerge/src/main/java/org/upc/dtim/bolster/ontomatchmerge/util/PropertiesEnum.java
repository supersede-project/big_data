package org.upc.dtim.bolster.ontomatchmerge.util;

/**
 * Created by snadal on 17/05/16.
 */
public enum PropertiesEnum {

    ONTO_MATCH_MERGE_URI("onto_match_merge_uri"),
    ONTO_MATCH_MERGE_URL("onto_match_merge_url");

    private String code;

    private PropertiesEnum(String s) {
        code = s;
    }

    public String getValue() {
        return code;
    }
}