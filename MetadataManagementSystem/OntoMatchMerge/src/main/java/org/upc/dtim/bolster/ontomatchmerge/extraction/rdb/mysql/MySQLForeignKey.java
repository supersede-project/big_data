/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.mysql;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Mapping;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMapping;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.rdb.RDBNamingUtility;

/**
 *
 * @author Rizkallah
 */
public class MySQLForeignKey {
    
    private String name;
    private MySQLTable table;
    private ArrayList<MySQLColumn> columns;
    private MySQLTable referencedTable;
    private ArrayList<MySQLColumn> referencedColumns;
    
    public MySQLForeignKey () {
        this.name = "";
        this.table = null;
        this.columns = new ArrayList();
        this.referencedTable = new MySQLTable();
        this.referencedColumns = new ArrayList();
    }
    
    public MySQLForeignKey(String name, MySQLTable table) {
        this.name = name;
        this.table = table;
        this.columns = new ArrayList();
        this.referencedTable = new MySQLTable();
        this.referencedColumns = new ArrayList();
    }

    public void loadDatabaseSchema(Connection conn) throws SQLException {
        
    }
    
    public OntModel convertSchemaToOWL(OntModel ontModel) {
        // Create domain and range resources
        Resource propDomain = ontModel.createResource(RDBNamingUtility.getClassURI(table.getDatabase().getName(), table.getName()));
        Resource propRange = ontModel.createResource(RDBNamingUtility.getClassURI(table.getDatabase().getName(), referencedTable.getName()));
        
        // Create ObjectProperty corresponding to FK
        ObjectProperty objProp = ontModel.createObjectProperty(RDBNamingUtility.getClassURI(table.getDatabase().getName(), name));
        objProp.addDomain(propDomain);
        objProp.addRange(propRange);
        
        // Add OWL Cardinality Restriction to the FK
        ontModel.createCardinalityRestriction(null, objProp, Integer.MAX_VALUE).addSubClass(propDomain);
        ontModel.createCardinalityRestriction(null, objProp, 1).addSubClass(propRange);
        return ontModel;
    }
    
    public OntModel convertInstancesToOWL(Connection conn, OntModel ontModel) throws SQLException {
        // Add all PK columns of referencing table to SELECT
        // If table doesn't have PKs, add all columns
        ArrayList<MySQLColumn> selectList = new ArrayList();
        if (table.getPrimaryKeyColumns().isEmpty())
            selectList.addAll(table.getColumns());
        else
            selectList.addAll(table.getPrimaryKeyColumns());
        
        int referencingTableColsCount = selectList.size();
        String sqlStmt = "SELECT " + table.getName() + "." + selectList.get(0).getName();
        for (int i = 1; i < selectList.size(); i++) {
            sqlStmt += ", " + table.getName() + "." + selectList.get(i).getName();
        }

        // Add all PK columns of referenced table to SELECT
        // If table doesn't have PKs, add all columns
        selectList = new ArrayList();
        if (referencedTable.getPrimaryKeyColumns().isEmpty())
            selectList.addAll(referencedTable.getColumns());
        else
            selectList.addAll(referencedTable.getPrimaryKeyColumns());

        for (int i = 0; i < selectList.size(); i++) {
            sqlStmt += ", " + referencedTable.getName() + "." + selectList.get(i).getName();
        }
        int referencedTableColsCount = selectList.size();

        // Add FROM clause
        sqlStmt += " FROM " + table.getName() + ", " + referencedTable.getName();
        
        // Add WHERE clauses
        sqlStmt += " WHERE " + table.getName() + "." + columns.get(0).getName() 
                + "=" + referencedTable.getName() + "." + referencedColumns.get(0).getName();
        for (int i = 1; i < columns.size(); i++) {
            sqlStmt += " AND " + table.getName() + "." + columns.get(i).getName() 
                    + "=" + referencedTable.getName() + "." + referencedColumns.get(i).getName();
        }

        PreparedStatement preparedStmt = conn.prepareStatement(sqlStmt);
        ResultSet rs = preparedStmt.executeQuery();
        if (rs != null) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colsCount = rsmd.getColumnCount();
            while (rs.next()) {
                // Get instance URI of referencing table row
                String referencingInstanceUri = RDBNamingUtility.getInstanceURI(table.getDatabase().getName(), table.getName());
                for (int i = 1; i <= referencingTableColsCount; i++) {
                    ///XXX ER Replaced to check null values
                    //String colVal = rs.getString(i);
                    String colVal = (rs.getString(i) == null) ? "" : rs.getString(i);
                    referencingInstanceUri += "_" + colVal.replace(" ", "_");
                }
                
                // Get instance URI of referenced table row
                String referencedInstanceUri = RDBNamingUtility.getInstanceURI(referencedTable.getDatabase().getName(), referencedTable.getName());
                for (int i = referencingTableColsCount + 1; i <= colsCount; i++) {
                    ///XXX ER Replaced to check null values
                    //String colVal = rs.getString(i);
                    String colVal = (rs.getString(i) == null) ? "" : rs.getString(i);
                    referencedInstanceUri += "_" + colVal.replace(" ", "_");
                }

                Individual referencingInst = ontModel.getIndividual(referencingInstanceUri);
                Individual referencedInst = ontModel.getIndividual(referencedInstanceUri);
                ObjectProperty fkObjProp = ontModel.getObjectProperty(RDBNamingUtility.getClassURI(table.getDatabase().getName(), name));
                referencingInst.addProperty(fkObjProp, referencedInst);
            }
            rs.close();
        }
        preparedStmt.close();

        return ontModel;
    }
    
    public SourceMappingStruct generateSourceMapping(SourceMappingStruct sourceMappings) {
        Mapping mapping = new Mapping();
        mapping.setSource_kind("relational");
        mapping.setConnection_name(table.getDatabase().getName());
        mapping.setTablename(table.getName());

        for (MySQLColumn fkCol : columns) {
            mapping.addProjections_attr(fkCol.getName());
        }

        SourceMapping sourceMapping = new SourceMapping();
        sourceMapping.setOntology_id(RDBNamingUtility.getClassURI(table.getDatabase().getName(), name));
        sourceMapping.setOntology_type("property");
        sourceMapping.setMapping(mapping);
        sourceMappings.addS_map(sourceMapping);
        return sourceMappings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MySQLTable getReferencedTable() {
        return referencedTable;
    }

    public void setReferencedTable(MySQLTable referencedTable) {
        this.referencedTable = referencedTable;
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

    public ArrayList<MySQLColumn> getReferencedColumns() {
        return referencedColumns;
    }
    
    public MySQLColumn getReferencedColumn(String column) {
        for (MySQLColumn referencedColumn : referencedColumns) {
            if (referencedColumn.getName().equals(column)) {
                return referencedColumn;
            }
        }
        return null;
    }

    public void addReferencedColumn(MySQLColumn referencedColumn) {
        this.referencedColumns.add(referencedColumn);
    }

    public MySQLTable getTable() {
        return table;
    }
}
