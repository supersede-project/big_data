/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import java.net.URI;
import java.util.Properties;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

/**
 *
 * @author Rizkallah
 */
public abstract class MatchingProcess  extends EDOALAlignment implements AlignmentProcess {
    
    public MatchingProcess() {
        
    }
    
    public final void run(URI onto1, URI onto2) throws AlignmentException {
        init (onto1, onto2);
        align((Alignment)null, new Properties());
        convertOutputToEDOAL();
    }

    @Override
    public void init( Object onto1, Object onto2 ) throws AlignmentException {
        // Initialize Alignment API
        super.init(onto1, onto2);
    }
    
    /**
     * The only methods to implement are align and convertOutputToEDOAL.
     * All the resources for reading the ontologies and rendering the alignment are from ObjectAlignment and its superclasses:
     * - this.ontology1 and this.ontology2 returns objects LoadedOntology
     * - addAlignCell adds a new mapping in the alignment object  
     */
    @Override
    public abstract void align(Alignment alignment, Properties param) throws AlignmentException;
    protected abstract void convertOutputToEDOAL() throws AlignmentException;

}
