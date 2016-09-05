/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.mysql;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.AbstractDatabase;
import com.hp.hpl.jena.ontology.OntModel;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Rizkallah
 */
public class MySQLDatabase extends AbstractDatabase {

    private ArrayList<MySQLTable> tables;

    public MySQLDatabase() {
        super();
        this.tables = new ArrayList();
    }
    
    public MySQLDatabase(String name, String type, String server, String username, String password) {
        super(name, type, server, username, password);
        getDatabaseConnection();
        this.tables = new ArrayList();
    }
    
    @Override
    public void getDatabaseConnection() {
        try {
            String driver = "com.mysql.jdbc.Driver";
            String dbUrl = "jdbc:mysql://" + getServer() + "/" + getName();
            
            Class.forName(driver);
            setConn(DriverManager.getConnection(dbUrl, getUsername(), getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void loadDatabaseSchema() throws SQLException {
        // 1. Get tables in database
        String sqlStmt = "SELECT table_name"
                + " FROM information_schema.tables"
                + " WHERE table_schema = ?";
        PreparedStatement preparedStmt = getConn().prepareCall(sqlStmt);
        preparedStmt.setString(1, getName());
        ResultSet rs = preparedStmt.executeQuery();
        if (rs != null) {
            while (rs.next()) {
                String tableName = rs.getString(1);
                tables.add(new MySQLTable(tableName, this));
            }
            rs.close();
        }
        preparedStmt.close();
        
        // 2. Call loadDatabaseSchema from all tables
        for (MySQLTable table : tables) {
            table.loadDatabaseSchema(getConn());
        }
        
        // 3. Get foreign key constraints in database
        sqlStmt = "SELECT table_name, column_name, constraint_name, referenced_table_name, referenced_column_name"
                + " FROM information_schema.key_column_usage"
                + " WHERE table_schema = ?"
                  + " AND referenced_table_name IS NOT NULL";
        preparedStmt = getConn().prepareStatement(sqlStmt);
        preparedStmt.setString(1, getName());
        rs = preparedStmt.executeQuery();
        if (rs != null) {
            while (rs.next()) {
                String tableName = rs.getString(1);
                String columnName = rs.getString(2);
                String fkConstraintName = rs.getString(3);
                String referencedTableName = rs.getString(4);
                String referencedColumnName = rs.getString(5);
                if (fkConstraintName == null || fkConstraintName.equals(""))
                    fkConstraintName = tableName + "_to_" + referencedTableName;

                if (getTable(tableName).getForeignKey(fkConstraintName) == null)
                    getTable(tableName).addForeignKey(new MySQLForeignKey(fkConstraintName, getTable(tableName)));

                getTable(tableName).getForeignKey(fkConstraintName).addColumn(getTable(tableName).getColumn(columnName));
                getTable(tableName).getForeignKey(fkConstraintName).setReferencedTable(getTable(referencedTableName));
                getTable(tableName).getForeignKey(fkConstraintName).addReferencedColumn(getTable(referencedTableName).getColumn(referencedColumnName));
            }
            rs.close();
        }
        preparedStmt.close();
    }

    @Override
    public OntModel convertSchemaToOWL(OntModel ontModel) {
        for (MySQLTable table : tables) {
            ontModel = table.convertSchemaToOWL(ontModel);
        }

        return ontModel;
    }
    
    @Override
    public OntModel convertInstancesToOWL(OntModel ontModel) throws SQLException {
        // 1. Create instances for each table
        for (MySQLTable table : tables) {
            ontModel = table.convertInstancesToOWL(getConn(), ontModel);
        }

        // 3. Modify instances with object properties extracted from FK constraints
        for (MySQLTable table : tables) {
            for (MySQLForeignKey fkConst : table.getForeignKeys()) {
                ontModel = fkConst.convertInstancesToOWL(getConn(), ontModel);
            }
        }

        return ontModel;
    }
    
    @Override
    public SourceMappingStruct generateSourceMapping(SourceMappingStruct sourceMappings) {
        org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Connection mappingsCon = new org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Connection();
        mappingsCon.setName(getName());
        mappingsCon.setServer(getServer());
        mappingsCon.setType(getType());
        mappingsCon.setDatabase(getName());
        mappingsCon.setUsername(getUsername());
        mappingsCon.setPassword(getPassword());
        
        sourceMappings.addConnection(mappingsCon);
        
        for (MySQLTable table : tables) {
            sourceMappings = table.generateSourceMapping(sourceMappings);
        }

        return sourceMappings;
    }

    public ArrayList<MySQLTable> getTables() {
        return tables;
    }

    public MySQLTable getTable(String table) {
        for (MySQLTable tb : tables) {
            if (tb.getName().equals(table)) {
                return tb;
            }
        }
        return null;
    }

    public void addTable(MySQLTable table) {
        this.tables.add(table);
    }
}
