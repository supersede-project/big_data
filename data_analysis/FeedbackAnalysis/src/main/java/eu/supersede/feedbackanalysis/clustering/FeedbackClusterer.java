/**
 * 
 */
package eu.supersede.feedbackanalysis.clustering;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntClass;

import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.FileManager;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;
import weka.core.neighboursearch.PerformanceStats;
import weka.filters.Filter;

/**
 * @author fitsum
 *
 */
public class FeedbackClusterer {

	Instances instances;
//	Instances filteredInstances;

	FeedbackAnnotator feedbackAnnotator;
	OntologyWrapper ontologyWrapper;
	
	/**
	 * 
	 */
	public FeedbackClusterer(String dataset, boolean arff, boolean file, double percent) {
		loadDataset(dataset, arff, file);

	}

	private void loadDataset(String dataset, boolean arff, boolean file) {
		try {
			InputStream is;
			if (file) {
				String fileName = FileManager.getResource(dataset, getClass().getClassLoader()).getFile();
				is = new FileInputStream(fileName);
			} else {
				is = new ByteArrayInputStream(dataset.getBytes());
			}
			if (arff) {
				DataSource ds = new DataSource(is);
				instances = ds.getDataSet();
			}else {
				CSVLoader csvLoader = new CSVLoader();
				csvLoader.setSource(is);
				instances = csvLoader.getDataSet();
			}

//			// set classindex
//			instances.setClassIndex(instances.numAttributes() - 2);
//			
//			weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
//			filter.setAttributeIndices((instances.classIndex() + 1) + "-last");
//			filter.setInputFormat(instances);
//			filteredInstances = Filter.useFilter(instances, filter);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to load dataset");
		}
	}

	public FeedbackClusterer (String ontologyFile, String wordnetDbPath, String language) {
		
		boolean classLabelsOnly = false;
		boolean directLinksOnly = false;
		feedbackAnnotator = new FeedbackAnnotator(ontologyFile, wordnetDbPath, language, classLabelsOnly, directLinksOnly);
		ontologyWrapper = feedbackAnnotator.getOntologyWrapper();
		
	}
	
	public SimpleKMeans computeClusters (List<UserFeedback> feedbacks, int numClusters) {
		
		StringBuffer fvs = new StringBuffer();
		boolean header = false;
		boolean addClass = false;
		fvs.append(ontologyWrapper.getFeatureVectorHeader(addClass));
		for (UserFeedback userFeedback : feedbacks) {
			Set<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			String fv = ontologyWrapper.conceptsToFeatureVectorString(concepts, header, addClass);
			fvs.append(fv + "\n");
		}
		boolean arff = false;
		boolean file = false;
		loadDataset(fvs.toString(), arff, file);
		
		// compute clusters
		SimpleKMeans clusterer = computeClusters(numClusters);
		
		return clusterer;
	}
	
	public Map<Integer, List<UserFeedback>> clusterUserFeedback(List<UserFeedback> allFeedback, SimpleKMeans clusterer) throws Exception{
		Map<Integer, List<UserFeedback>> feedbackClusters = new HashMap<Integer, List<UserFeedback>>();
		for (UserFeedback userFeedback : allFeedback) {
			Set<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			int[] fv = ontologyWrapper.conceptsToFeatureVector(concepts);
			double weight = instances.attribute(0).weight();
			Instance instance = new DenseInstance(weight, Arrays.stream(fv).asDoubleStream().toArray());
			int cluster = clusterer.clusterInstance(instance);
			if (!feedbackClusters.containsKey(cluster)) {
				feedbackClusters.put(cluster, new ArrayList<UserFeedback>());
			}
			feedbackClusters.get(cluster).add(userFeedback);
		}
		return feedbackClusters;
	}
	
	
	public SimpleKMeans computeClusters2 (List<FeedbackMessage> feedbacks, int numClusters) {
		
		StringBuffer fvs = new StringBuffer();
		boolean header = false;
		boolean addClass = false;
		fvs.append(ontologyWrapper.getFeatureVectorHeader(addClass));
		for (FeedbackMessage userFeedback : feedbacks) {
			Set<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			String fv = ontologyWrapper.conceptsToFeatureVectorString(concepts, header, addClass);
			fvs.append(fv + "\n");
		}
		boolean arff = false;
		boolean file = false;
		loadDataset(fvs.toString(), arff, file);
		
		// save fv
		try {
			String fvFile = "src/test/resources/trainingsets/SENERCON_fv.csv";
			FileUtils.writeStringToFile(new File(fvFile), fvs.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// compute clusters
		SimpleKMeans clusterer = computeClusters(numClusters);
		
		return clusterer;
	}
	
	public Map<Integer, List<FeedbackMessage>> clusterUserFeedback2 (List<FeedbackMessage> allFeedback, SimpleKMeans clusterer) throws Exception{
		Map<Integer, List<FeedbackMessage>> feedbackClusters = new HashMap<Integer, List<FeedbackMessage>>();
		for (FeedbackMessage userFeedback : allFeedback) {
			Set<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			int[] fv = ontologyWrapper.conceptsToFeatureVector(concepts);
			double weight = instances.attribute(0).weight();
			Instance instance = new DenseInstance(weight, Arrays.stream(fv).asDoubleStream().toArray());
			int cluster = clusterer.clusterInstance(instance);
			if (!feedbackClusters.containsKey(cluster)) {
				feedbackClusters.put(cluster, new ArrayList<FeedbackMessage>());
			}
			feedbackClusters.get(cluster).add(userFeedback);
		}
		return feedbackClusters;
	}
	
	public SimpleKMeans computeClusters(int numClusters) {

		SimpleKMeans clusterer = new SimpleKMeans();
		try {
			clusterer.setNumClusters(numClusters);
			clusterer.setMaxIterations(100);
			DistanceFunction df = new ManhattanDistance(instances);
			clusterer.setDistanceFunction(df);
			clusterer.buildClusterer(instances);

			// evaluate the clusters
//			ClusterEvaluation eval = new ClusterEvaluation();
//			eval.setClusterer(clusterer);
//			eval.evaluateClusterer(instances);
			
			// print results
//			System.err.println(eval.clusterResultsToString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clusterer;
	}

	public Instances computeNearestNeighbors (int k) {
		Instances nearestNeighbours = null;
		try {
			Instance t = instances.remove(0);
			NearestNeighbourSearch nn = new LinearNNSearch(instances);
			nn.setDistanceFunction(new ManhattanDistance(instances));
			
			nearestNeighbours = nn.kNearestNeighbours(t, k);
			Iterator<Instance> iterator = nearestNeighbours.iterator();
			while (iterator.hasNext()) {
				Instance instance = iterator.next();
				System.err.println(instance);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return nearestNeighbours;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dataset = "trainingsets/SDO_ontology.ttl.class_only.fv.csv";
		boolean arff = false;
		boolean file = true;
		double percent = 0.7;
		FeedbackClusterer fCluster = new FeedbackClusterer(dataset, arff, file, percent);
		fCluster.computeClusters(5);
		
		Instances nearestNeighbors = fCluster.computeNearestNeighbors(5);

	}

}
