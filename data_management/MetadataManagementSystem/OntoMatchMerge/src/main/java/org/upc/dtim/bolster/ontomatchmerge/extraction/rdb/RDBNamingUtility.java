/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

/**
 *
 * @author Rizkallah
 */
public class RDBNamingUtility {
    public static final String ONTOLOGY_BASE_URI = "http://www.upc.edu/it4bi/rt";
    public static final String ONTOLOGY_INSTANCE_BASE_URI = "upc:it4bi:rt:INSTANCE";
    public static final String DATATYPE_PROPERTY_PREFIX = "";
    public static final String OBJECT_PROPERTY_PREFIX = "has";
    
    public static String getClassURI(String dbName, String resourceName) {
        return RDBNamingUtility.ONTOLOGY_BASE_URI + "/" + dbName + "#" + resourceName;
    }
    
    public static String getDTPropURI(String dbName, String resourceName) {
        return RDBNamingUtility.ONTOLOGY_BASE_URI + "/" + dbName + "#" + RDBNamingUtility.DATATYPE_PROPERTY_PREFIX + resourceName;
    }
    
    public static String getInstanceURI(String dbName, String resourceName) {
        return RDBNamingUtility.ONTOLOGY_BASE_URI + "/" + dbName + "/instance#" + resourceName;
    }
    
    public static XSDDatatype getXsdDataType(String rdbType) {
        if (rdbType.equals("bit")
                || rdbType.equals("tinyint") 
                || rdbType.equals("smallint") 
                || rdbType.equals("mediumint"))
            return XSDDatatype.XSDshort;
        else if (rdbType.equals("int"))
            return XSDDatatype.XSDint;
        else if (rdbType.equals("bigint"))
            return XSDDatatype.XSDlong;
        else if (rdbType.equals("integer"))
            return XSDDatatype.XSDinteger;
        else if (rdbType.equals("bool")
                || rdbType.equals("boolean"))
            return XSDDatatype.XSDboolean;
        else if (rdbType.equals("decimal") 
                || rdbType.equals("dec"))
            return XSDDatatype.XSDdecimal;
        else if (rdbType.equals("numeric") 
                || rdbType.equals("fixed") 
                || rdbType.equals("float"))
            return XSDDatatype.XSDfloat;
        else if (rdbType.equals("double"))
            return XSDDatatype.XSDdouble;
        else if (rdbType.equals("date"))
            return XSDDatatype.XSDdate;
        else if (rdbType.equals("datetime")
                || rdbType.equals("timestamp") )
            return XSDDatatype.XSDdateTime;
        else if (rdbType.equals("time"))
            return XSDDatatype.XSDtime;
        else if (rdbType.equals("year"))
            return XSDDatatype.XSDgYear;
        else
            return XSDDatatype.XSDstring;
    }
}
