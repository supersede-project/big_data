/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching.dummy;

import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import java.util.Properties;
import java.util.Set;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

/**
 *
 * @author Rizkallah
 */
public class MyEDOALAlignmentProcess extends EDOALAlignment implements AlignmentProcess  {
    private LoadedOntology<Object> ontology1;
    private LoadedOntology<Object> ontology2;
    
    public MyEDOALAlignmentProcess() {

    }

    /**
     * The only method to implement is align.
     * All the resources for reading the ontologies and rendering the alignment are from ObjectAlignment and its superclasses:
     * - this.ontology1 and this.ontology2 returns objects LoadedOntology
     * - addAlignCell adds a new mapping in the alignment object  
     */
    public void align( Alignment alignment, Properties param ) throws AlignmentException {
        this.ontology1 = (LoadedOntology<Object>)onto1;
        this.ontology2 = (LoadedOntology<Object>)onto2;
	MyMatcher matcher = new MyMatcher(this.ontology1, this.ontology2);

        try {
	    // Match classes
            Set<Object> onto1Classes = (Set<Object>)this.ontology1.getClasses();
            Set<Object> onto2Classes = (Set<Object>)this.ontology2.getClasses();
	    for ( Object cl2: onto2Classes ){
                Expression exp2 = new ClassId(this.ontology2.getEntityURI(cl2));
		for ( Object cl1: onto1Classes ){
		    // add mapping into alignment object 
                    Expression exp1 = new ClassId(this.ontology1.getEntityURI(cl1));
		    addAlignCell(exp1, exp2, "=", matcher.match(cl1, cl2));
		}
	    }

	    // Match dataProperties
            Set<Object> onto1DataProps = (Set<Object>)this.ontology1.getDataProperties();
            Set<Object> onto2DataProps = (Set<Object>)this.ontology2.getDataProperties();
	    for ( Object p2: onto2DataProps ){
                Expression exp2 = new PropertyId(this.ontology2.getEntityURI(p2));
		for ( Object p1: onto1DataProps ){
		    // add mapping into alignment object 
                    Expression exp1 = new PropertyId(this.ontology1.getEntityURI(p1));
		    addAlignCell(exp1, exp2, "=", matcher.match(p1, p2));    
		}
	    }

	    // Match objectProperties
            Set<Object> onto1ObjProps = (Set<Object>)this.ontology1.getObjectProperties();
            Set<Object> onto2ObjProps = (Set<Object>)this.ontology2.getObjectProperties();
	    for ( Object p2: onto2ObjProps ){
                Expression exp2 = new RelationId(this.ontology2.getEntityURI(p2));
		for ( Object p1: onto1ObjProps ){
		    // add mapping into alignment object 
                    Expression exp1 = new RelationId(this.ontology1.getEntityURI(p1));
		    addAlignCell(exp1, exp2, "=", matcher.match(p1, p2));    
		}
	    }
	} catch (Exception e) { e.printStackTrace(); }
    }
}
