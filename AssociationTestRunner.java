import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;

public class AssociationTestRunner {	
	
	public static void main(String[] args) throws Exception {
		// Load Data
		String path = "src/data/vote.arff";
		DataSource source = new DataSource(path);
		Instances data = source.getDataSet();
		data.deleteAttributeAt(data.numAttributes() - 1);
		System.out.println("Size of whole dataset: " + data.size());
		
		// Randomize Data
		data.randomize(new Random());
		
		// Generate Training Set
		RemovePercentage filter = new RemovePercentage();
		filter.setInputFormat(data);
		filter.setPercentage(20);
		Instances trainData = Filter.useFilter(data, filter);
		System.out.println("Size of training set: " + trainData.size());
		
		// Generate Testing Set
		filter = new RemovePercentage();
		filter.setInputFormat(data);
		filter.setPercentage(20);
		filter.setInvertSelection(true);
		Instances testData = Filter.useFilter(data, filter);
		System.out.println("Size of testing set: " + testData.size());
		
		// Apply FPGrowth Model
		AssociationManager assMan = new AssociationManager(trainData);
		ValidationResult validationResult = new ValidationResult();
		for(int i = 0; i<testData.numInstances(); i++) {
			Instance ins = testData.instance(i);
			String result = assMan.test(ins);
			validationResult.read(result);	
		}
		
		int correct = validationResult.correctlyClassified;
		int total = validationResult.totalInstances;
		double accuracy = validationResult.getAccuracy();
		double precision = validationResult.getPrecision();
		double recall = validationResult.getRecall();
		
		System.out.println();
		System.out.println("Test result: " + correct + "/" + total + " ratio: " + accuracy);
		System.out.println("Precision: " + precision);
		System.out.println("Recall: " + recall);
	}
}
