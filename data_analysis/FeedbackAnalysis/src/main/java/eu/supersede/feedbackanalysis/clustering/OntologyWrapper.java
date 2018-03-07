package eu.supersede.feedbackanalysis.clustering;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import com.github.andrewoma.dexx.collection.HashMap;

import edu.smu.tspell.wordnet.SynsetType;

/**
 * 
 * @author fitsum
 *
 */

public class OntologyWrapper {
	private String rdfFileName;
	// private Model model;
	private OntModel ontModel;

	private Map<String, Set<OntClass>> termCache = new java.util.HashMap<String, Set<OntClass>>();

	private List<OntClass> classes;
	private List<OntProperty> properties;

	private Map<OntClass, Set<String>> classTerms = new java.util.HashMap<OntClass, Set<String>>();

	// consider only directly related properties or not
	boolean directLinksOnly = true;
	boolean classLabelsOnly = false;
	String language = "en";
	
	public OntologyWrapper(String ontology, String lang, boolean classOnly, boolean direct) {
		rdfFileName = ontology;
		language = lang;
		classLabelsOnly = classOnly;
		directLinksOnly = direct;

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(rdfFileName); //FileManager.get().open(rdfFileName);
		if (in == null) {
			throw new IllegalArgumentException("File: " + rdfFileName + " not found");
		}

		// model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);

		ontModel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		ontModel.read(in, null, "TTL");

		classes = getAllClasses();
		properties = getAllProperties();
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO this is added to handle classes with owl:Classs, rather than rdf:Class.
		// Is there a better way?
		if (classes.isEmpty()) {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(rdfFileName);
			ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			ontModel.read(in, null, "TTL");
			classes = getAllClasses();
			properties = getAllProperties();
		}
		
		// collect terms from Classes and related properties
		colectClassTerms(directLinksOnly);
	}

	private void colectClassTerms(boolean direct) {
		for (OntClass cl : classes) {
			Set<String> terms = new HashSet<String>();

			String resourceName = cl.getLocalName();
			System.out.println("Class: " + resourceName);
			if (resourceName != null) {
				String classLbl = cl.getLabel(language).toLowerCase();
				terms.addAll(Arrays.asList(classLbl.split(" ")));
			}

			if (!classLabelsOnly) {
				// get all Properties in which this class is either a Domain or Range
				ExtendedIterator<OntProperty> declaredProperties = cl.listDeclaredProperties(direct);
				while (declaredProperties.hasNext()) {
					OntProperty property = declaredProperties.next();
	//				System.out.println("Related prop >>> " + property.getLocalName());
					String lbl = property.getLabel(language).toLowerCase();
					terms.addAll(Arrays.asList(lbl.split(" ")));
				}
			}
			
			// add terms to map
			classTerms.put(cl, terms);
		}
	}
	
	/**
	 * Returns a List of all the classes in the ontology. It returns a List, instead
	 * of a Set, to guarantee same ordering in a given execution. Note that the
	 * order may be different in different executions.
	 * 
	 * @return
	 */
	public List<OntClass> getAllClasses() {
		Set<OntClass> allClasses = new HashSet<>();
		ExtendedIterator<OntClass> classeIterator = ontModel.listClasses();
		while (classeIterator.hasNext()) {
			OntClass cl = classeIterator.next();
			if (cl.getLocalName() != null) {
				allClasses.add(cl);
			}
		}
		List<OntClass> classes = new LinkedList<>();
		classes.addAll(allClasses);
		return classes;
	}

	/**
	 * Returns a List of all the properties in the ontology.
	 * 
	 * @return
	 */
	public List<OntProperty> getAllProperties() {
		Set<OntProperty> allProperties = new HashSet<>();
		ExtendedIterator<OntProperty> propertyIterator = ontModel.listOntProperties();
		while (propertyIterator.hasNext()) {
			OntProperty p = propertyIterator.next();
			if (p.getLocalName() != null) {
				allProperties.add(p);
			}
		}
		List<OntProperty> properties = new LinkedList<>();
		properties.addAll(allProperties);
		return properties;
	}

	public String conceptsToFeatureVector(Map<FeedbackMessage, Set<OntClass>> annotatedFeedbacks) {
		StringBuffer buffer = new StringBuffer();

		// collect an array of all concepts
		// Set<OntClass> allClasses = getAllClasses();
		for (OntClass cl : classes) {
			buffer.append(cl.toString() + ",");
		}
		// append label
		buffer.append("class,feedback_id\n");
		// buffer.deleteCharAt(buffer.lastIndexOf(","));

		// map each feedbacks concepts to a feature vector
		for (Entry<FeedbackMessage, Set<OntClass>> entry : annotatedFeedbacks.entrySet()) {
			for (OntClass c : classes) {
				if (entry.getValue().contains(c)) {
					buffer.append("1,");
				} else {
					buffer.append("0,");
				}
			}
			// append label, in this case category
			String category = entry.getKey().getCategory().trim();
			if (category.isEmpty()) {
				category = "UNLABELED";
			}
			buffer.append(category + "," + entry.getKey().getId() + "\n");
		}

		return buffer.toString();
	}

