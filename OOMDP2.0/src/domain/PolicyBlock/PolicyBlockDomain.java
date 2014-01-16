package domain.PolicyBlock;

/**
 * PolicyBlockDomain()
 * Just to generate policies of four rooms for the option generator.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteMaskHashingFactory;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.*;

public class PolicyBlockDomain {

	GridWorldDomain policyBlock;
	Domain domain;
	StateParser sp;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalCondition;
	State initialState;
	DiscreteMaskHashingFactory hashFactory;
	HashMap<List<State>, Policy> stateSpace;
	HashMap<Collection<StateHashTuple>, Policy> hashStateSpace;
	ArrayList<EpisodeAnalysis> episodes;
	OOMDPPlanner planner;
	double DISCOUNTFACTOR = 0.99;
	
	
	//Main Function
	public static void main(String[] args) {
		PolicyBlockDomain blocks = new PolicyBlockDomain();
		String output = "policyBlocks";
		//blocks.QLearn(output);
		blocks.computePolicy("policy-", 4, "outputPolicyBlocks");
		blocks.visualize(output);
	}
	
	public PolicyBlockDomain(){
		//create the gridworld
		policyBlock = new GridWorldDomain(11,11);
		policyBlock.setMapToFourRooms();
		domain = policyBlock.generateDomain();
		stateSpace = new HashMap<List<State>, Policy>();
		hashStateSpace = new HashMap<Collection<StateHashTuple>, Policy>();
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
		hashFactory = new DiscreteMaskHashingFactory();
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
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); 	//run the episode
			ea.writeToFile(String.format("%se%03d", output, i), sp); 			//record the episode
			System.out.println("Episode "+ i + " : " + ea.numTimeSteps()); 		//print the performance of the episode
		}
	}
	
	//Write the episode to the file
	public void writeEpisode(EpisodeAnalysis obj, String output){
		int i = 500;
		obj.writeToFile(String.format("%se%03d", output, i), sp); 				//record the episode
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
		
		//declarations
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashFactory, 0., 0.9); //create the QLearning agent
		EpisodeAnalysis one = new EpisodeAnalysis();
		
		
		//testing with variable number of episodes
		for(int k = 0; k < number; k++)
		{
			setGoal(10-((int)(Math.random()*4)),10-((int)(Math.random()*5)));
			for(int j = 0; j < 100; j++){
				one = agent.runLearningEpisodeFrom(initialState); //run the episode
			}
			episodes.add(one);
			System.out.println("Done: " + (k+1));
		}
	}
	
	//policy computer - for later stuff
	public void computePolicy(String str, int number, String outputPath){
	
		//setup the filepath
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		for(int k =  0; k < number; k++){
			
			setAgent(0,0);
			setGoal(10-((int)(Math.random()*4)),10-((int)(Math.random()*5)));
			ValueFunctionPlanner plan = new ValueIteration(domain, rf, tf, DISCOUNTFACTOR, hashFactory, 0.001, 100);
			plan.planFromState(initialState);
			
			//collects the state list
			List<State> states = plan.getAllStates();
			List<StateHashTuple> hashStateTuple = new ArrayList<StateHashTuple>();
			
			//hashes the state according to the attributes defined
			for(State s:states){
				hashStateTuple.add(plan.stateHash(s));
			}
			
			//policy object
			Policy p = new GreedyDeterministicQPolicy((QComputablePlanner)plan);
			p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + str + k, sp);
			
			//saves the collection/list and policy object to hash map
			stateSpace.put(states, p);
			hashStateSpace.put(hashStateTuple, p);
		}
		
	}
	
	public void showPolicy(StateHashTuple s, PolicyBlockPolicy p, String output, int numSteps){
		String str = "merged";
		p.justDoIt().writeToFile(output + str, sp);
		System.out.println("Written to the file.");
		this.visualizePolicies(output);
	}
	
	public HashMap<List<State>, Policy> getPolicyMap(){
		return stateSpace;
	}
	
	public HashMap<Collection<StateHashTuple>, Policy> getHashPolicyMap(){
		return hashStateSpace;
	}
	
	public void visualizePolicies(String outputPath){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, policyBlock.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
	}
}
