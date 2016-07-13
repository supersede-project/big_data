/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching.dummy;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import org.semanticweb.owl.align.AlignmentException;

/**
 *
 * @author Rizkallah
 */
public class MyMatcher {
    
    private LoadedOntology<Object> ontology1;
    private LoadedOntology<Object> ontology2;
    private ObjectAlignment alignmentProcess;
    
    public MyMatcher(LoadedOntology<Object> onto1, LoadedOntology<Object> onto2) {
        this.ontology1 = onto1;
        this.ontology2 = onto2;
    }

    /*
    * *Very* simple matcher, based on equality of names (in the example, only classes and properties)
    */
    public double match(Object o1, Object o2) throws AlignmentException {
	try {
	    String s1 = ontology1.getEntityName(o1);
	    String s2 = ontology2.getEntityName(o2);
	    if (s1 == null || s2 == null) return 0.;
	    if (s1.toLowerCase().equals(s2.toLowerCase())) { 
		return 1.0;
	    } else { 
		return 0.;
	    }
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error getting entity name", owex );
	}
    }
}
