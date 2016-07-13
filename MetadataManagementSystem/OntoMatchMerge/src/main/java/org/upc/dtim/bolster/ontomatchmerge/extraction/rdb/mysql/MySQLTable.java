/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.mysql;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMapping;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.RDBNamingUtility;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Mapping;

/**
 *
 * @author Rizkallah
 */
public class MySQLTable {
    
    private String name;
    private MySQLDatabase database;
    private ArrayList<MySQLColumn> columns;
    private ArrayList<MySQLColumn> primaryKeyColumns;
    private ArrayList<MySQLForeignKey> foreignKeys;
    
    private String tempInstanceURI;

    public MySQLTable() {
        this.name = "";
        this.database = null;
        this.columns = new ArrayList();
        this.primaryKeyColumns = new ArrayList();
        this.foreignKeys = new ArrayList();
    }
    
    public MySQLTable(String name, MySQLDatabase database) {
        this.name = name;
        this.database = database;
        this.columns = new ArrayList();
        this.primaryKeyColumns = new ArrayList();
        this.foreignKeys = new ArrayList();
    }

    public void loadDatabaseSchema(Connection conn) throws SQLException {
        // 1. Get all columns of table
        String sqlStmt = "SELECT column_name, data_type"
                + " FROM information_schema.columns"
                + " WHERE table_schema = ?"
                  + " AND table_name = ?";
        PreparedStatement preparedStmt = conn.prepareStatement(sqlStmt);
        preparedStmt.setString(1, database.getName());
        preparedStmt.setString(2, name);
        ResultSet rs = preparedStmt.executeQuery();
        if (rs != null) {
            while (rs.next()) {
                String columnName = rs.getString(1);
                String columnType = rs.getString(2);

                columns.add(new MySQLColumn(columnName, columnType, this));
            }
            rs.close();
        }
        preparedStmt.close();

        // 2. Get primary key columns of table
        sqlStmt = "SELECT column_name, data_type"
                + " FROM information_schema.columns"
                + " WHERE table_schema = ?"
                  + " AND table_name = ?"
                  + " AND column_key = 'PRI'";
        preparedStmt = conn.prepareStatement(sqlStmt);
        preparedStmt.setString(1, database.getName());
        preparedStmt.setString(2, name);
        rs = preparedStmt.executeQuery();
        if (rs != null) {
            while (rs.next()) {
                String columnName = rs.getString(1);

                // Add column to the PK hashmap
                primaryKeyColumns.add(getColumn(columnName));
            }
            rs.close();
        }
        preparedStmt.close();
    }
    
    public OntModel convertSchemaToOWL(OntModel ontModel) {
        OntClass tableOntClass = ontModel.createClass(RDBNamingUtility.getClassURI(database.getName(), name));
        
        for (MySQLColumn col : columns) {
            ontModel = col.convertSchemaToOWL(ontModel);
        }
        for (MySQLForeignKey fk : foreignKeys) {
            ontModel = fk.convertSchemaToOWL(ontModel);
        }
        
        return ontModel;
    }
    
