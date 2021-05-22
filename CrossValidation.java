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
		
		//set class index to the last attribute
//		dataset.setClassIndex(dataset.numAttributes()-1);
		
		
		int seed = 1;
		int folds = 10;
		int count1 = 0;
		int count2 = 0;
		
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
			Instances train = randData.trainCV(folds, n);
				  
			// for the training, use the Clustering Class from Hoang to generate n clusters     
			DBScanClustering trainingClusterings = new DBScanClustering(train, 0.9, 5);
			
			HashMap<Integer, Instances> trainMap = trainingClusterings.getClusteredInstances();
//			AssociationManager[] TrainAssoManaArr = new AssociationManager[trainMap.size()];
			HashMap<Integer, AssociationManager> clusterIDtoTrainAssoMana = new HashMap<Integer, AssociationManager>();
			
			// for each number of clusters do the association mining
			int i = 0;
			for (int clusterID : trainMap.keySet()) {
				clusterIDtoTrainAssoMana.put(clusterID, new AssociationManager(trainMap.get(clusterID)));
			}
			
			// repeat for whole data
			i = 0;
			DBScanClustering wholeDataClusterings = new DBScanClustering(train, 0.9, 5);
			HashMap<Integer, Instances> wholeDataMap = wholeDataClusterings.getClusteredInstances();
			HashMap<Integer, AssociationManager> clusterIDtoWholeAssoMana = new HashMap<Integer, AssociationManager>();
			
			for (int clusterID : wholeDataMap.keySet()) {
				clusterIDtoWholeAssoMana.put(clusterID, new AssociationManager(wholeDataMap.get(clusterID)));
			}
			
			Instances test = randData.testCV(folds, n);
			for (int j=0; j<test.size(); j++) {
				// for each item in the test set
				Instance item = test.get(j);
				
				// use the clustering model to figure out what cluster it belongs to
				int clusterNum; 
				try {
					clusterNum = trainingClusterings.getCluster(item);
				} catch (Exception e) {
					count1--;
					count2--;
					continue;
				}
				
				
				// use the association model of that cluster to test it
				if (clusterIDtoTrainAssoMana.get(clusterNum).test(item)) count1++;
				
				// use the association model of the whole data to test
				// without clustering
				if (clusterIDtoWholeAssoMana.get(clusterNum).test(item)) count2++;
			}
		}
		System.out.println("Count1: " + count1);
		System.out.println("Count2: " + count2);
	}
}
			
//		}
		
//		//create the classifier
//		NaiveBayes nb = new NaiveBayes();
//
//		int seed = 1;
//		int folds = 15;
//		// randomize data
//		Random rand = new Random(seed);
//		//create random dataset
//		Instances randData = new Instances(dataset);
//		randData.randomize(rand);
//		//stratify	    
//		if (randData.classAttribute().isNominal())
//			randData.stratify(folds);
//
//		// perform cross-validation	    	    
//		for (int n = 0; n < folds; n++) {
//			Evaluation eval = new Evaluation(randData);
//			//get the folds	      
//			Instances train = randData.trainCV(folds, n);
//			Instances test = randData.testCV(folds, n);	      
//			// build and evaluate classifier	     
//			nb.buildClassifier(train);
//			eval.evaluateModel(nb, test);
//
//			// output evaluation
//			System.out.println();
//			System.out.println(eval.toMatrixString("=== Confusion matrix for fold " + (n+1) + "/" + folds + " ===\n"));
//			System.out.println("Correct % = "+eval.pctCorrect());
//			System.out.println("Incorrect % = "+eval.pctIncorrect());
//			System.out.println("AUC = "+eval.areaUnderROC(1));
//			System.out.println("kappa = "+eval.kappa());
//			System.out.println("MAE = "+eval.meanAbsoluteError());
//			System.out.println("RMSE = "+eval.rootMeanSquaredError());
//			System.out.println("RAE = "+eval.relativeAbsoluteError());
//			System.out.println("RRSE = "+eval.rootRelativeSquaredError());
//			System.out.println("Precision = "+eval.precision(1));
//			System.out.println("Recall = "+eval.recall(1));
//			System.out.println("fMeasure = "+eval.fMeasure(1));
//			System.out.println("Error Rate = "+eval.errorRate());
//			//the confusion matrix
//			//System.out.println(eval.toMatrixString("=== Overall Confusion Matrix ===\n"));
//		}

