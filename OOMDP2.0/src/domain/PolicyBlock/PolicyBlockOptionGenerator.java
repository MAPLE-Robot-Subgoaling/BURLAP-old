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
		
		EpisodeAnalysis e0 = episodes.get(0);
		EpisodeAnalysis e1 = episodes.get(1);
		
		EpisodeAnalysis merged = new EpisodeAnalysis();
		
		if(episodes.get(0).actionSequence.size() <= episodes.get(1).actionSequence.size()){
			for(int i = 0; i < e0.stateSequence.size(); i++){
				State s = e0.stateSequence.get(i);
				
				for(int j = 0; j < e0.stateSequence.size(); i++){
					State p = e1.stateSequence.get(j);
					
					System.out.println("\t" + i + ") " + s.equals(p));
					
					if(s.equals(p)){
						if(e0.actionSequence.size() <= i){
							break;
						}else{
						merged.stateSequence.add(e0.stateSequence.get(i));
						merged.actionSequence.add(e0.actionSequence.get(i));
						merged.rewardSequence.add(e0.rewardSequence.get(i));
						}
					}
				}
			}
		}else{
			for(int i = 0; i < e1.stateSequence.size(); i++){
				State s = e1.stateSequence.get(i);
				
				for(int j = 0; j < e1.stateSequence.size(); i++){
					State p = e0.stateSequence.get(j);
					System.out.println("\t" + i + ") " + s.equals(p));
					
					if(s.equals(p)){
						if(e1.actionSequence.size() <= i){
							break;
						}else{
						merged.stateSequence.add(e1.stateSequence.get(i));
						merged.actionSequence.add(e1.actionSequence.get(i));
						merged.rewardSequence.add(e1.rewardSequence.get(i));
						}
					}
				}
			}
			
		}
		
		System.out.println("\nMerging Done\n");
		
		for(int i = 0; i < merged.actionSequence.size(); i++){
			System.out.println("\t" + merged.actionSequence.get(i));
		}
		
		
		//visualize it.
		environ.writeEpisode(merged, "policyBlocks/");
		
	}
	
}
