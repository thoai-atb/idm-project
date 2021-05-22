import java.util.HashMap;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;

public class CrossValidation {
	public static void main(String[] args) throws Exception {
		DataSource source = new DataSource("src/data/transaction.arff");
		Instances dataset1 = source.getDataSet();
		int seed = 1;
		int folds = 10;
		double avgAcc1 = 0;
		double avgRec1 = 0;
		double avgPre1 = 0;
		double avgAcc2 = 0;
		double avgRec2 = 0;
		double avgPre2 = 0;
		
		RemovePercentage filter = new RemovePercentage();
		filter.setInputFormat(dataset1);
		filter.setPercentage(50);
		Instances dataset = Filter.useFilter(dataset1, filter);
		
		// randomize data
		Random rand = new Random(seed);
		//create random dataset
		Instances randData = new Instances(dataset);
		randData.randomize(rand);
		
		// perform cross-validation	    	    
		for (int n = 0; n < folds; n++) {
			ValidationResult[] results = validateFold(randData, folds, n);
			avgAcc1 += results[0].getAccuracy();
			avgRec1 += results[0].getRecall();
			avgPre1 += results[0].getPrecision();
			avgAcc2 += results[1].getAccuracy();
			avgRec2 += results[1].getRecall();
			avgPre2 += results[1].getPrecision();
		}
		avgAcc1 /= folds;
		avgRec1 /= folds;
		avgPre1 /= folds;
		avgAcc2 /= folds;
		avgRec2 /= folds;
		avgPre2 /= folds;
		System.out.println();
		System.out.println("AVERAGE: (Accuracy - Recall - Precision)");
		System.out.println("With clustering: " + avgAcc1 + " " + avgRec1 + " " + avgPre1);
		System.out.println("Without clustering: " + avgAcc2 + " " + avgRec2 + " " + avgPre2);
	}
	
	public static ValidationResult[] validateFold(Instances randData, int folds, int n) throws Exception {
		ValidationResult result1 = new ValidationResult();
		ValidationResult result2 = new ValidationResult();
		Instances train = randData.trainCV(folds, n);
			  
		// for the training, use the Clustering Class from Hoang to generate n clusters     
		DBScanClustering trainingClusterings = new DBScanClustering(train, 0.9, 5);
		HashMap<Integer, Instances> trainMap = trainingClusterings.getClusteredInstances();
		HashMap<Integer, AssociationManager> clusterIDtoTrainAssoMana = new HashMap<Integer, AssociationManager>();
		
		// for each number of clusters do the association mining
		for (int clusterID : trainMap.keySet()) {
			AssociationManager assoc = new AssociationManager(trainMap.get(clusterID));
			assoc.setDefault("FALSE");
			clusterIDtoTrainAssoMana.put(clusterID, assoc);
		}
		
		// repeat for whole data
		AssociationManager wholeDataAssoc = new AssociationManager(train);
		wholeDataAssoc.setDefault("FALSE");
		
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
			result1.read(clusterIDtoTrainAssoMana.get(clusterNum).test(item));
			// use the association model of the whole data to test without clustering
			result2.read(wholeDataAssoc.test(item));
		}
		
		double acc1 = result1.getAccuracy() * 100;
		double acc2 = result2.getAccuracy() * 100;
		System.out.println("Fold #" + (n + 1));
		System.out.println("With clustering: " + result1.correctlyClassified + "/" + result1.totalInstances + " (" + acc1 + " %)");
		System.out.println("Recall: " + result1.getRecall() + ", Precision: " + result1.getPrecision());
		System.out.println("Without clustering: " + result2.correctlyClassified + "/" + result2.totalInstances + " (" + acc2 + " %)");
		System.out.println("Recall: " + result2.getRecall() + ", Precision: " + result2.getPrecision());
		System.out.println();
		
		return new ValidationResult[] {result1, result2};
	}
}