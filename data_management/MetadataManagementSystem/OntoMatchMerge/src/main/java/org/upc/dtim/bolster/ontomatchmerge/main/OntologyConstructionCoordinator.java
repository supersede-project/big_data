/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.upc.dtim.bolster.ontomatchmerge.main;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.upc.dtim.bolster.ontomatchmerge.extraction.OntologyExtractionCoordinator;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.OWLIOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.IOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.matching.OntologyMatchingCoordinator;
import org.upc.dtim.bolster.ontomatchmerge.merging.OntologyMergingCoordinator;

/**
 *
 * @author Rizkallah
 */
public class OntologyConstructionCoordinator {

    public static final String ONTOLOGY_SERIALIZATION_FORMAT = "RDF/XML-ABBREV";

    private String projectName;
    private ArrayList<IOArtifact> sources;
    private HashMap<String, ArrayList<String>> filePathsByMatcher;

    ///XXX ER Added to manage previous ontologies
    private OWLIOArtifact previousOWL;
    ///XXX ER End Added

    public OntologyConstructionCoordinator() {
        this.projectName = "";
        this.sources = new ArrayList();

        // Initialize HashMap with the available matchers
        filePathsByMatcher = new HashMap();
        filePathsByMatcher.put(OntologyMatchingCoordinator.FALCON_ISUB_MATCHER, new ArrayList());
        filePathsByMatcher.put(OntologyMatchingCoordinator.FALCON_VDOC_MATCHER, new ArrayList());
        filePathsByMatcher.put(OntologyMatchingCoordinator.FALCON_COMBINED_MATCHER, new ArrayList());
    }

    public OntologyConstructionCoordinator(String porjectName) {
        this.projectName = porjectName;
        new File(projectName).mkdir();
        this.sources = new ArrayList();

        // Initialize HashMap with the available matchers
        filePathsByMatcher = new HashMap();
        filePathsByMatcher.put(OntologyMatchingCoordinator.FALCON_ISUB_MATCHER, new ArrayList());
        filePathsByMatcher.put(OntologyMatchingCoordinator.FALCON_VDOC_MATCHER, new ArrayList());
        filePathsByMatcher.put(OntologyMatchingCoordinator.FALCON_COMBINED_MATCHER, new ArrayList());
    }

    public void runOntologyConstructionProcess() {
        // 1. Extract ontology from each source
        for (IOArtifact source : sources) {
            OntologyExtractionCoordinator.extractOntology(source, new Properties());
        }

        // 2. Run the matching process using all available matchers
        runMatchingAndMergingProcess(OntologyMatchingCoordinator.FALCON_ISUB_MATCHER);
        runMatchingAndMergingProcess(OntologyMatchingCoordinator.FALCON_VDOC_MATCHER);
        runMatchingAndMergingProcess(OntologyMatchingCoordinator.FALCON_COMBINED_MATCHER);
    }

    public void runMatchingAndMergingProcess(String matcherName) {
        String finalOwlPath = projectName + "/" + projectName + "_" + matcherName + "_final_onto.owl";
        String finalSourceMappingsPath = projectName + "/" + projectName + "_" + matcherName + "_final_onto_mappings.xml";
        filePathsByMatcher.get(matcherName).add(finalOwlPath);
        filePathsByMatcher.get(matcherName).add(finalSourceMappingsPath);

        OWLIOArtifact finalOwlOutput = new OWLIOArtifact(finalOwlPath, finalSourceMappingsPath);
        OWLIOArtifact owlSource = new OWLIOArtifact(sources.get(0).getOutputOntologyPath(), sources.get(0).getSourceMappingsPath());

        ///XXX ER Check if there is a previous ontology
        if (this.previousOWL != null){
            String alignmentsPath = projectName + "/" + sources.get(0).getSourceName() + "_" + projectName
                    + "_" + matcherName + "_aligns.xml";
            filePathsByMatcher.get(matcherName).add(alignmentsPath);
            OntologyMatchingCoordinator.matchOntologies(matcherName, this.previousOWL, sources.get(0), alignmentsPath, finalOwlOutput);
            owlSource = new OWLIOArtifact(finalOwlOutput.getOutputOntologyPath(), finalOwlOutput.getSourceMappingsPath());
        } 
        ///XXX ER End of code
        
        for (int i = 1; i < sources.size(); i++) {
            String alignmentsPath = projectName + "/" + sources.get(i).getSourceName() + "_" + projectName
                    + "_" + matcherName + "_aligns.xml";
            filePathsByMatcher.get(matcherName).add(alignmentsPath);
            OntologyMatchingCoordinator.matchOntologies(matcherName,
                    owlSource,
                    sources.get(i),
                    alignmentsPath,
                    finalOwlOutput);
            owlSource = new OWLIOArtifact(finalOwlOutput.getOutputOntologyPath(), finalOwlOutput.getSourceMappingsPath());
        }

    }

