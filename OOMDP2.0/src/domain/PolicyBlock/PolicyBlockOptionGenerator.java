package domain.PolicyBlock;

import java.util.ArrayList;
import java.util.HashMap;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;

//This class is designed to read in the options generated from the policy blocks and created options based on the 
//merge, score, and subtract method described in the Pickett & Barto Paper: PolicyBlocks

public class PolicyBlockOptionGenerator {

	PolicyBlockDomain environ;
	ArrayList<EpisodeAnalysis> episodes;
	
	//Main
	public static void main(String args[]){
		PolicyBlockOptionGenerator generator = new PolicyBlockOptionGenerator();
		generator.generatePolicies();
		generator.merge();
		generator.showEpisodes();
		
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
		
		EpisodeAnalysis e1 = episodes.get(0);
		EpisodeAnalysis e2 = episodes.get(1);
		
		EpisodeAnalysis merged = new EpisodeAnalysis();
		
		for(int i = 0; i < e1.stateSequence.size(); i++){
			State s = e1.stateSequence.get(i);
			
			for(int j = 0; j < e2.stateSequence.size(); i++){
				State p = e2.stateSequence.get(j);
				
				if(s.equals(p)){
					merged.stateSequence.add(e1.stateSequence.get(i));
					merged.actionSequence.add(e1.actionSequence.get(i));
					merged.rewardSequence.add(e1.rewardSequence.get(i));
				}
			}
		}
		
		//visualize it.
		environ.writeEpisode(merged, "policyBlocks");
		System.out.println(merged.stateSequence);
	}
	
}