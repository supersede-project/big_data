/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.matching;

// Alignment API classes
import org.semanticweb.owl.align.AlignmentVisitor;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

// Java standard classes
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import org.upc.dtim.bolster.ontomatchmerge.main.OntologyConstructionCoordinator;
import org.upc.dtim.bolster.ontomatchmerge.matching.falcon.FalconISubMatchingProcess;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.OWLIOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.matching.falcon.FalconCombinedMatchingProcess;
import org.upc.dtim.bolster.ontomatchmerge.matching.falcon.FalconVDocMatchingProcess;
import org.upc.dtim.bolster.ontomatchmerge.merging.MergingProcess;

/**
 *
 * @author Rizkallah
 */
public abstract class OntologyMatchingCoordinator {
    
    public static final String FALCON_ISUB_MATCHER = "falcon_isub_matcher";
    public static final String FALCON_VDOC_MATCHER = "falcon_vdoc_matcher";
    public static final String FALCON_COMBINED_MATCHER = "falcon_combined_matcher";

    public static final double CUTOFF_THRESHOLD = 0.75;

    public OntologyMatchingCoordinator() {
        
    }

    public static void matchOntologies(String matcherName, IOArtifact source1, IOArtifact source2, 
                String alignmentsPath, OWLIOArtifact output) {

	try {
            URI onto1Uri = new File(source1.getOutputOntologyPath()).toURI();
            URI onto2Uri = new File(source2.getOutputOntologyPath()).toURI();
            MatchingProcess ap = null;
            if (matcherName.equals(FALCON_VDOC_MATCHER))
                ap = new FalconVDocMatchingProcess();
            else if (matcherName.equals(FALCON_COMBINED_MATCHER))
                ap = new FalconCombinedMatchingProcess();
            else    // Default matcher is Falcon I-Sub
                ap = new FalconISubMatchingProcess();

            // 1. Match Ontologies and output alignments
            ap.run(onto1Uri, onto2Uri);
            // Trim at a certain threshold
            ap.cut(OntologyMatchingCoordinator.CUTOFF_THRESHOLD);

            // Output alignments as OWL rules
            PrintWriter writer = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(alignmentsPath), "UTF-8")), true);
            AlignmentVisitor renderer = new RDFRendererVisitor(writer);
            ap.render(renderer);
            writer.flush();
            writer.close();

            // 2. Merge ontologies using resulting alignments and output merged ontology
            MergingProcess ontMerger = new MergingProcess(onto1Uri, onto2Uri, source1.getSourceMappingsPath(), source2.getSourceMappingsPath());
            ontMerger.mergeOntologies(ap);

            // Write out the merged ontology model and merged source mappings file
            ontMerger.writeMergedOntologyModel(new FileOutputStream(
                                                        new File(output.getOutputOntologyPath())),
                                                    OntologyConstructionCoordinator.ONTOLOGY_SERIALIZATION_FORMAT);
            ontMerger.writeSourceMappings(new FileOutputStream(new File(output.getSourceMappingsPath())));
	    
	} catch (Exception e) { 
            e.printStackTrace();
        }
    }
}
