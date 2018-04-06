/**
 * 
 */
package eu.supersede.feedbackanalysis.clustering;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Combinations;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.jena.ontology.OntClass;
import org.apache.lucene.util.MathUtil;

import com.opencsv.CSVWriter;

import eu.supersede.feedbackanalysis.ds.UserFeedback;
import eu.supersede.feedbackanalysis.preprocessing.utils.FileManager;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
import weka.core.SparseInstance;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.expressionlanguage.common.MathFunctions;
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
	
	boolean COUNT_FV = false;
	
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
	

	/**
	 * Clusters a given set of UserFeedback objects into numClusters clusters
	 * @param allFeedback
	 * @param numClusters
	 * @return a map of cluster ID to the set of UserFeedback objects in that cluster
	 * @throws Exception
	 */
	public Map<Integer, List<UserFeedback>> clusterUserFeedback(List<UserFeedback> allFeedback, int numClusters) throws Exception{
		
		// first build the clusterer model
		Clusterer clusterer = buildClustererFromUserFeedback(allFeedback, numClusters);
		
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
	
	/**
	 * Clusters a given set of UserFeedback objects into numClusters clusters
	 * @param allFeedback
	 * @param numClusters
	 * @return a map of cluster concepts to the set of UserFeedback objects in that cluster
	 * @throws Exception
	 */
	public Map<Set<OntClass>, List<UserFeedback>> clusterUserFeedbackConceptsIntersection(List<UserFeedback> allFeedback, int numClusters) throws Exception{
		
		// first build the clusterer model
		Clusterer clusterer = buildClustererFromUserFeedback(allFeedback, numClusters);
		
		Map<Set<OntClass>, List<UserFeedback>> feedbackClusters = new HashMap<Set<OntClass>, List<UserFeedback>>();
		
		Map<Integer, List<UserFeedback>> clusterFeedback = new HashMap<Integer, List<UserFeedback>>();
		Map<Integer, Set<OntClass>> clusterConcepts = new HashMap<Integer, Set<OntClass>>();
		
		for (UserFeedback userFeedback : allFeedback) {
			Set<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			int[] fv = ontologyWrapper.conceptsToFeatureVector(concepts);
			double weight = instances.attribute(0).weight();
			Instance instance = new DenseInstance(weight, Arrays.stream(fv).asDoubleStream().toArray());
			int cluster = clusterer.clusterInstance(instance);
			
			if (!clusterFeedback.containsKey(cluster)) {
				clusterFeedback.put(cluster, new ArrayList<UserFeedback>());
			}
			if(!clusterConcepts.containsKey(cluster)) {
				clusterConcepts.put(cluster, new HashSet<OntClass>());
				clusterConcepts.get(cluster).addAll(concepts);
			}else { // keep only intersection
				clusterConcepts.get(cluster).retainAll(concepts);
			}
			clusterFeedback.get(cluster).add(userFeedback);
		}
		for (Entry<Integer, List<UserFeedback>> entry : clusterFeedback.entrySet()) {
			feedbackClusters.put(clusterConcepts.get(entry.getKey()), entry.getValue());
		}
		return feedbackClusters;
	}
	
	
	/**
	 * Clusters a given set of UserFeedback objects into numClusters clusters
	 * @param allFeedback
	 * @param numClusters
	 * @return a map of cluster concepts to the set of UserFeedback objects in that cluster
	 * @throws Exception
	 */
	public Map<Set<OntClass>, List<UserFeedback>> clusterUserFeedbackConceptsUnion(List<UserFeedback> allFeedback, int numClusters) throws Exception{
		
		// first build the clusterer model
		Clusterer clusterer = buildClustererFromUserFeedback(allFeedback, numClusters);
		
		Map<Set<OntClass>, List<UserFeedback>> feedbackClusters = new HashMap<Set<OntClass>, List<UserFeedback>>();
		
		Map<Integer, List<UserFeedback>> clusterFeedback = new HashMap<Integer, List<UserFeedback>>();
		Map<Integer, Set<OntClass>> clusterConcepts = new HashMap<Integer, Set<OntClass>>();
		
		for (UserFeedback userFeedback : allFeedback) {
			Set<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			int[] fv = ontologyWrapper.conceptsToFeatureVector(concepts);
			double weight = instances.attribute(0).weight();
			Instance instance = new DenseInstance(weight, Arrays.stream(fv).asDoubleStream().toArray());
			int cluster = clusterer.clusterInstance(instance);
			
			if (!clusterFeedback.containsKey(cluster)) {
				clusterFeedback.put(cluster, new ArrayList<UserFeedback>());
			}
			if(!clusterConcepts.containsKey(cluster)) {
				clusterConcepts.put(cluster, new HashSet<OntClass>());
			}
			clusterConcepts.get(cluster).addAll(concepts);
			
			clusterFeedback.get(cluster).add(userFeedback);
		}
		for (Entry<Integer, List<UserFeedback>> entry : clusterFeedback.entrySet()) {
			feedbackClusters.put(clusterConcepts.get(entry.getKey()), entry.getValue());
		}
		return feedbackClusters;
	}
	
	/**
	 * Build a clusterer models from a given set of UserFeedback objects
	 * @param feedbacks
	 * @param numClusters
	 * @return
	 */
	public Clusterer buildClustererFromUserFeedback (List<UserFeedback> feedbacks, int numClusters) {
		
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
		Clusterer clusterer = trainClusterer(numClusters);
		
		return clusterer;
	}
	
	/** 
	 * (A convenience method for training models from a different dataset structure (research))
	 * Build a clusterer models from a given set of FeedbackMessage objects
	 * @param feedbacks
	 * @param numClusters
	 * @return
	 */
	public Clusterer buildClustererFromFeedbackMessage (List<FeedbackMessage> feedbacks, int numClusters) {
		
		StringBuffer fvs = new StringBuffer();
		boolean header = false;
		boolean addClass = false;
		fvs.append(ontologyWrapper.getFeatureVectorHeader(addClass));
		for (FeedbackMessage userFeedback : feedbacks) {
			Collection<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			String fv = ontologyWrapper.conceptsToFeatureVectorString(concepts, header, addClass);
			fvs.append(fv + "\n");
		}
		boolean arff = false;
		boolean file = false;
		loadDataset(fvs.toString(), arff, file);
		
		// save fv, statistics
		try {
			String fvFile = "src/test/resources/trainingsets/SENERCON_fv.csv";
			FileUtils.writeStringToFile(new File(fvFile), fvs.toString());
			String statFile = "src/test/resources/trainingsets/SENERCON_stats.csv";
			FileUtils.writeStringToFile(new File(statFile), feedbackAnnotator.getStat());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// compute clusters
		Clusterer clusterer = trainClusterer(numClusters);
		
		return clusterer;
	}
	
	/**
	 * Train clusterer models using different methods available in Weka
	 * @param numClusters
	 * @return
	 */
	Clusterer trainClusterer (int numClusters) {
		Clusterer clusterer = computeClustersKMeans(numClusters);
//		Clusterer clusterer = computeClustersDensity(0);
		return clusterer;
	}
	
	public Map<Integer, List<FeedbackMessage>> clusterFeedbackMessages (List<FeedbackMessage> allFeedback, int numClusters) throws Exception{
		
		// first build the clusterer model
		Clusterer clusterer = buildClustererFromFeedbackMessage(allFeedback, numClusters);
		
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
	
	/**
	 * Maps every user feedback to a cluster
	 * @param allFeedback
	 * @param clusterer
	 * @return a Map with key user feedback and value the cluster id
	 * @throws Exception
	 */
	public Map<FeedbackMessage, Integer> clusterFeedbackMessages2 (List<FeedbackMessage> allFeedback, int numClusters) throws Exception{
		
		// first build the clusterer model
		Clusterer clusterer = buildClustererFromFeedbackMessage(allFeedback, numClusters);
				
		Map<FeedbackMessage, Integer> feedbackClusters = new HashMap<FeedbackMessage, Integer>();
		for (FeedbackMessage userFeedback : allFeedback) {
			Collection<OntClass> concepts = feedbackAnnotator.annotateFeedback2(userFeedback);
			int[] fv = ontologyWrapper.conceptsToFeatureVector(concepts);
			double weight = instances.attribute(0).weight();
			Instance instance = new DenseInstance(weight, Arrays.stream(fv).asDoubleStream().toArray());
			int cluster = clusterer.clusterInstance(instance);
			
			feedbackClusters.put(userFeedback, cluster);
		}
		return feedbackClusters;
	}
	
	
	
	
	private Clusterer computeClustersKMeans(int numClusters) {

		Clusterer clusterer = new SimpleKMeans();
		try {
			((SimpleKMeans)clusterer).setNumClusters(numClusters);
			((SimpleKMeans)clusterer).setMaxIterations(100);
			DistanceFunction df = new EuclideanDistance(instances); // ManhattanDistance(instances);
			((SimpleKMeans)clusterer).setDistanceFunction(df);
			clusterer.buildClusterer(instances);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return clusterer;
	}

	private Clusterer computeClustersDensity(int numClusters) {

		Clusterer clusterer = new EM();
		try {
			if (numClusters > 0) {
				((EM)clusterer).setNumClusters(numClusters);
			}
			clusterer.buildClusterer(instances);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return clusterer;
	}
	
	Instances computeNearestNeighbors (int k) {
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
	 * compute metrics for comparing the computed clusters with the ones provided by the expert (gold standard) 
	 * @param feedbackClusters a map with clusters as key and feedbacks in the cluster as value
	 */
	private double computeClusteringAccuracy(Map<FeedbackMessage, Integer> feedbackClusters){
		
		/**
		 * S11 = {pairs that are in the same cluster under C and C’}
		 * S00 = {pairs that are in different clusters under C and C’}
		 * S10 = {pairs that are in the same cluster under C but in different ones under C’}
		 * S01 = {pairs that are in different clusters under C but in the same under C’}
		 */
//		int[][] counts = new int [2][2];
		Set<String> s11 = new HashSet<String>();
		Set<String> s01 = new HashSet<String>();
		Set<String> s10 = new HashSet<String>();
		Set<String> s00 = new HashSet<String>();
		
		// build a list of feedback objects
		List<FeedbackMessage> feedbackMessages = new ArrayList<>();
		feedbackMessages.addAll(feedbackClusters.keySet());
		
		for (int i = 0; i < feedbackMessages.size(); i++) {
			FeedbackMessage f1 = feedbackMessages.get(i);
			int c1 = feedbackClusters.get(f1);
			for (int j = i + 1; j < feedbackMessages.size(); j++) {
				FeedbackMessage f2 = feedbackMessages.get(j);
				int c2 = feedbackClusters.get(f2);
				
				String pair = "{" + f1.getId() + "," + f2.getId() + "}";
				if (c1 == c2 && f1.getClusterId().equalsIgnoreCase(f2.getClusterId())) { // S11
					s11.add(pair);
				} else if (c1 == c2 && !f1.getClusterId().equalsIgnoreCase(f2.getClusterId())) { // S10
					s10.add(pair);
				} else if (c1 != c2 && f1.getClusterId().equalsIgnoreCase(f2.getClusterId())) { // S01
					s01.add(pair);
				} else { // S00
					s00.add(pair);
				}
			}
		}
		
//		double score = randIndex (s11, s01, s10, s00, feedbackMessages.size());
		double score = fowlkesMallows (s11, s01, s10, s00);
//		double score = jaccardIndex (s11, s01, s10, s00);
//		double score = mirkinMetric (s10, s01);
		
		// the two clusterings (side effects of contingency table function)
//		Map<Integer, Set<FeedbackMessage>> c1 = new HashMap<>();
//		Map<Integer, Set<FeedbackMessage>> c2 = new HashMap<>();
//		double[][] contingencyTable = computeContinegencyTable(feedbackClusters, c1, c2);
//		double score = adjustedRandIndex (c1, c2, contingencyTable, feedbackMessages.size());
		
		return score;
	}
	
	private double mirkinMetric (Set<String> s10, Set<String> s01) {
		return 2*(s01.size() + s10.size());
	}
	
	/**
	 * @param c1
	 * @param c2
	 * @param contingencyTable
	 * @param size
	 * @return
	 */
	private double adjustedRandIndex(Map<Integer, Set<FeedbackMessage>> c1, Map<Integer, Set<FeedbackMessage>> c2,
			double[][] m, int n) {
		double t1 = 0d, t2 = 0d, t3 = 0d;
		
		// t1
		for (Entry<Integer, Set<FeedbackMessage>> entry : c1.entrySet()) {
			double combination = 0;
			if (entry.getValue().size() >= 2) {
				combination = CombinatoricsUtils.binomialCoefficient (entry.getValue().size(), 2);
			}
			t1 += combination;
		}
		
		// t2
		for (Entry<Integer, Set<FeedbackMessage>> entry : c2.entrySet()) {
			double combination = 0;
			if (entry.getValue().size() >= 2) {
				combination = CombinatoricsUtils.binomialCoefficient (entry.getValue().size(), 2);
			}
			t2 += combination;
		}
		
		// t3
		t3 = 2 * t1 * t2 / (double)(n * (n - 1));
		
		double r = 0d, sum = 0d;
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[0].length; j++) {
				if (m[i][j] >= 2) {
					sum += CombinatoricsUtils.binomialCoefficient((int)m[i][j], 2);
				}
			}
		}
		
		r = (sum - t3) / (0.5 * (t1 + t2) - t3);
		
		return r;
	}

	/**
	 * @param s11
	 * @param s01
	 * @param s10
	 * @param s00
	 * @return
	 */
	private double randIndex(Set<String> s11, Set<String> s01, Set<String> s10, Set<String> s00, int numEntries) {
//		double totalPairs = s11.size() + s00.size() + s10.size() + s01.size();
		return 2* (s11.size() + s00.size()) / (double)(numEntries * (numEntries -1 ));
	}

	
	/**
	 * @param s11
	 * @param s01
	 * @param s10
	 * @param s00
	 * @return
	 */
	private double fowlkesMallows(Set<String> s11, Set<String> s01, Set<String> s10, Set<String> s00) {
		return (double)s11.size() / Math.sqrt((s11.size() + s10.size())*(s11.size()*s01.size()));
	}
	
	/**
	 * @param s11
	 * @param s01
	 * @param s10
	 * @param s00
	 * @return
	 */
	private double jaccardIndex(Set<String> s11, Set<String> s01, Set<String> s10, Set<String> s00) {
		return (double)s11.size() / (s11.size() + s10.size() + s01.size());
	}
	
	private double[][] computeContinegencyTable(Map<FeedbackMessage, Integer> feedbackClusters, Map<Integer, Set<FeedbackMessage>> c1, Map<Integer, Set<FeedbackMessage>> c2){
		if (c1 == null) {
			c1 = new HashMap<>();
		}
		if (c2 == null) {
			c2 = new HashMap<>();
		}
		
		for (Entry<FeedbackMessage, Integer> entry : feedbackClusters.entrySet()) {
			FeedbackMessage fm = entry.getKey();
			int c1Id = entry.getValue();
			int c2Id = Integer.parseInt(entry.getKey().getClusterId());
			if (!c1.containsKey(c1Id)) {
				c1.put(c1Id, new HashSet<FeedbackMessage>());
			}
			c1.get(c1Id).add(fm);
			
			if (!c2.containsKey(c2Id)) {
				c2.put(c2Id, new HashSet<FeedbackMessage>());
			}
			c2.get(c2Id).add(fm);
		}
		
		double[][] m = new double[c1.keySet().size()][c2.keySet().size()];
		
		// build the matrix
		int i = 0;
		for (Entry<Integer, Set<FeedbackMessage>> entry1 : c1.entrySet()) {
			int j = 0;
			for (Entry<Integer, Set<FeedbackMessage>> entry2 : c2.entrySet()) {
				Set<FeedbackMessage> c2Copy = new HashSet<FeedbackMessage> (entry2.getValue());
				c2Copy.retainAll(entry1.getValue()); // intersection
				m[i][j] = c2Copy.size();
				j++;
			}
			i++;
		}
		
		return m;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		String csvPath = "trainingsets/SENERCON_userfeedback_clustering_ALL.csv"; //SENERCON_userfeedback_clustering.csv";
		String ontologyFile = "SDO_ontology_ER.ttl";
		String wordnetDbPath = null;
		String language = "en";
		
		List<FeedbackMessage> feedbackMessages = null;
		if ("de".equalsIgnoreCase(language)) {
			feedbackMessages = FeedbackAnnotator.getGermanFeedbackMessagesForClustering(csvPath);
		} else { // English
			feedbackMessages = FeedbackAnnotator.getFeedbackMessagesForClustering(csvPath);
		}
		
		FeedbackClusterer feedbackClusterer = new FeedbackClusterer(ontologyFile, wordnetDbPath, language);
		int numClusters = 40;
		Map<FeedbackMessage, Integer> feedbackClusters = feedbackClusterer.clusterFeedbackMessages2(feedbackMessages, numClusters);
		double accuracy = feedbackClusterer.computeClusteringAccuracy(feedbackClusters);
		System.out.println(accuracy);

		// save the clusters to csv
		Map<Integer, List<FeedbackMessage>> clustersForReporting = feedbackClusterer.clusterFeedbackMessages(feedbackMessages, numClusters);
		saveClusters (clustersForReporting);
	}

	/**
	 * @param clusters
	 * @throws IOException 
	 */
	private static void saveClusters(Map<Integer, List<FeedbackMessage>> clusters) throws IOException {
		String fileName = "src/test/resources/trainingsets/SENERCON_userfeedback_clustering_report.csv";
		CSVWriter csvWriter = new CSVWriter(new FileWriter(fileName));
		//write header
		String[] header = "computedCluster, feedbackText, senerconGroupId".split(",");
		csvWriter.writeNext(header);
		for (Entry<Integer, List<FeedbackMessage>> entry : clusters.entrySet()) {
			String clusterId = "Cluster" + entry.getKey();
			for (FeedbackMessage fm : entry.getValue()) {
//				String[] line = { ""+fm.getId(), fm.getMessage(), clusterId, fm.getClusterId()};
				String[] line = { clusterId, fm.getMessage(), fm.getClusterId()};
				csvWriter.writeNext(line);
			}
		}
		csvWriter.close();
	}

}
