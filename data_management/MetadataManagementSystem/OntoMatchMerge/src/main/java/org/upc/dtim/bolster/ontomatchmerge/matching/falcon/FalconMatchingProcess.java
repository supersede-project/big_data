/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching.falcon;

import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import nju.websoft.falcon.model.Node;
import nju.websoft.falcon.output.Mapping;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.upc.dtim.bolster.ontomatchmerge.matching.MatchingProcess;

/**
 *
 * @author Rizkallah
 */
public abstract class FalconMatchingProcess extends MatchingProcess {

    private nju.websoft.falcon.output.Alignment falconOutput;

    public FalconMatchingProcess() {

    }

    @Override
    public void init( Object onto1, Object onto2 ) throws AlignmentException {
        // Initialize Alignment API
        super.init(onto1, onto2);
    }

    /**
     * The only method to implement is align.
     * All the resources for reading the ontologies and rendering the alignment are from ObjectAlignment and its superclasses:
     * - this.ontology1 and this.ontology2 returns objects LoadedOntology
     * - addAlignCell adds a new mapping in the alignment object  
     */
    @Override
    public void align(Alignment alignment, Properties param) throws AlignmentException {
        
    }

    @Override
    protected void convertOutputToEDOAL () throws AlignmentException {
        // Convert matcher output to EDOAL
        for (int i = 0; i < falconOutput.size(); i++) {
            Mapping falconMapping = falconOutput.getMapping(i);
            String entity1Name = falconMapping.getEntity1().getNameSpace() + falconMapping.getEntity1().getLocalName();
            String entity2Name = falconMapping.getEntity2().getNameSpace() + falconMapping.getEntity2().getLocalName();
            Expression exp1 = null;
            Expression exp2 = null;
            
            // Initialize AlignmentAPI expression based on Falcon-AO entity category
            int entityCategory = falconMapping.getEntity1().getCategory();
            try {
            switch (entityCategory) {
                case Node.CLASS:    exp1 = new ClassId(new URI(entity1Name));
                                    exp2 = new ClassId(new URI(entity2Name));
                                    break;
                case Node.DATATYPEPROPERTY: exp1 = new PropertyId(new URI(entity1Name));
                                            exp2 = new PropertyId(new URI(entity2Name));
                                            break;
                case Node.OBJECTPROPERTY: exp1 = new RelationId(new URI(entity1Name));
                                          exp2 = new RelationId(new URI(entity2Name));
                                          break;
                case Node.PROPERTY: exp1 = new PropertyId(new URI(entity1Name));
                                    exp2 = new PropertyId(new URI(entity2Name));
                                    break;
                case Node.INSTANCE: exp1 = new InstanceId(new URI(entity1Name));
                                    exp2 = new InstanceId(new URI(entity2Name));
                                    break;
            }
            } catch (URISyntaxException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            
            // Addd EDOAL alignment cell to AlignmentAPI
            addAlignCell(exp1, exp2, falconMapping.getRelation(), falconMapping.getSimilarity());
            if (falconMapping.getRelation().equals("=")) {
                addAlignCell(exp2, exp1, falconMapping.getRelation(), falconMapping.getSimilarity());
            }
        }
    }

    public nju.websoft.falcon.output.Alignment getFalconOutput() {
        return falconOutput;
    }

    public void setFalconOutput(nju.websoft.falcon.output.Alignment falconOutput) {
        this.falconOutput = falconOutput;
    }
}
