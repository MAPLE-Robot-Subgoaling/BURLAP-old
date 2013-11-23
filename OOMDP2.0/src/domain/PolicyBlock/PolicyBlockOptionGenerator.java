package domain.PolicyBlock;

import java.util.ArrayList;
import java.util.HashMap;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;

//This class is designed to read in the options generated from the policy blocks and created options based on the 
//merge, score, and subtract method described in the Pickett & Barto Paper: PolicyBlocks

public class PolicyBlockOptionGenerator {

	PolicyBlockDomain environ;
	ArrayList<EpisodeAnalysis> episodes;
	
	//Main
	public static void main(String args[]){
		PolicyBlockOptionGenerator generator = new PolicyBlockOptionGenerator();
		generator.generatePolicies();
		generator.showEpisodes();
	//	generator.merge();
	}
	
	//creates a new Policy Domain Object
	public PolicyBlockOptionGenerator(){
		environ = new PolicyBlockDomain();
	}
	
	//Generates 5 iterations which contains 100 policies run via Q-Learning
	public void generatePolicies(){	
		environ.createEpisodes("policyBlocks");
	}
	
	public void showEpisodes(){
		environ.visualize("policyBlocks");
	}
	
	public void merge(){
		this.episodes = environ.episodes;
		
		
	}
}
