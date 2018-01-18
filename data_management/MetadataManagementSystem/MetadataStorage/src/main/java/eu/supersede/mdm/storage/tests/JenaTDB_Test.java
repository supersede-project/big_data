package eu.supersede.mdm.storage.tests;

import com.google.common.collect.Lists;
import eu.supersede.mdm.storage.model.bdi_ontology.Namespaces;
import eu.supersede.mdm.storage.model.bdi_ontology.metamodel.GlobalOntology;
import eu.supersede.mdm.storage.util.RDFUtil;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by snadal on 20/06/17.
 */
public class JenaTDB_Test {

    public static void main(String[] args) throws Exception {
        List<String> s = Lists.newArrayList("AAA","AA","A");
        s.forEach(t -> System.out.println(t.length()));

        List<Integer> lenghts = s.stream().map(t -> t.length()).collect(Collectors.toList());
        System.out.println(lenghts);

        System.exit(0);
        Dataset dataset = TDBFactory.createDataset("/home/snadal/Desktop/BolsterMetadataStorage");
        dataset.begin(ReadWrite.WRITE);
        Model model = dataset.getDefaultModel();
        RDFUtil.addTriple(model, Namespaces.R.val()+"DataGatheringTool", Namespaces.rdf.val()+"type", GlobalOntology.CONCEPT.val());
        model.commit();
        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();

    }

}