    public HashMap<String, ArrayList<String>> getMatcherResults(String matcherName) {
        // Get alignment file paths. First two paths are ignored
        ArrayList<String> alignmentPaths = new ArrayList(filePathsByMatcher.get(matcherName).subList(2,
                filePathsByMatcher.get(matcherName).size()));

        // Initialize HashMap of results to new ArrayList for each source
        HashMap<String, ArrayList<String>> matcherResults = new HashMap();
        HashMap<String, String> sourceURItoNameMap = new HashMap();
        for (IOArtifact source : sources) {
            matcherResults.put(source.getSourceName(), new ArrayList());
            sourceURItoNameMap.put(source.getOutputOntologyUri(), source.getSourceName());
        }

        // Iterate through all alignments and fill matcher results
        for (String alignPath : alignmentPaths) {
            try {
                AlignmentParser parser = new AlignmentParser();
                Alignment alignment = parser.parse(new File(alignPath).toURI());
                Enumeration<Cell> alignments = alignment.getElements();
                while (alignments.hasMoreElements()) {
                    Cell cell = alignments.nextElement();
                    String obj1FullUri = cell.getObject1AsURI().toString();
                    String obj2FullUri = cell.getObject2AsURI().toString();
                    String source1 = sourceURItoNameMap.get(obj1FullUri.substring(0, obj1FullUri.lastIndexOf("#") + 1));
                    String source2 = sourceURItoNameMap.get(obj2FullUri.substring(0, obj2FullUri.lastIndexOf("#") + 1));
                    String obj1LocalName = obj1FullUri.substring(obj1FullUri.lastIndexOf("#") + 1);
                    String obj2LocalName = obj2FullUri.substring(obj2FullUri.lastIndexOf("#") + 1);

                    ///XXX ER Commented original code
//                    // Check matcherResults to see if first object already exists
//                    if (matcherResults.get(source1).contains(obj1LocalName)) {
//                        int index = matcherResults.get(source1).indexOf(obj1LocalName);
//                        matcherResults.get(source2).set(index, obj2LocalName);
//                    } // Check matcherResults to see if second object already exists
//                    else if (matcherResults.get(source2).contains(obj2LocalName)) {
//                        int index = matcherResults.get(source2).indexOf(obj2LocalName);
//                        matcherResults.get(source1).set(index, obj1LocalName);
//                    } // If not, add new alignment to matcherResults
//                    else {
//                        matcherResults.get(source1).add(obj1LocalName);
//                        matcherResults.get(source2).add(obj2LocalName);
//                        for (String source : matcherResults.keySet()) {
//                            if (!source.equals(source1) && !source.equals(source2)) {
//                                matcherResults.get(source).add("");
//                            }
//                        }
//                    }
                    ///XXX End commented code
                    ///XXX ER Check if source1 and source2 are null, this is for vdoc aligns.xml file that has more elements.
                    if (source1 != null && source2 != null) {
                        // Check matcherResults to see if first object already exists
                        if (matcherResults.get(source1).contains(obj1LocalName)) {
                            int index = matcherResults.get(source1).indexOf(obj1LocalName);
                            matcherResults.get(source2).set(index, obj2LocalName);
                        } // Check matcherResults to see if second object already exists
                        else if (matcherResults.get(source2).contains(obj2LocalName)) {
                            int index = matcherResults.get(source2).indexOf(obj2LocalName);
                            matcherResults.get(source1).set(index, obj1LocalName);
                        } // If not, add new alignment to matcherResults
                        else {
                            matcherResults.get(source1).add(obj1LocalName);
                            matcherResults.get(source2).add(obj2LocalName);
                            for (String source : matcherResults.keySet()) {
                                if (!source.equals(source1) && !source.equals(source2)) {
                                    matcherResults.get(source).add("");

                                }
                            }
                        }
                    } else if (source1 != null && source2 == null) {
                        matcherResults.get(source1).add(obj1LocalName);
                    }
                    ///XXX ER modified code, only added initial if.
                }
            } catch (AlignmentException ex) {
                Logger.getLogger(OntologyConstructionCoordinator.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

        return matcherResults;
    }

    // Should not be used manually. Only used from the interface
    public void finalizeOntologyConstructionOutput(String matcherName) {
        ///XXX ER Disable delete for files
        //keepMatcherResults(matcherName);
        ///XXX ER
        generateQuarryOutput(matcherName);
    }

    public void keepMatcherResults(String matcherName) {
        // Delete all files generated from matching processes using the other matchers
        for (String key : filePathsByMatcher.keySet()) {
            if (key.equals(matcherName)) {
                continue;
            }
            for (String filePath : filePathsByMatcher.get(key)) {
                try {
                    Files.deleteIfExists(new File(filePath).toPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void generateQuarryOutput(String matcherName) {
        String finalOwlPath = projectName + "/" + projectName + "_output_onto.owl";
        String finalSourceMappingsPath = projectName + "/" + projectName + "_output_onto_mappings.xml";

        OWLIOArtifact finalOwlOutput = new OWLIOArtifact(finalOwlPath, finalSourceMappingsPath);
        OWLIOArtifact owlSource = new OWLIOArtifact(sources.get(0).getOutputOntologyPath(), sources.get(0).getSourceMappingsPath());

        ///XXX ER This was put in zero to consider just one source
//        for (int i = 1; i < sources.size(); i++) {
        for (int i = 1; i < sources.size(); i++) {
            String alignmentsPath = projectName + "/" + sources.get(i).getSourceName() + "_" + projectName
                    + "_" + matcherName + "_aligns.xml";
            OntologyMergingCoordinator.mergeOntologies(owlSource,
                    sources.get(i),
                    alignmentsPath,
                    finalOwlOutput);
            owlSource = new OWLIOArtifact(finalOwlOutput.getOutputOntologyPath(), finalOwlOutput.getSourceMappingsPath());
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
        new File(projectName).mkdir();

        // Update paths in all sources
        for (IOArtifact source : sources) {
            source.setOutputOntologyPath(this.projectName + "/" + source.getSourceName() + "_onto.owl");
            source.setSourceMappingsPath(this.projectName + "/" + source.getSourceName() + "_onto_mappings.xml");
        }
    }

    public ArrayList<IOArtifact> getSources() {
        return sources;
    }

    public void addSource(IOArtifact source) {
        source.setOutputOntologyPath(projectName + "/" + source.getSourceName() + "_onto.owl");
        source.setSourceMappingsPath(projectName + "/" + source.getSourceName() + "_onto_mappings.xml");
        this.sources.add(source);
    }

    public void addPreviousOWL(String owlPath, String mappingPath) {
        this.previousOWL = new OWLIOArtifact(owlPath, mappingPath);
    }
}