    public OntModel convertInstancesToOWL(Connection conn, OntModel ontModel) throws SQLException {
        // 1. Create instance with rdf:about from the values of all primary keys
        String sqlStmt = "SELECT " + columns.get(0).getName();
        for (int i = 1; i < columns.size(); i++) {
            sqlStmt += ", " + columns.get(i).getName();
        }
        sqlStmt += " FROM " + name;
        ///XXX ER Disabled limit because some associations doesn't work
        ///sqlStmt += " LIMIT 10";
                
        PreparedStatement preparedStmt = conn.prepareStatement(sqlStmt);
        ResultSet rs = preparedStmt.executeQuery();
        if (rs != null) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colsCount = rsmd.getColumnCount();
            while (rs.next()) {
                // First iteration cycle: Compose instance URI of PK columns values only
                // If table does not have primary keys, concatenate values of ALL columns
                String instanceUri = RDBNamingUtility.getInstanceURI(database.getName(), name);
                for (int i = 1; i <= colsCount; i++) {
                    if (primaryKeyColumns.size() != 0 
                            && getPrimaryKeyColumn(rsmd.getColumnName(i)) == null)
                        continue;
                    ///XXX ER Replaced colVal to check null values
                    //String colVal = rs.getString(i).replace(" ", "_");
                    String colVal = (rs.getString(i) == null) ? "" : rs.getString(i).replace(" ", "_");
                    instanceUri += "_" + colVal;
                }
                // Create instance
                Individual ontInst = ontModel.createIndividual(instanceUri, ontModel.getOntClass(RDBNamingUtility.getClassURI(database.getName(), name)));
                
                // 2. Second iteration cycle: Instantiate datatype properties of each column
                for (int i = 1; i <= colsCount; i++) {
                    String colName = rsmd.getColumnName(i);
                    ///XXX ER Replace next line
                    //String colVal = rs.getString(i);
                    String colVal = (rs.getString(i) == null) ? "" : rs.getString(i);
                    ontInst.addProperty(ontModel.getProperty(RDBNamingUtility.getDTPropURI(database.getName(), colName)), 
                            colVal,
                                RDBNamingUtility.getXsdDataType(getColumn(colName).getType()));
                }
            }
            rs.close();
        }
        preparedStmt.close();
        
        return ontModel;
    }
    
    public SourceMappingStruct generateSourceMapping(SourceMappingStruct sourceMappings) {
        Mapping mapping = new Mapping();
        mapping.setSource_kind("relational");
        mapping.setConnection_name(database.getName());
        mapping.setTablename(name);

        // If table doesn't have any primary key columns, add all its columns to projection attributes
        if (primaryKeyColumns.size() == 0) {
            for (MySQLColumn col : columns) {
                mapping.addProjections_attr(col.getName());
            }
        }
        else {
            for (MySQLColumn pkCol : primaryKeyColumns) {
                mapping.addProjections_attr(pkCol.getName());
            }
        }

        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.setOntology_id(RDBNamingUtility.getClassURI(database.getName(), name));
        sourceMapping.setOntology_type("concept");
        sourceMapping.setMapping(mapping);
        sourceMappings.addS_map(sourceMapping);
        
        for (MySQLColumn col : columns) {
            sourceMappings = col.generateSourceMapping(sourceMappings);
        }
        for (MySQLForeignKey fk : foreignKeys) {
            sourceMappings = fk.generateSourceMapping(sourceMappings);
        }

        return sourceMappings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<MySQLColumn> getColumns() {
        return columns;
    }
    
    public MySQLColumn getColumn(String column) {
        for (MySQLColumn col : columns) {
            if (col.getName().equals(column)) {
                return col;
            }
        }
        return null;
    }

    public void addColumn(MySQLColumn column) {
        this.columns.add(column);
    }

    public ArrayList<MySQLForeignKey> getForeignKeys() {
        return foreignKeys;
    }
    
    public MySQLForeignKey getForeignKey(String foreignKey) {
        for (MySQLForeignKey fk : foreignKeys) {
            if (fk.getName().equals(foreignKey)) {
                return fk;
            }
        }
        return null;
    }

    public void addForeignKey(MySQLForeignKey foreignKey) {
        this.foreignKeys.add(foreignKey);
    }

    public ArrayList<MySQLColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }
    
    public MySQLColumn getPrimaryKeyColumn(String column) {
        for (MySQLColumn col : primaryKeyColumns) {
            if (col.getName().equals(column)) {
                return col;
            }
        }
        return null;
    }

    public void addPrimaryKeyColumn(MySQLColumn primaryKeyColumn) {
        this.primaryKeyColumns.add(primaryKeyColumn);
    }

    public MySQLDatabase getDatabase() {
        return database;
    }

    public String getTempInstanceURI() {
        return tempInstanceURI;
    }
}
