package org.upc.dtim.bolster.metadatadatalayer.util.tests;

import com.google.common.io.Files;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.propertytable.graph.GraphCSV;
import org.apache.jena.propertytable.lang.CSV2RDF;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.upc.dtim.bolster.metadatadatalayer.util.Utils;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by snadal on 6/06/16.
 */
public class OntologyExtractionTest {

    public static void main(String[] args) {
        CSV2RDF.init();

        Dataset dataset = Utils.getTDBDataset();
        dataset.begin(ReadWrite.WRITE);

        Model CSVmodel = ModelFactory.createModelForGraph(new GraphCSV("/home//snadal//Desktop//example.csv"));
        String tempFileName = UUID.randomUUID().toString();
        String filePath = "";
        try {
            File tempFile = File.createTempFile(tempFileName,".tmp");
            filePath = tempFile.getAbsolutePath();
            FileWriter out = new FileWriter(filePath);
            CSVmodel.write(out,"N-TRIPLES");
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("written in "+filePath);
        Model model = dataset.getNamedModel("csv");
        OntModel ontModel = ModelFactory.createOntologyModel();
        model.add(FileManager.get().readModel(ontModel, filePath));
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
    }
}
