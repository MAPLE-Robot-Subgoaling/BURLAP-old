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
		
		int sizeE1 = e1.stateSequence.size();
		int sizeE2 = e2.stateSequence.size();
		
		EpisodeAnalysis mergedEpisodes = new EpisodeAnalysis();
		
		if(sizeE1 > sizeE2){
			//vanilla check?
			for(int i = 0; i < sizeE2; i++){
				if(e1.stateSequence.get(i).equals(e2.stateSequence.get(i))){
					mergedEpisodes.stateSequence.add(e1.stateSequence.get(i));
					mergedEpisodes.actionSequence.add(e1.actionSequence.get(i));
					mergedEpisodes.rewardSequence.add(e1.rewardSequence.get(i));
				}
			}
		}else{
			//vanilla check?
			for(int i = 0; i < sizeE1; i++){
				if(e1.stateSequence.get(i).equals(e2.stateSequence.get(i))){
					mergedEpisodes.stateSequence.add(e1.stateSequence.get(i));
					mergedEpisodes.actionSequence.add(e1.actionSequence.get(i));
					mergedEpisodes.rewardSequence.add(e1.rewardSequence.get(i));
				}
			}
		}
		
		//visualize it.
		//environ.writeEpisode(mergedEpisodes, "policyBlocks");
		System.out.println(mergedEpisodes.stateSequence);
	}
	
}