	public int[] conceptsToFeatureVector(Set<OntClass> concepts) {
		int[] fv = new int[classes.size()];
		int i = 0;
		for (OntClass concept : classes) {
			if (concepts.contains(concept)) {
				fv[i++] = 1;
			} else {
				fv[i++] = 0;
			}
		}
		return fv;
	}
	
	/**
	 * this is just a convenience method to get the vector as a String so that it can be easily parsed to Weka Instances
	 * @param concepts
	 * @return
	 */
	public String conceptsToFeatureVectorString(Set<OntClass> concepts, boolean header, boolean addClass) {
		StringBuffer fv = new StringBuffer();
		
		if (header) {
			for (OntClass cl : classes) {
				fv.append(cl.getLocalName() + ",");
			}
			if (addClass) {
				fv.append("class");
			}else {
				fv.deleteCharAt(fv.length() - 1);
			}
			fv.append("\n");
		}
		
		for (OntClass concept : classes) {
			if (concepts.contains(concept)) {
				fv.append("1,");
			}else {
				fv.append("0,");
			}
		}
		if (addClass) {
			fv.append("?");
		}else {
			fv.deleteCharAt(fv.length() - 1);
		}
		return fv.toString();
	}
	
	public Set<OntClass> lookupConcepts(String term) {
		term = term.trim().toLowerCase();

		Set<OntClass> concepts = null;

		// first check in cache
		concepts = termCache.get(term);
		if (concepts == null) {
			concepts = new HashSet<OntClass>();

			for (OntClass cl : classes) {
				if (classTerms.get(cl).contains(term)) {
					concepts.add(cl);
				}
			}
			termCache.put(term, concepts);
		}
		return concepts;
	}

//	public Set<OntClass> lookupConceptsFromClassesOnly(String term) {
//		term = term.trim().toLowerCase();
//
//		Set<OntClass> concepts = null;
//
//		// first check in cache
//		concepts = termCache.get(term);
//		if (concepts == null) {
//			concepts = new HashSet<OntClass>();
//
//			for (OntClass cl : classes) {
//				String resourceName = cl.getLocalName();
//				if (resourceName != null) {
//					// System.err.println(resourceName);
//					if (isSimilar(term, cl.getLabel(language))) {
//						concepts.add(cl);
//					}
//				}
//			}
//			termCache.put(term, concepts);
//		}
//		return concepts;
//	}
//
//	private boolean isSimilar(String term, String classLabel) {
//		boolean similar = false;
//		Set<String> labelTerms = new HashSet<String>();
//		labelTerms.addAll(Arrays.asList(classLabel.split(" ")));
//		if (labelTerms.contains(term)) {
//			similar = true;
//		}
//		return similar;
//	}
//
//	private boolean isSimilar(String term, OntClass concept) {
//		boolean similar = false;
//		// String localName = resource.getLocalName();
//		String label = concept.getLabel(language).toLowerCase(); // null = don't care, "en" = English
//		Set<String> labelTerms = new HashSet<String>();
//		labelTerms.addAll(Arrays.asList(label.split(" ")));
//		if (labelTerms.contains(term)) {
//			similar = true;
//		}
//		return similar;
//	}
//
//	public String getLabel(Resource resource) {
//		StmtIterator i = resource.listProperties(RDFS.label);
//		while (i.hasNext()) {
//			Literal l = i.next().getLiteral();
//
//			if (l.getLanguage() != null && l.getLanguage().equals(language)) {
//				return l.getLexicalForm().toLowerCase();
//			}
//		}
//
//		return resource.getLocalName().toLowerCase();
//	}

	// public Model getModel() {
	// return model;
	// }
	//
	// public void setModel(Model model) {
	// this.model = model;
	// }

	public Set<OntClass> findTopConcepts(Set<OntClass> concepts) {
		Set<OntClass> topConcepts = new HashSet<OntClass>();
		// for (OntClass concept : concepts) {
		Graph graph = ontModel.getGraph();
		ExtendedIterator<Triple> iterator = graph.find();
		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			System.out.println(triple);
		}
		// }
		return topConcepts;
	}

	public OntModel getOntModel() {
		return ontModel;
	}

}
