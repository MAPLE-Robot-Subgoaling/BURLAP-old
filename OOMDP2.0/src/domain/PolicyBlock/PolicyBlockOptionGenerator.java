package domain.PolicyBlock;

//This class is designed to read in the options generated from the policy blocks and created options based on the 
//merge, score, and subtract method described in the Pickett & Barto Paper: PolicyBlocks

public class PolicyBlockOptionGenerator {

	PolicyBlockDomain environ;
	
	public static void main(String args[]){
		PolicyBlockOptionGenerator generator = new PolicyBlockOptionGenerator();
		generator.generatePolicies();
	}
	
	public PolicyBlockOptionGenerator(){
		environ = new PolicyBlockDomain();
	}
	
	public void generatePolicies(){
		
		for(int i = 0; i < 5; i++){
			environ.QLearn("policyBlocks/" +  i + "-set/");
		}
		
		environ.visualize("policyBlocks/2-set/");
	}
}
