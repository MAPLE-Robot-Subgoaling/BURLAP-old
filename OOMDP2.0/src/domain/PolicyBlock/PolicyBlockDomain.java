package domain.PolicyBlock;

import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.core.*;

public class PolicyBlockDomain {

	GridWorldDomain policyBlock;
	Domain domain;
	StateParser sp;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalCondition;
	State initialState;
	DiscreteStateHashFactory hashFactory;
	
	//Main Function
	public static void main(String[] args) {
		PolicyBlockDomain blocks = new PolicyBlockDomain();
		String output = "policyBlocks";
		blocks.QLearn(output);
		blocks.visualize(output);
	}
	
	public PolicyBlockDomain(){
		//create the gridworld
		policyBlock = new GridWorldDomain(11,11);
		policyBlock.setMapToFourRooms();
		domain = policyBlock.generateDomain();
		
		//define the parser, reward, and termination conditions
		sp = new GridWorldStateParser(domain);
		rf = new UniformCostRF();
		tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION));
		goalCondition = new TFGoalCondition(tf);
		
		//set up initial state
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		//set up the state hashing system
		hashFactory = new DiscreteStateHashFactory();
		hashFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList); //uses agent position to hash
	}
	
	//create the visualizer for the policyBlock
	public void visualize(String output){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, policyBlock.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, output);
	}
	
	//Learning Algorithm
	public void QLearn(String output){
		if(!output.endsWith("/")){
			output = output + "/";
		}
		
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashFactory, 0., 0.9); //create the QLearning agent
		
		for(int i = 0; i < 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); //run the episode
			ea.writeToFile(String.format("%se%03d", output, i), sp); //record the episode
			System.out.println("Episode "+ i + " : " + ea.numTimeSteps()); //print the performance of the episode
		}
	}

}
