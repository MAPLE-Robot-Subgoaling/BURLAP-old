package domain.PolicyBlock;

/**
 * PolicyBlockDomain
 * 		The main Domain Class used to generate the Trajectory and 
 * 		Policy objects needed for running Trajectory merge, and Policy
 * 		Merge as well. 
 * 
 * 		The interfaces define the functions needed to run the functions
 * 		nessecary for creating the policy blocks. 
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
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.stocashticgames.Agent;
import burlap.oomdp.visualizer.Visualizer;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.behavior.PolicyBlock.PolicyBlockPolicy;
import burlap.behavior.PolicyBlock.TrajectoryInterface;
import burlap.behavior.PolicyBlock.TrajectoryPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
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

public class PolicyBlockDomain implements TrajectoryInterface{

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
	public ArrayList<EpisodeAnalysis> episodes;
	OOMDPPlanner planner;
	double DISCOUNTFACTOR = 0.99;
	
	
	/**
	 * PolicyBlockDomain() - Constructor Object
     * Policy Blocks Domain is derived from the Four Rooms Domain Object, created with the idea of running
     * the policy blocks algorithm indicated by Pickett and Barto. The Domain is independant of the algorithm
     * and only creates the policy & trajectory objects used in the Policy Blocks Domain.
	 */
	public PolicyBlockDomain(){
		
		//create the gridworld 
		policyBlock = new GridWorldDomain(11,11);
		policyBlock.setMapToFourRooms();
		domain = policyBlock.generateDomain();
		
		//defined the state spaces designed to hold the merged items via policy blocks
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
		
		//set up the state hashing system - this discretises the state 
		//to merge according to the attributes by the agent
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
		
		for(int i = 1; i <= 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); 	//run the episode
			ea.writeToFile(String.format("%se%03d", output, i), sp); 			//record the episode
			//System.out.println("Episode "+ i + " : " + ea.numTimeSteps()); 		//print the performance of the episode
			System.out.print(".");
			if(i % 10 == 0)
				System.out.println();
		}
	}
	
	//Write the episode to the file
	public void writeEpisode(EpisodeAnalysis obj, String output){
		int i = 500;
		obj.writeToFile(String.format("%se%03d", output, i), sp); 				//record the episode
	}
	
	/**
	 * createEpisodes() - for trajectory merging via PolicyBlocksOptionGenerator. This merges object  
	 * according to trajectories determined after running Q-Learning, and build options based off of that. 
	 * @param output - the filepath for printing the results
	 * @param number - number of episodes to create
	 * 
	 * Note:
	 * 		switch to a Planner such as Value Iteration...
	 */
	public void createEpisodes(String output, int number){
		
		//setup the filepath
		if(!output.endsWith("/")){
			output = output + "/";
		}
		
		//declarations
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashFactory, 0., 0.9); //create the QLearning agent
		EpisodeAnalysis one = new EpisodeAnalysis();
		
		
		/*
		 * Currently this generates n number of episodes, n defined by the user.
		 * The episode is overritten 100 times, and then the final iteration (whether optimal
		 * or not) is passed in the end. 
		 * 
		 * Plan to switch to add all episodes from 1 to n. 
		 */
		for(int k = 0; k < number; k++)
		{
			setGoal(10-((int)(Math.random()*4)),10-((int)(Math.random()*5))); //creates a random goal state in the 4th room
			for(int j = 0; j < 100; j++){
				one = agent.runLearningEpisodeFrom(initialState); //run the episode
			}
			episodes.add(one); //add the episode to the set of trajectories
			System.out.println("Done: " + (k+1));
		}
		
		
		//Working on the new policies to generate...
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashFactory, 0.001, 100);
		planner.planFromState(initialState);
		
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		p.evaluateBehavior(initialState, rf, tf).writeToFile(output, sp);
		
	}
	
	/**
	 * computePolicy() - for policy merging via PolicyBlockPolicyGenerator. This is used to merge full policies
	 * generated by Value Iteration, and then passed onto the PB generator for merging, and build options based
	 * off of the result of the mergings
	 * @param str - name of each policy
	 * @param number - number of policies to generate
	 * @param outputPath - filepath to visualize policies. 
	 */
	public void computePolicy(String str, int number, String outputPath){
	
		//setup the filepath
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		/*
		 * Currently generates n policies, where n is the number of polices passed in by the user
		 */
		for(int k =  0; k < number; k++){
			
			//setting up the intial states of the Policy
			setAgent(0,0);
			setGoal(10-((int)(Math.random()*4)),10-((int)(Math.random()*5)));
			
			//defining and running the VI Planner
			ValueFunctionPlanner plan = new ValueIteration(domain, rf, tf, DISCOUNTFACTOR, hashFactory, 0.001, 100);
			plan.planFromState(initialState);
			
			//collects the state list
			List<State> states = plan.getAllStates();
			List<StateHashTuple> hashStateTuple = new ArrayList<StateHashTuple>();
			
			//hashes the state according to the attributes defined
			for(State s:states){
				hashStateTuple.add(plan.stateHash(s));
			}
			
			//Create the Policy Object from the Planner
			Policy p = new GreedyDeterministicQPolicy((QComputablePlanner)plan);
			p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + str + k, sp);
			
			//saves the collection/list and policy object to hash map
			stateSpace.put(states, p);
			hashStateSpace.put(hashStateTuple, p);
		}
		
	}
	
	//shows the policies from the output - Must contain a PolicyBlockPolicy to run
	//Note: modify name for mutiple merged Policies.
	public void writePolicy(PolicyBlockPolicy p, String output){
		p.justDoIt().writeToFile(output, sp);
	}
	
	/**
	 * For Trajectory Merge
	 * @param t
	 * @param output
	 */
	public void writeTrajectory(TrajectoryPolicy t, String output){
		t.justDoIt().writeToFile(output, sp);
	}
	
	//collects the map of states to policies
	public HashMap<List<State>, Policy> getPolicyMap(){
		return stateSpace;
	}
	
	//collected the map of hashed states to policies
	public HashMap<Collection<StateHashTuple>, Policy> getHashPolicyMap(){
		return hashStateSpace;
	}
	
	//basic visualizer
	public void visualizePolicies(String outputPath){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, policyBlock.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
	}
	
	
	public void addOptions(ArrayList<Option> options){
		 for(Option o:options){
			 domain.addAction(o);
		 }
		 
		 System.out.println("\tPrinting all Actions/Options:");
		 
		 for(Action a:domain.getActions()){
			 System.out.println("\t\t--" + a.getName());
		 }
	}
}
