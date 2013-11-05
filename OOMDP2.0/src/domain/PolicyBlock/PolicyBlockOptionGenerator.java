package domain.PolicyBlock;

import java.util.HashMap;

import burlap.behavior.singleagent.Policy;

//This class is designed to read in the options generated from the policy blocks and created options based on the 
//merge, score, and subtract method described in the Pickett & Barto Paper: PolicyBlocks

public class PolicyBlockOptionGenerator {

	PolicyBlockDomain environ;
	HashMap<String, Policy> policySet;
	HashMap<String, Policy> mergeSet = new HashMap<String, Policy>();
	
	//Main
	public static void main(String args[]){
		PolicyBlockOptionGenerator generator = new PolicyBlockOptionGenerator();
		generator.generatePolicies();
		generator.getPolicyMap();
		generator.merge();
	}
	
	//creates a new Policy Domain Object
	public PolicyBlockOptionGenerator(){
		environ = new PolicyBlockDomain();
	}
	
	//Generates 5 iterations which contains 100 policies run via Q-Learning
	public void generatePolicies(){	
		for(int i = 0; i < 5; i++){
			//environ.QLearn("policyBlocks/" +  i + "-set/");
			environ.computePolicy(i +"-set");
		}
		//environ.visualize("policyBlocks/2-set/");
	}
	
	public void getPolicyMap(){
		policySet = environ.getPolicyMap();
	}
	
	public void merge(){
		
	}
}
