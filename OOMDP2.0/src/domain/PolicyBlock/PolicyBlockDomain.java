package domain.PolicyBlock;

/**
 * PolicyBlockDomain()
 * Just to generate policies of four rooms for the option generator.
 */

import java.util.ArrayList;
import java.util.HashMap;

import domain.fourroomsdomain.FourRooms;

import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;	
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.stocashticgames.Agent;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
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
	HashMap<String,Policy> policies;
	ArrayList<EpisodeAnalysis> episodes;
	OOMDPPlanner planner;
	double DISCOUNTFACTOR = 0.99;
	
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
		policies = new HashMap<String, Policy>();
		episodes = new ArrayList<EpisodeAnalysis>();
		
		//define the parser, reward, and termination conditions
		sp = new GridWorldStateParser(domain);
		rf = new UniformCostRF();
		tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION));
		goalCondition = new TFGoalCondition(tf);
		
		//set up initial state
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		setAgent(0,0);
		setGoal(10,10);
		
		//set up the state hashing system
		hashFactory = new DiscreteStateHashFactory();
		hashFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList); //uses agent position to hash
	}
	
	//create the visualizer for the policyBlock
	public void visualize(String output){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, policyBlock.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, output);
	}
	
	//sets up the agent's inital position
	public void setAgent(int x, int y){
		GridWorldDomain.setAgent(initialState, x, y);
	}
	
	//sets up the agent's final position
	public void setGoal(int x, int y){
		GridWorldDomain.setLocation(initialState, 0, x, y);
	}
	
	//Learning Algorithm - Q-learning
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
	
	//Write the episode to the file
	public void writeEpisode(EpisodeAnalysis obj, String output){
		int i = 500;
		obj.writeToFile(String.format("%se%03d", output, i), sp); //record the episode
	}
	
	//called from OptionGenerator
	/*
	 * This is the main thing you should worry about. Here is where the two policies are generated.
	 */
	public void createEpisodes(String output, int number){
		
		//setup the filepath
		if(!output.endsWith("/")){
			output = output + "/";
		}
		
		int i = 0;
		
		//declarations
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashFactory, 0., 0.9); //create the QLearning agent
		EpisodeAnalysis one = new EpisodeAnalysis();
		EpisodeAnalysis two = new EpisodeAnalysis();
		
		
		//testing with variable number of episodes
		for(int k = 0; k < number; k++)
		{
			setGoal(10-((int)(Math.random()*3)),10-((int)(Math.random()*3)));
			for(int j = 0; j < 100; j++){
				one = agent.runLearningEpisodeFrom(initialState); //run the episode
			}
			episodes.add(one);
			System.out.println("Done: " + (k+1));
		}
		/*
		//for the first episode - keeps overwriting the episode 100 times (you may not get the most optimal one)
		setGoal(10, 10);
		for(int j = 0; j < 100; j++){
			one = agent.runLearningEpisodeFrom(initialState); //run the episode
		}
		
		//saves the episode to a file
		episodes.add(one);
		one.writeToFile(String.format("%se%03d", output, i), sp); //record the episode
		System.out.println("0) Goal 10-10 : " + one.numTimeSteps()); //print the performance of the episode
		
		i++;
		
		//for the second episode
		setGoal(10, 8);
		for(int j = 0; j < 100; j++){
			two = agent.runLearningEpisodeFrom(initialState); //run the episode
		}
		
		//saves the episode to a file
		episodes.add(two);
		two.writeToFile(String.format("%se%03d", output, i), sp); //record the episode
		System.out.println("1) Goal 10-8 : " + two.numTimeSteps()); //print the performance of the episode
		*/
	}
	
	//policy computer - for later stuff
	public void computePolicy(String str){
		planner = new ValueIteration(domain, rf, tf, DISCOUNTFACTOR, hashFactory, 0.001, 100);
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		policies.put(str, p);
	}
	
	public HashMap<String, Policy> getPolicyMap(){
		return policies;
	}
}
