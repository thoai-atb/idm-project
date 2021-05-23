import java.util.HashMap;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;

public class CrossValidation {
	
	public static long clusterTotalTime = 0;
	public static long nonClusterTotalTime = 0;
	
	public static void main(String[] args) throws Exception {
		DataSource source = new DataSource("src/data/transaction.arff");
		Instances dataset1 = source.getDataSet();
		int seed = 1;
		int folds = 10;
		ValidationResult result1 = new ValidationResult();
		ValidationResult result2 = new ValidationResult();
		
		RemovePercentage filter = new RemovePercentage();
		filter.setInputFormat(dataset1);
		filter.setPercentage(0);
		Instances dataset = Filter.useFilter(dataset1, filter);
		
		// randomize data
		Random rand = new Random(seed);
		//create random dataset
		Instances randData = new Instances(dataset);
		randData.randomize(rand);
		
		// perform cross-validation	    	    
		for (int n = 0; n < folds; n++) {
			ValidationResult[] results = validateFold(randData, folds, n);
			result1.read(results[0]);
			result2.read(results[1]);
		}
		System.out.println();
		System.out.println("AVERAGE: (Accuracy)");
		System.out.println("With clustering: " + result1.getAccuracy());
		System.out.println("Total time taken to build models: " + clusterTotalTime + " ms");
		System.out.println("Without clustering: " + result2.getAccuracy());
		System.out.println("Total time taken to build models: " + nonClusterTotalTime + " ms");
	}
	
	public static ValidationResult[] validateFold(Instances randData, int folds, int n) throws Exception {
		ValidationResult result1 = new ValidationResult();
		ValidationResult result2 = new ValidationResult();
		Instances train = randData.trainCV(folds, n);
		long clusterTime = 0;
		long nonClusterTime = 0;
			  
		// for the training, use the Clustering Class from Hoang to generate n clusters     
		DBScanClustering trainingClusterings = new DBScanClustering(train, 0.9, 5);
		HashMap<Integer, Instances> trainMap = trainingClusterings.getClusteredInstances();
		HashMap<Integer, AssociationManager> clusterIDtoTrainAssoMana = new HashMap<Integer, AssociationManager>();
		
		// for each number of clusters do the association mining
		for (int clusterID : trainMap.keySet()) {
			AssociationManager assoc = new AssociationManager(trainMap.get(clusterID));
			assoc.setDefault("TRUE", "FALSE");
			clusterTime += assoc.buildTime;
			clusterIDtoTrainAssoMana.put(clusterID, assoc);
		}
		
		// repeat for whole data
		AssociationManager wholeDataAssoc = new AssociationManager(train);
		wholeDataAssoc.setDefault("TRUE", "FALSE");
		nonClusterTime += wholeDataAssoc.buildTime;
		
		Instances test = randData.testCV(folds, n);
		for (int j=0; j<test.size(); j++) {
			// for each item in the test set
			Instance item = test.get(j);
			
			// use the clustering model to figure out what cluster it belongs to
			int clusterNum; 
			try {
				clusterNum = trainingClusterings.getCluster(item);
			} catch (Exception e) {
				clusterNum = -1;
			}
			
			// use the association model of that cluster to test it
			clusterIDtoTrainAssoMana.get(clusterNum).test(item, result1);
			// use the association model of the whole data to test without clustering
			wholeDataAssoc.test(item, result2);
		}
		
		double acc1 = result1.getAccuracy() * 100;
		double acc2 = result2.getAccuracy() * 100;
		System.out.println("Fold #" + (n + 1));
		System.out.println("With clustering: " + result1.correctCount + "/" + result1.total + " (" + acc1 + " %)");
		System.out.println("Time taken to build model: " + clusterTime + " ms");
		System.out.println("Without clustering: " + result2.correctCount + "/" + result2.total + " (" + acc2 + " %)");
		System.out.println("Time taken to build model: " + nonClusterTime + " ms");
		System.out.println();
		
		clusterTotalTime += clusterTime;
		nonClusterTotalTime += nonClusterTime;
		return new ValidationResult[] {result1, result2};
	}
}