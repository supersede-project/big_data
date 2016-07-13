/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.merging;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.OWLIOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.main.OntologyConstructionCoordinator;

/**
 *
 * @author Rizkallah
 */
public abstract class OntologyMergingCoordinator {

    public OntologyMergingCoordinator() {
        
    }
    
    public static void mergeOntologies(IOArtifact source1, IOArtifact source2, String alignmentsPath, OWLIOArtifact owlOutput) {
        try {
            URI onto1Uri = new File(source1.getOutputOntologyPath()).toURI();
            URI onto2Uri = new File(source2.getOutputOntologyPath()).toURI();

            // Merge ontologies using input alignments and output merged ontology
            MergingProcess ontMerger = new MergingProcess(onto1Uri, onto2Uri, source1.getSourceMappingsPath(), source2.getSourceMappingsPath());
            ontMerger.mergeOntologiesForQuarry(alignmentsPath);

            // Write out the merged ontology model and merged source mappings file
            ontMerger.writeMergedOntologyModel(new FileOutputStream(
                                                    new File(owlOutput.getOutputOntologyPath())), 
                                            OntologyConstructionCoordinator.ONTOLOGY_SERIALIZATION_FORMAT);
            ontMerger.writeSourceMappings(new FileOutputStream(new File(owlOutput.getSourceMappingsPath())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
