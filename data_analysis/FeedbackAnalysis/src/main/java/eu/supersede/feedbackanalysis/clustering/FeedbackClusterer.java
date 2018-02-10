/**
 * 
 */
package eu.supersede.feedbackanalysis.clustering;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;

import eu.supersede.feedbackanalysis.preprocessing.utils.FileManager;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.ManhattanDistance;
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
	Instances filteredInstances;

	/**
	 * 
	 */
	public FeedbackClusterer(String dataset, boolean arff, boolean file, double percent) {
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

			// set classindex
			instances.setClassIndex(instances.numAttributes() - 2);
			
			weka.filters.unsupervised.attribute.Remove filter = new weka.filters.unsupervised.attribute.Remove();
			filter.setAttributeIndices((instances.classIndex() + 1) + "-last");
			filter.setInputFormat(instances);
			filteredInstances = Filter.useFilter(instances, filter);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to load dataset");
		}

	}

	
	public void computeClusters(int numClusters) {

		SimpleKMeans clusterer = new SimpleKMeans();
		try {
			clusterer.setNumClusters(numClusters);
			clusterer.setMaxIterations(100);
			DistanceFunction df = new ManhattanDistance(filteredInstances);
			clusterer.setDistanceFunction(df);
			clusterer.buildClusterer(filteredInstances);

			// evaluate the clusters
			ClusterEvaluation eval = new ClusterEvaluation();
			eval.setClusterer(clusterer);
			eval.evaluateClusterer(instances);
			
			// print results
			System.err.println(eval.clusterResultsToString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Instances computeNearestNeighbors (int k) {
		Instances nearestNeighbours = null;
		try {
			Instance t = filteredInstances.remove(0);
			NearestNeighbourSearch nn = new LinearNNSearch(filteredInstances);
			nn.setDistanceFunction(new ManhattanDistance(filteredInstances));
			
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
		String dataset = "SDO_ontology.ttl.fv.csv";
		boolean arff = false;
		boolean file = true;
		double percent = 0.7;
		FeedbackClusterer fCluster = new FeedbackClusterer(dataset, arff, file, percent);
		fCluster.computeClusters(5);
		
		Instances nearestNeighbors = fCluster.computeNearestNeighbors(5);

	}

}
