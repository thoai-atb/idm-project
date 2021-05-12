import java.util.Collection;
import java.util.List;
import java.util.Random;

import weka.associations.AssociationRule;
import weka.associations.FPGrowth;
import weka.associations.Item;
import weka.associations.NominalItem;
import weka.core.Instance;
import weka.core.Instances;

public class AssociationManager {
	
	private FPGrowth fpGrowth;
	private boolean log = false;
	private String defaultValue = "n";
	
	public AssociationManager(Instances input) throws Exception {
		fpGrowth = new FPGrowth();
		fpGrowth.buildAssociations(input);
	}
	
	public void setLog(boolean log) {
		this.log = log;
	}
	
	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public boolean test(Instance target) throws Exception {
		// create random generator
		Random rand = new Random();
		int randomIdx = rand.nextInt(target.numAttributes());
		
		// pick a value and remove it from the target
		String valueAtMissing = target.stringValue(randomIdx);
		target.setMissing(randomIdx);
		
		String prediction = defaultValue;
		
		boolean found = false;
		for(AssociationRule r : fpGrowth.getAssociationRules().getRules()) {
			for(Item item : r.getConsequence()) {
				// Check if r appears in the right side of the rule
				if(item.getAttribute().index() != randomIdx)
					continue;
				
				// Check if the premises are met
				boolean allMet = true;
				for(Item premI : r.getPremise()) {
					NominalItem prem = (NominalItem) premI;
					int premIdx = prem.getAttribute().index();
					String src = target.stringValue(premIdx);
					String cur = prem.getItemValueAsString();
					if (src.compareTo(cur) != 0) {
						allMet = false;
						break;
					}
				}
				
				if(allMet) {
					found = true;
					prediction = item.getItemValueAsString();
					break;
				}
			}
			
			if(found)
				break;
		}
		
		boolean result = prediction.equals(valueAtMissing);
	
		if(log) {
			System.out.println(target);
			System.out.println(randomIdx + " " + valueAtMissing);
			System.out.println("predicted: " + prediction);
			System.out.println("correct: " + result);
			System.out.println();
		}
		
		return result;
	}
	
	public void printRules() {
		List<AssociationRule> rules = fpGrowth.getAssociationRules().getRules();
		for(AssociationRule r : rules) {
			System.out.println();
			System.out.println("For rule " + r);
			System.out.println("Premises: ");
			Collection<Item> prem = r.getPremise();
			for(Item item: prem)
				System.out.println("\t" + item);
			System.out.println("Consequences: ");
			Collection<Item> cons = r.getConsequence();
			for(Item item : cons)
				System.out.println("\t" + item);
		}
	}
}
