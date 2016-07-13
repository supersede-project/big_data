/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching.falcon;

import java.net.URI;
import java.util.Properties;
import nju.websoft.falcon.cc.Controller;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

/**
 *
 * @author Rizkallah
 */
public class FalconCombinedMatchingProcess extends FalconMatchingProcess implements AlignmentProcess   {

    private Controller falconController;

    public FalconCombinedMatchingProcess() {
        super();
    }

    @Override
    public void init( Object onto1, Object onto2 ) throws AlignmentException {
        // Initialize AlignmentAPI
        super.init(onto1, onto2);
        
        // Initialize Falcon-AO
        this.falconController = new Controller(((URI)onto1).toString(), ((URI)onto2).toString());
    }

    @Override
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        setFalconOutput(this.falconController.run());
    }
}
