package org.upc.dtim.bolster.ontomatchmerge.extraction.mappings;

/**
 *
 * @author Petar
 *
 * The class that represents the selections inside the source mappings (if the mapping is derived from the selection)
 *
 */
public class Selection{
        public String column;
        public String operator;
        public String constant;
        public Selection (String column, String operator, String constant){
            this.column = column;
            this.constant = constant;
            this.operator = operator;
        }
        public Selection (){      }
    }