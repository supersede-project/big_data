package org.upc.dtim.bolster.ontomatchmerge.extraction.mappings;

import java.util.ArrayList;

/**
 *
 * @author Petar
 *
 * The class that stores the content of an input source mapping file.
 *
 */
public class SourceMappingStruct{

    private ArrayList<SourceMapping> s_map; // source mappings
    private ArrayList<Connection> connections; // database connections

    /**
     * @return the s_map
     */


    public SourceMappingStruct(){}
    
    public ArrayList<SourceMapping> getS_map() {
        return s_map;
    }

    /**
     * @param s_map the s_map to set
     */
    public void setS_map(ArrayList<SourceMapping> s_map) {
        this.s_map = s_map;
    }


    public void addS_map(SourceMapping sm){
        if (s_map == null)
            s_map = new ArrayList<SourceMapping>();
        s_map.add(sm);
    }

    public SourceMapping findSM(String id, String ont_type){
        for (int i=0 ; i<s_map.size() ; i++){
            if (s_map.get(i).getOntology_id().equalsIgnoreCase(id) && s_map.get(i).getOntology_type().equalsIgnoreCase(ont_type))
                return s_map.get(i);
        }

        return null;
    }


    @Override
    public SourceMappingStruct clone(){

        ArrayList<Connection> connectionsc = new ArrayList<Connection>();


        SourceMappingStruct sms = new SourceMappingStruct();

        ArrayList<SourceMapping> smList = new ArrayList<SourceMapping>();

        for (int i=0 ; i<this.s_map.size() ; i++) smList.add(this.s_map.get(i));
        if (connections != null)
            for (int i=0 ; i<this.connections.size() ; i++) connectionsc.add(this.connections.get(i));


        sms.setS_map(smList);
        sms.setConnections(connectionsc);

        return sms;

    }

    /**
     * @return the connections
     */
    public ArrayList<Connection> getConnections() {
        return connections;
    }

    /**
     * @param connections the connections to set
     */
    public void setConnections(ArrayList<Connection> connections) {
        this.connections = connections;
    }

     /**
     * @param connection the connection to add
     */
    public void addConnection(Connection connection) {
        if (this.connections == null) this.connections = new ArrayList<Connection>();
        this.connections.add(connection);
    }
}
