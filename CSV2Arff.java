import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import weka.filters.Filter;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.filters.unsupervised.attribute.Remove;
public class CSV2Arff {
	public static void main(String[] args) throws Exception {
		// load CSV
		CSVLoader loader = new CSVLoader();
		loader.setSource(new File("src/data/transaction.csv"));
		Instances data = loader.getDataSet();
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
		String[] opts = new String[] {"-R","1,2"};
		ArrayList<String> ar = new ArrayList<String>();
		Remove remove = new Remove();
		remove.setOptions(opts);
		remove.setInputFormat(data);
		Instances removeData = Filter.useFilter(data,remove); // delete 2 Attribute useless
		Instances dataTransaction = null ;
		Attribute attribute = removeData.attribute(0); // read attribute from data
		for (int i = 0;i<attribute.numValues();i++) {
			String str = attribute.value(i); //get type of attribute
			String[] tokens = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);	//split ","
			for(String t : tokens) {
				ar.add(t);  //save each type to array
			}
		}
		LinkedHashSet<String> tempAr =  new LinkedHashSet<String>(ar); // delete exist type
		ArrayList<String> attAr = new ArrayList<String>(tempAr); //move array to new ArrayList
		for (int i = 0;i<attAr.size();i++) {
			List<String> type = new ArrayList<String>(2); 
			type.add("TRUE"); 
			type.add("FALSE");
			Attribute A = new Attribute(attAr.get(i),type); // create Attribute
			attributeList.add(A);
		}
		dataTransaction = new Instances("transaction",attributeList,0); // create new Instances and add attributeList
		for (int i = 0;i < attribute.numValues();i++) {
			String[] tokens=null;
			String str = attribute.value(i);
			tokens = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); 	
			double[] instanceValue = new double[dataTransaction.numAttributes()]; // declare instanceValue
			for(int j=0;j<attAr.size();j++) {	
				int check = 0;
				for(int k=0;k<tokens.length;k++) {	        		
					if(attAr.get(j).equalsIgnoreCase(tokens[k])) {						
						check=1;
					}
				}
				if(check==1) {
					instanceValue[j] = 0;}
				else {
					instanceValue[j] = 1;
				}
			}
			dataTransaction.add(new DenseInstance(1.0, instanceValue)); //add instanceValue to data
		}
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataTransaction);
		saver.setFile(new File("src/data/transaction.arff")); //output file arff
		saver.writeBatch();
	}
}
