import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import weka.associations.AssociationRule;
import weka.associations.FPGrowth;
import weka.associations.Item;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class AssociationManager {

	public String falseString = "n";
	public String trueString = "y";
	private FPGrowth fpGrowth;
	private List<AssociationRule> rules;
	
	public AssociationManager(Instances input) throws Exception {
		fpGrowth = new FPGrowth();
		String[] options = "-P 1 -I -1 -N 1000 -T 0 -C 0.9 -D 0.05 -U 1.0 -M 0.005".split(" ");
		fpGrowth.setOptions(options);
		fpGrowth.buildAssociations(input);
		rules = fpGrowth.getAssociationRules().getRules();
		rules.sort(new Comparator<AssociationRule>() {
			@Override
			public int compare(AssociationRule arg0, AssociationRule arg1) {
				return arg0.getTotalSupport() - arg1.getTotalSupport();
			}
		});
	}
	
	public void setDefault(String trueString, String falseString) {
		this.trueString = trueString;
		this.falseString = falseString;
	}
	
	public void test (Instance target, ValidationResult validationResult) throws Exception {
		// GET THE LIST OF ITEMS
		List<Attribute> targetAttrList = new ArrayList<Attribute>();
		for(int i = 0; i<target.numAttributes(); i++) {
			String value = target.stringValue(i);
			if(value.equals(trueString)) {
				targetAttrList.add(target.attribute(i));
			}
		}
		
		// FOR EACH SEQUENCE IN THE TESTING SET:
		for(int i = 1; i<targetAttrList.size(); i++) {
			// get the items up to i as the input
			List<Attribute> inputs = targetAttrList.subList(0, i);
			// get the target item we want to predict
			Attribute desireAttr = targetAttrList.get(i);
			// test
			test(inputs, desireAttr, validationResult);
		}
	}
	
	public void test(List<Attribute> inputs, Attribute desireAttr, ValidationResult validationResult) {
		int maxPremiseCount = -1;
		Attribute chosenAttribute = null;
		
		// FOR EACH RULE
		for(AssociationRule r : rules) {
			boolean allPremisesMet = true;
			for(Item premI : r.getPremise()) {
				boolean premiseTrue = premI.getItemValueAsString().equals(trueString);
				boolean inputsContainPremise = inputs.contains(premI.getAttribute());
				if(premiseTrue && !inputsContainPremise) {
					allPremisesMet = false;
					break;
				}
			}
			Attribute candidate = null;
			if(allPremisesMet) {
				// GET THE PREDICTION
				for(Item conI : r.getConsequence()){
					candidate = conI.getAttribute();
					if(inputs.contains(candidate)) {
						candidate = null;
						continue;
					}
					break;
				}
			}
			int premiseCount = r.getPremise().size();
			if(candidate != null && maxPremiseCount < premiseCount) {
				maxPremiseCount = premiseCount;
				chosenAttribute = candidate;
			}
		}
//		System.out.println(chosenAttribute);
		boolean correct = desireAttr.equals(chosenAttribute);
		validationResult.read(correct);
	}
	
	public void printRules() {
		for(AssociationRule r : rules) {
			System.out.println();
			System.out.println("For rule " + r);
			System.out.println("Total Support: " + r.getTotalSupport());
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
