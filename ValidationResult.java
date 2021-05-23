
public class ValidationResult {
	int correctCount = 0;
	int total = 0;
	
	public void read(boolean correct) {
		if(correct) correctCount ++;
		total ++;
	}
	
	public void read(ValidationResult other) {
		correctCount += other.correctCount;
		total += other.total;
	}
	
	public double getAccuracy() {
		return correctCount * 1.0 / total;
	}
}
