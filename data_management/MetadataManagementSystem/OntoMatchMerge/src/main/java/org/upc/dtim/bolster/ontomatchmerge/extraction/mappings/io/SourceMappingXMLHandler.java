package org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.io;

import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Connection;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Selection;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMapping;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.SourceMappingStruct;
import org.upc.dtim.bolster.ontomatchmerge.extraction.mappings.Mapping;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Petar
 * 
 * The class for reading and storing the contents of an input source mappings (XML file).
 *
 */

class Flags{
    public boolean onm = false;
    public boolean ont = false;
    public boolean onr = false;
    public boolean map = false;
    public boolean tbn = false;
    public boolean att = false;
    public boolean sel = false;
    public boolean col = false;
    public boolean opr = false;
    public boolean cos = false;
    public boolean sqo = false;
    public boolean con = false;
    public boolean cnn = false;
    public boolean cnt = false;
    public boolean cns = false;
    public boolean cna = false;
    public boolean cnd = false;
    public boolean cnp = false;
    public boolean cnu = false;
    public boolean cpw = false;

}

public class SourceMappingXMLHandler extends DefaultHandler {
    int t=0;

    private Flags flags = new Flags();
    private SourceMappingStruct xml_sm_input;
    private SourceMapping s_temp;
    private Selection sl_temp;
    private Connection connection;
    private Mapping map_temp;
    private int map_depth = 0;
    private int map_position = 0;
    
    
    private String currAttr = "";




    @Override
     public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {


       if (qName.equalsIgnoreCase("connection")){
           connection = new Connection();
           flags.con = true;
       }      
       else if(qName.equalsIgnoreCase("OntologyMapping")){
           flags.onm = true;
           s_temp = new SourceMapping();
                   
       }
       else if (qName.equalsIgnoreCase("Ontology")){
           flags.ont = true;
           s_temp.setOntology_type(atts.getValue("type"));
       }
       else if (qName.equalsIgnoreCase("Mapping")){
           flags.map = true;
           map_depth++;
           // depth 1....n
           // position 0....m

           if (map_depth == 1) {
               map_temp = new Mapping();
               if (atts.getValue("connectionName") != null && !atts.getValue("connectionName").equalsIgnoreCase(""))
                   map_temp.setConnection_name(atts.getValue("connectionName"));
               if (atts.getValue("sourceKind") != null && !atts.getValue("sourceKind").equalsIgnoreCase(""))
                   map_temp.setSource_kind(atts.getValue("sourceKind"));
               s_temp.setMapping(map_temp);
               map_position = 0;

           }
           else {
               Mapping m = map_temp;
               for (int i=1 ; i<map_depth-1 ; i++)
                   m = m.getMappings().get(m.getMappings().size()-1);

               Mapping map_temp_sub = new Mapping();
               map_temp_sub.setConnection_name(atts.getValue("connectionName"));
               map_temp.setSource_kind(atts.getValue("sourceKind"));
               m.addMapping(map_temp_sub);
               map_position = m.getMappings().size()-1;
           }

       }
       else if (qName.equalsIgnoreCase("Tablename")) flags.tbn = true;
       else if (qName.equalsIgnoreCase("Attribute")) flags.att = true;
       else if (qName.equalsIgnoreCase("Selection")) flags.sel = true;
       else if (qName.equalsIgnoreCase("Column")) flags.col = true;
       else if (qName.equalsIgnoreCase("Operator")) flags.opr = true;
       else if (qName.equalsIgnoreCase("Constant")) flags.cos = true;
       else if (qName.equalsIgnoreCase("SQLOperator")) flags.sqo = true;

       else if (qName.equalsIgnoreCase("name") && flags.con) flags.cnn = true;
       else if (qName.equalsIgnoreCase("type") && flags.con) flags.cnt = true;
       else if (qName.equalsIgnoreCase("server") && flags.con) flags.cns = true;
       else if (qName.equalsIgnoreCase("access") && flags.con) flags.cna = true;
       else if (qName.equalsIgnoreCase("database") && flags.con) flags.cnd = true;
       else if (qName.equalsIgnoreCase("port") && flags.con) flags.cnp = true;
       else if (qName.equalsIgnoreCase("username") && flags.con) flags.cnu = true;
       else if (qName.equalsIgnoreCase("password") && flags.con) flags.cpw = true;



   }

    @Override
   public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

      if (qName.equalsIgnoreCase("connection")){
           xml_sm_input.addConnection(connection);
           flags.con = false;
      }
      else if(qName.equalsIgnoreCase("OntologyMapping")){
          flags.onm = false;
          xml_sm_input.addS_map(s_temp);
      }
      else if (qName.equalsIgnoreCase("Ontology"))  flags.ont = false;
      else if (qName.equalsIgnoreCase("RefOntology"))  flags.onr = false;
      else if (qName.equalsIgnoreCase("Mapping")){
         map_depth--;
      }
      else if (qName.equalsIgnoreCase("Tablename")) flags.tbn = false;
      else if (qName.equalsIgnoreCase("Attribute")){
          flags.att = false;
          
          Mapping m = map_temp;
          for (int i=1 ; i<map_depth ; i++)
                m = m.getMappings().get(m.getMappings().size()-1);              

          m.addProjections_attr(currAttr);
          currAttr = "";
      }
      else if (qName.equalsIgnoreCase("Selection")){
          flags.sel = false;

          Mapping m = map_temp;
          for (int i=1 ; i<map_depth ; i++)
              m = m.getMappings().get(m.getMappings().size()-1);
          
          m.addSelection(sl_temp);

          sl_temp = null;
      }

