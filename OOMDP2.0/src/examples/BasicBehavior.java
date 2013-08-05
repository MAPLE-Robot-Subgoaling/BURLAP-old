package examples;

import domain.gridworld.*;
import oomdptb.behavior.*;
import oomdptb.behavior.learning.*;
import oomdptb.behavior.learning.tdmethods.*;
import oomdptb.behavior.planning.*;
import oomdptb.behavior.planning.commonpolicies.GreedyQPolicy;
import oomdptb.behavior.planning.deterministic.*;
import oomdptb.behavior.planning.deterministic.informed.Heuristic;
import oomdptb.behavior.planning.deterministic.informed.astar.AStar;
import oomdptb.behavior.planning.deterministic.uninformed.bfs.BFS;
import oomdptb.behavior.planning.deterministic.uninformed.dfs.DFS;
import oomdptb.behavior.planning.stochastic.valueiteration.ValueIteration;
import oomdptb.behavior.statehashing.DiscreteStateHashFactory;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.StateParser;
import oomdptb.oomdp.TerminalFunction;
import oomdptb.oomdp.common.*;
import oomdptb.oomdp.visualizer.Visualizer;

public class BasicBehavior {

	
	
	GridWorldDomain 			gwdg;
	Domain						domain;
	StateParser 				sp;
	RewardFunction 				rf;
	TerminalFunction			tf;
	StateConditionTest			goalCondition;
	State 						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		BasicBehavior example = new BasicBehavior();
		String outputPath = "output"; //directory to record results
		
		
		//uncomment the example you want to see (and comment-out the rest)
		
		//example.QLearningExample(outputPath);
		//example.SarsaLearningExample(outputPath);
		//example.BFSExample(outputPath);
		//example.DFSExample(outputPath);
		example.AStarExample(outputPath);
		//example.ValueIterationExample(outputPath);
		
		
		//run the visualizer
		example.visualize(outputPath);

	}
	
	
	public BasicBehavior(){
		
		gwdg = new GridWorldDomain(11, 11);
		//gwdg.setProbSucceedTransitionDynamics(0.8);
		gwdg.setMapToFourRooms(); //will use the standard four rooms layout
		domain = gwdg.generateDomain();
		sp = new GridWorldStateParser(domain); //for writing states to a file
		
		rf = new UniformCostRF(); //reward always returns -1 (no positive reward on goal state either; but since the goal state ends action it will still be favored)
		tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION)); //ends when the agent reaches a location
		goalCondition = new TFGoalCondition(tf); //create a goal condition that is synonymous with the termination criteria; this is used with deterministic planners
		
		//set up the initial state
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		//set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList); //optional code line; uses only the agent position to perform hash calculations instead of the agent and all locations
		
		
		
	}
	
	
	public void visualize(String outputPath){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, gwdg.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
	}
	
	
	
	////////////////////////////////////////////BEGIN BEAHVIOR EXAMPLES/////////////////////////////////////////////////////////
	
	public void QLearningExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//creating the learning algorithm object; discount= 0.99; initialQ=0.0; learning rate=0.9
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashingFactory, 0., 0.9);
		
		//run learning for 100 episodes
		for(int i = 0; i < 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); //run learning episode
			ea.writeToFile(String.format("%se%03d", outputPath, i), sp); //record episode to a file
			System.out.println(i + ": " + ea.numTimeSteps()); //print the performance of this episode
		}
		
	}
	
	
	public void SarsaLearningExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//creating the learning algorithm object; discount= 0.99; initialQ=0.0; learning rate=0.5; lambda=1.0 (online Monte carlo at 1.0, one step at 0.0)
		LearningAgent agent = new SarsaLam(domain, rf, tf, 0.99, hashingFactory, 0., 0.5, 1.0);
		
		//run learning for 100 episodes
		for(int i = 0; i < 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); //run learning episode
			ea.writeToFile(String.format("%se%03d", outputPath, i), sp); //record episode to a file
			System.out.println(i + ": " + ea.numTimeSteps()); //print the performance of this episode
		}
		
	}
	
	
	public void BFSExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//BFS ignores reward; it just searches for a goal condition satisfying state
		DeterministicPlanner planner = new BFS(domain, goalCondition, hashingFactory); 
		planner.planFromState(initialState);
		
		//capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	
	public void DFSExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//DFS ignores reward; it just searches for a goal condition satisfying state
		DeterministicPlanner planner = new DFS(domain, goalCondition, hashingFactory);
		planner.planFromState(initialState);
		
		//capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	
	public void AStarExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//A* will need a heuristic function; lets use the Manhattan distance between the agent an the goal as an example
		Heuristic mdistHeuristic = new Heuristic() {
			
			@Override
			public double h(State s) {
				
				ObjectInstance agent = s.getObjectsOfTrueClass(GridWorldDomain.CLASSAGENT).get(0); //assume one agent
				ObjectInstance location = s.getObjectsOfTrueClass(GridWorldDomain.CLASSLOCATION).get(0); //assume one goal location in state
				
				//get agent position
				int ax = agent.getDiscValForAttribute(GridWorldDomain.ATTX);
				int ay = agent.getDiscValForAttribute(GridWorldDomain.ATTY);
				
				//get location position
				int lx = location.getDiscValForAttribute(GridWorldDomain.ATTX);
				int ly = location.getDiscValForAttribute(GridWorldDomain.ATTY);
				
				//compute Manhattan distance
				double mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
				
				return -mdist; //return the negative value since we use reward functions and negative reward is equivalent to cost
			}
		};
		
		//A* will search for a goal condition satisfying state, but also uses the reward function to keep track of the cost of states; A* expects the RF to always return negative values representing the cost
		DeterministicPlanner planner = new AStar(domain, rf, goalCondition, hashingFactory, mdistHeuristic);
		planner.planFromState(initialState);
		
		
		//capture the computed plan in a partial policy
		Policy p = new SDPlannerPolicy(planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	
	public void ValueIterationExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		
		//Value iteration computing for discount=0.99 with stopping criteria either being a maximum change in value less then 0.001 or 100 passes over the state space (which ever comes first)
		OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.001, 100);
		planner.planFromState(initialState);
		
		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	
	

}
