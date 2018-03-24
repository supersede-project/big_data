package eu.supersede.feedbackanalysis.clustering;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.FilterUnique;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntTools;
import org.apache.jena.ontology.OntTools.Path;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.Filter;
import org.apache.xalan.xsltc.dom.EmptyFilter;
import org.junit.Before;
import org.junit.Test;

import edu.stanford.nlp.util.RegexStringFilter;
import eu.supersede.feedbackanalysis.clustering.OntologyWrapper;

/**
 * 
 * @author fitsum
 *
 */
public class OntologyWrapperTest {

	private OntologyWrapper ontologyWrapper;
	
	@Before
	public void init() {
		String ontologyFile = "SDO_ontology.ttl"; //"saref.ttl"; //
		boolean classOnly = false;
		boolean direct = false;
		String language = "en";
		ontologyWrapper = new OntologyWrapper(ontologyFile, language, classOnly, direct);
	}
	
	@Test
	public void testGraphs() {
		String terms = "heat meter consumption gas house";
		Set<OntClass> concepts = new HashSet<OntClass>();
		for (String term : terms.split(" ")) {
			concepts.addAll(ontologyWrapper.lookupConcepts(term));
		}
		assertNotNull(concepts);

		OntModel ontModel = ontologyWrapper.getOntModel();
//		Graph graph = ontModel.getGraph();
		OntClass first = null;
		for (OntClass concept : concepts) {
			System.out.println(concept.getURI());
			if (first == null) {
				first = concept;
			} else {
				Predicate<Statement> onPath =  new FilterUnique<>();
				Path shortestPath = OntTools.findShortestPath(ontModel, first, concept, Filter.any );
//				OntClass lca = OntTools.getLCA(ontModel, first, concept);
				if (shortestPath != null) {
				System.out.println("Path: " + first.toString() + ", " + concept.toString() + " ==> " + shortestPath.toString());
				}else {
					System.err.println("Unable to compute shortest path between: " + first + " and " + concept);
				}
			}
			
		}
	}
	
	@Test
	public void testOntologyWrapper() {
		assertNotNull(ontologyWrapper);
	}

	@Test
	public void testLookupConcepts() {
		String term = "consumption";
		Set<OntClass> concepts = ontologyWrapper.lookupConcepts(term);
		assertNotNull(concepts);
		for (Resource concept : concepts) {
			System.out.println(concept.getURI());
		}
	}

	@Test
	public void testGetAllProperties() {
		List<OntProperty> properties = ontologyWrapper.getAllProperties();
		assertNotNull(properties);
		assertFalse(properties.isEmpty());
		for (OntProperty p : properties) {
			if (p.getLocalName() != null){
				System.out.println("Found property: " + p.getLocalName() + " : " + p.getLabel(null) + " : " + p.toString());
			}else {
				System.err.println("Cannot be viewed as a property: " + p.getNameSpace());
			}
		}
	}

	@Test
	public void testGetAllConcepts() {
		List<OntClass> classes = ontologyWrapper.getAllClasses();
		assertNotNull(classes);
		assertFalse(classes.isEmpty());
		for (OntClass cl : classes) {
			if (cl.getLocalName() != null){
				System.out.println("Found class: " + cl.getLocalName() + " : " + cl.getLabel(null) + " : " + cl.toString());
			}else {
				System.err.println("Cannot be viewed as a class.");
			}
		}
	}
	
	@Test
	public void testFindTopConcepts() {
		Set<OntClass> concepts = new HashSet<OntClass>();
		ontologyWrapper.findTopConcepts(concepts );
	}
	
	@Test
	public void testLookupConceptsFromClassesAndProperties() {
		String term = "household";
		Set<OntClass> relatedConcepts = ontologyWrapper.lookupConcepts(term);
		assertNotNull(relatedConcepts);
		for (OntClass cl : relatedConcepts) {
			System.err.println(cl.getLocalName());
		}
	}
}
