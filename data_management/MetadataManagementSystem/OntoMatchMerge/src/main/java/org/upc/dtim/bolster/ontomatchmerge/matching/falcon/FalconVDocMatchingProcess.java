/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching.falcon;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.net.URI;
import java.util.Properties;
import nju.websoft.falcon.matcher.AbstractMatcher;
import nju.websoft.falcon.matcher.vdoc.VDocMatcher;
import nju.websoft.falcon.model.RBGModel;
import nju.websoft.falcon.model.RBGModelFactory;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

/**
 *
 * @author Rizkallah
 */
public class FalconVDocMatchingProcess extends FalconMatchingProcess implements AlignmentProcess  {
    
    private AbstractMatcher falconMatcher;

    public FalconVDocMatchingProcess() {
        super();
    }

    @Override
    public void init( Object onto1, Object onto2 ) throws AlignmentException {
        // Initialize AlignmentAPI
        super.init(onto1, onto2);
        
        // Initialize Falcon-AO
        OntDocumentManager mgr = new OntDocumentManager();
        mgr.setProcessImports(false);
        OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
        spec.setDocumentManager(mgr);

        OntModel ontModel1 = ModelFactory.createOntologyModel(spec, null);
        ontModel1.read(((URI)onto1).toString());
        OntModel ontModel2 = ModelFactory.createOntologyModel(spec, null);
        ontModel2.read(((URI)onto2).toString());

        RBGModel rbgmString1 = RBGModelFactory.createModel("VDOC_MODEL");
        rbgmString1.setOntModel(ontModel1);
        RBGModel rbgmString2 = RBGModelFactory.createModel("VDOC_MODEL");
        rbgmString2.setOntModel(ontModel2);
        this.falconMatcher = new VDocMatcher(rbgmString1, rbgmString2);
    }

    @Override
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        this.falconMatcher.match();
        setFalconOutput(this.falconMatcher.getAlignment());
    }
}
