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
		double avgAcc2 = 0;
		
		RemovePercentage filter = new RemovePercentage();
		filter.setInputFormat(dataset1);
		filter.setPercentage(50	);
		Instances dataset = Filter.useFilter(dataset1, filter);
		
		// randomize data
		Random rand = new Random(seed);
		//create random dataset
		Instances randData = new Instances(dataset);
		randData.randomize(rand);
		
		
		
		// perform cross-validation	    	    
		for (int n = 0; n < folds; n++) {
			int count1 = 0;
			int count2 = 0;
			
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
				if (clusterIDtoTrainAssoMana.get(clusterNum).test(item)) count1++;
				
				// use the association model of the whole data to test
				// without clustering
				if (wholeDataAssoc.test(item)) count2++;
			}
			
			double acc1 = count1 * 100.0 / test.size();
			double acc2 = count1 * 100.0 / test.size();
			System.out.println("Fold #" + (n + 1));
			System.out.println("With clustering: " + count1 + "/" + test.size() + " (" + acc1 + " %)");
			System.out.println("Without clustering: " + count2 + "/" + test.size() + " (" + acc2 + " %)");
			System.out.println();
			
			avgAcc1 += acc1;
			avgAcc2 += acc2;
		}
		avgAcc1 /= 10;
		avgAcc2 /= 10;
		System.out.println();
		System.out.println("AVERAGE ACCURACY: ");
		System.out.println("With clustering: " + avgAcc1 + " %");
		System.out.println("Without clustering: " + avgAcc2 + " %");
	}
}