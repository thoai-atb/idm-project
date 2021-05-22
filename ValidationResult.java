
public class ValidationResult {
	int correctlyClassified = 0;
	int precisionDenominator = 0;
	int recallDenominator = 0;
	int truePositives = 0;
	int totalInstances = 0;
	
	public void read(String result) {
		totalInstances ++;
		if(result.equals("tp")) {
			correctlyClassified ++;
			precisionDenominator ++;
			recallDenominator ++;
			truePositives ++;
		} else if (result.equals("tn")) {
			correctlyClassified ++;
		} else if (result.equals("fp")) {
			precisionDenominator ++;
		} else if (result.equals("fn")) {
			recallDenominator ++;
		}
	}
	
	public double getAccuracy() {
		return correctlyClassified * 1.0 / totalInstances;
	}
	
	public double getPrecision() {
		return truePositives * 1.0 / precisionDenominator;
	}
	
	public double getRecall() {
		return truePositives * 1.0 / recallDenominator;
	}
}
