package org.upc.dtim.bolster.ontomatchmerge.util;

import com.google.common.io.Files;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import org.upc.dtim.bolster.ontomatchmerge.extraction.OntologyExtractionCoordinator;
import org.upc.dtim.bolster.ontomatchmerge.main.OntologyConstructionCoordinator;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by snadal on 13/06/16.
 */
public class OntologyFile {

    public static OntModel readFromFile(String path) {
        Model model = ModelFactory.createDefaultModel();
        OntModel ontModel = ModelFactory.createOntologyModel();
        model.add(FileManager.get().readModel(ontModel, path));
        return ontModel;
    }

    public static String storeOntModelToFile(OntModel model) {
        String tempPath = TempFiles.storeInTempFile("");
        try {
            FileOutputStream os = new FileOutputStream(tempPath);
            model.write(os, OntologyConstructionCoordinator.ONTOLOGY_SERIALIZATION_FORMAT);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempPath;
    }
}
