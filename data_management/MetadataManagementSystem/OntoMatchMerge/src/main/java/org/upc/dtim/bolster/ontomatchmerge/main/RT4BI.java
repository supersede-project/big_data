/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Things to test
 * - Check &gt; &lt; again
 * - Remove Falcon-AO source code and include JAR only (throws exception)
 */
package org.upc.dtim.bolster.ontomatchmerge.main;

import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.RDBIOArtifact;
import org.upc.dtim.bolster.ontomatchmerge.matching.OntologyMatchingCoordinator;
import org.upc.dtim.bolster.ontomatchmerge.extraction.ioartifacts.XMLIOArtifact;

/**
 *
 * @author Rizkallah
 */

/** ToDo
 * [IMPORTANT]: Keep a document with what you have coded so far
 * Merge CDA schemas and check mappings
 * Start the "update" module:
    * Extract mappings from more than one instance into the same file
    * Serialize "dtpropTypes" and "processedXmlElems" hashmaps into files and read it from file when adding source mappings
 * Fix source mappings code?
 */
public class RT4BI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        // 1. Create different data sources
        XMLIOArtifact xmlSource = new XMLIOArtifact("test/tpc_di/cust_mgmt_schema.xsd");
        xmlSource.addXmlInstancesPath("test/tpc_di/cust_mgmt_instance_extract.xml");
        RDBIOArtifact rdbSource = new RDBIOArtifact("tpcdi", "localhost", "MySQL", "sqluser", "sqluserpw");
        //XMLIOArtifact xmlSource2 = new XMLIOArtifact("test/tpc_di/test.xsd");
        //xmlSource2.addXmlInstancesPath("test/tpc_di/test_instance.xml");
        
        // 2. Add data sources to coordinator
        OntologyConstructionCoordinator constrCoord = new OntologyConstructionCoordinator("tpcdi");
        //constrCoord.addSource(xmlSource2);
        constrCoord.addSource(xmlSource);
        constrCoord.addSource(rdbSource);
        
        // 3. Run ontology construction process
        constrCoord.runOntologyConstructionProcess();
        
        // 4. Choose which matcher to keep and generate one final ontology
        constrCoord.keepMatcherResults(OntologyMatchingCoordinator.FALCON_ISUB_MATCHER);
        
        // 5. IF you want to integrate with Quarry, call the following function to generate a suitable ontology
        constrCoord.generateQuarryOutput(OntologyMatchingCoordinator.FALCON_ISUB_MATCHER);
        
        // 4.a The following code shows the results of the alignments resulting from a specific matcher
        // This is used by the interface to display the results for the user before deciding which matcher to
        // use in the merging process.
        /*HashMap<String, ArrayList<String>> matcherResults = constrCoord.getMatcherResults(OntologyMatchingCoordinator.FALCON_ISUB_MATCHER);
        for (int i = 0; i < matcherResults.get(xmlSource.getSourceName()).size(); i++) {
            for (String source : matcherResults.keySet()) {
                System.out.print(matcherResults.get(source).get(i) + ", ");
            }
            System.out.print("\n");
        }*/
    }
}