      else if (qName.equalsIgnoreCase("Column")) flags.col = false;
      else if (qName.equalsIgnoreCase("Operator")) flags.opr = false;
      else if (qName.equalsIgnoreCase("Constant")) flags.cos = false;
      else if (qName.equalsIgnoreCase("SQLOperator")) flags.sqo = false;

      else if (qName.equalsIgnoreCase("name") && flags.con) flags.cnn = false;
      else if (qName.equalsIgnoreCase("type") && flags.con) flags.cnt = false;
      else if (qName.equalsIgnoreCase("server") && flags.con) flags.cns = false;
      else if (qName.equalsIgnoreCase("access") && flags.con) flags.cna = false;
      else if (qName.equalsIgnoreCase("database") && flags.con) flags.cnd = false;
      else if (qName.equalsIgnoreCase("port") && flags.con) flags.cnp = false;
      else if (qName.equalsIgnoreCase("username") && flags.con) flags.cnu = false;
      else if (qName.equalsIgnoreCase("password") && flags.con) flags.cpw = false;


   }

    @Override
   public void characters(char[] ch, int start, int length)  throws SAXException{
        if (flags.con && flags.cnn) connection.setName(String.valueOf(ch, start, length));
        else if(flags.con && flags.cnt) connection.setType(String.valueOf(ch, start, length));
        else if(flags.con && flags.cnd) connection.setDatabase(String.valueOf(ch, start, length));
        else if(flags.con && flags.cnp) connection.setPort(String.valueOf(ch, start, length));
        else if(flags.con && flags.cns) connection.setServer(String.valueOf(ch, start, length));
        else if(flags.con && flags.cnu) connection.setUsername(String.valueOf(ch, start, length));
        else if(flags.con && flags.cpw) connection.setPassword(String.valueOf(ch, start, length));
        else if(flags.ont)
        {
            s_temp.setOntology_id(String.valueOf(ch, start, length));
        }    
        else if (flags.tbn){

            Mapping m = map_temp;
            for (int i=1 ; i<map_depth ; i++)
                m = m.getMappings().get(m.getMappings().size()-1);

            m.setTablename(String.valueOf(ch, start, length));            
        }
        else if (flags.att){

//            Mapping m = map_temp;
//            for (int i=1 ; i<map_depth ; i++)
//                m = m.getMappings().get(m.getMappings().size()-1);              
//
//            m.addProjections_attr(String.valueOf(ch, start, length));
            
            currAttr += String.valueOf(ch, start, length);
        }
        else if (flags.col){
            if (sl_temp == null) sl_temp = new Selection();
            sl_temp.column = String.valueOf(ch, start, length);
        }
        else if (flags.opr){
            if (sl_temp == null) sl_temp = new Selection();
            sl_temp.operator = String.valueOf(ch, start, length);
        }
        else if (flags.cos){
            if (sl_temp == null) sl_temp = new Selection();
            sl_temp.constant = String.valueOf(ch, start, length);
        }
        else if (flags.sqo){
            
            Mapping m = map_temp;
            for (int i=1 ; i<map_depth ; i++)
                m = m.getMappings().get(m.getMappings().size()-1);
            
            m.addOperator(String.valueOf(ch, start, length));
        }


   }

    @Override
   public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException{
       System.out.print("");
   }

    @Override
   public void endDocument() throws SAXException{
       //System.out.println("END_source_mapping");
   }



    @Override
   public void startDocument() throws SAXException{
       //System.out.println("START_source_mapping");
   }


   public void printTabs(int n){
       for (int i=0 ; i<n ; i++)
           System.out.print("  ");
   }

    @Override
      public void warning(SAXParseException e) throws SAXException {
         System.err.println("Warning: ");
         printInfo(e);
         throw new SAXException("Warning");
      }
    @Override
      public void error(SAXParseException e) throws SAXException {
         System.err.println("Error: ");
         printInfo(e);
         throw new SAXException("Error");
      }
    @Override
      public void fatalError(SAXParseException e) throws SAXException {
         System.err.println("Fattal error: ");
         printInfo(e);
         throw new SAXException("Fattal error");
      }
      private void printInfo(SAXParseException e) {
         System.err.println("   Public ID: "+e.getPublicId());
         System.err.println("   System ID: "+e.getSystemId());
         System.err.println("   Line number: "+e.getLineNumber());
         System.err.println("   Column number: "+e.getColumnNumber());
         System.err.println("   Message: "+e.getMessage());
      }

    /**
     * @return the xml_sm_input
     */
    public SourceMappingStruct getXml_sm_input() {
        return xml_sm_input;
    }

    /**
     * @param xml_sm_input the xml_sm_input to set
     */
    public void setXml_sm_input(SourceMappingStruct xml_sm_input) {
        this.xml_sm_input = xml_sm_input;
    }



}



