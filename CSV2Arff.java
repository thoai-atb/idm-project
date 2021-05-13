package weka.api;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import weka.filters.Filter;
import weka.core.Attribute;
import weka.core.DenseInstance;

import java.lang.Object;
import weka.filters.unsupervised.attribute.Remove;
public class CSV2Arff {
	public static void main(String[] args) throws Exception {
	    // load CSV
	    CSVLoader loader = new CSVLoader();
	    
	    loader.setSource(new File("/Users/po/Desktop/idm-project/data/transaction.csv"));
	    Instances data = loader.getDataSet();
	    ArrayList<Attribute> atts = new ArrayList<Attribute>();
	    String[] opts = new String[] {"-R","1,2"};
	    // save ARFF
	    ArrayList<String> ar = new ArrayList<String>();
	    Remove remove = new Remove();
	    remove.setOptions(opts);
	    remove.setInputFormat(data);
	    Instances removeData = Filter.useFilter(data,remove);
	    Instances dataRaw = null ;
	    Attribute attribute = removeData.attribute(0);
	    for (int i = 0;i<attribute.numValues();i++) {
	    	String str = attribute.value(i);
	    	String[] tokens = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);	 
	    	
	        for(String t : tokens) {
	        	ar.add(t);
	        }
	    }
	    LinkedHashSet<String> tempAr =  
                new LinkedHashSet<String>(ar);
	    ArrayList<String> attAr = new ArrayList<String>(tempAr);
	    for (int i = 0;i<attAr.size();i++) {
	    List my_nominal_values = new ArrayList(2); 
	 	    my_nominal_values.add("TRUE"); 
	 	    my_nominal_values.add("FALSE");
	 	   Attribute B = new Attribute(attAr.get(i),my_nominal_values);
	 	 atts.add(B);
	 	 dataRaw = new Instances("transaction",atts,0);
	    }
	    
	    
	    for (int i = 0;i < attribute.numValues();i++) {
	    	String[] tokens=null;
	    String str = attribute.value(i);
	 	tokens = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); 	
	 	for(int j=0;j<attAr.size();j++) {
	 	double[] instanceValue = new double[dataRaw.numAttributes()];
        	for(int k=0;k<tokens.length;k++) {
//        		System.out.println(attAr.get(j));
//        		System.out.println(tokens[k]);
        		System.out.println(attAr.get(j).equalsIgnoreCase(tokens[k]));
        	if(attAr.get(j).equalsIgnoreCase(tokens[k])) {
        	 	instanceValue[j] = 0;
        	} else {
        	 	instanceValue[j] = 1;
        		}
        	}
        dataRaw.add(new DenseInstance(1.0, instanceValue));
	 		}
        }
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(dataRaw);
	    saver.setFile(new File("/Users/po/Desktop/idm-project/data/transaction1.arff"));
	    saver.writeBatch();
	  }
}
