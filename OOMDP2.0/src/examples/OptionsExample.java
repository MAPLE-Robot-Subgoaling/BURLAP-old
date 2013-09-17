package examples;

import java.util.Iterator;

import burlap.behavior.*;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.*;
import burlap.behavior.singleagent.options.*;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.StateConditionTestIterable;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteMaskHashingFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class OptionsExample extends BasicBehavior{

	
	

	
	public static void main(String[] args) {
		
		
		OptionsExample example = new OptionsExample();
		String outputPath = "output"; //directory to record results
		
		
		//uncomment the example you want to see (and comment-out the rest)
		
		//example.QLearningExample(outputPath);
		//example.SarsaLearningExample(outputPath);
		//example.BFSExample(outputPath);
		//example.DFSExample(outputPath);
		//example.AStarExample(outputPath);
		example.ValueIterationExample(outputPath);
		
		
		//run the visualizer
		example.visualize(outputPath);

	}
	
	
	public OptionsExample(){
		super();
		
		//override initial state goal location to be on a hallway where options can be most exploited
		GridWorldDomain.setLocation(initialState, 0, 5, 8);
	}
	
	
	
	
	
	////////////////////////////////////////////BEGIN BEAHVIOR EXAMPLES/////////////////////////////////////////////////////////
	
	public void QLearningExample(String outputPath){
		
		if(!outputPath.endsWith("/")){
			outputPath = outputPath + "/";
		}
		
		//creating the learning algorithm object; discount= 0.99; initialQ=-90.0; learning rate=0.9
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashingFactory, -90.0, 0.9);
		this.addRoomsOptionsToPlanner((OOMDPPlanner)agent);
		
		
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
		
		//creating the learning algorithm object; discount= 0.99; initialQ=-90.0; learning rate=0.5; lambda=1.0 (online Monte carlo at 1.0, one step at 0.0)
		LearningAgent agent = new SarsaLam(domain, rf, tf, 0.99, hashingFactory, -90.0, 0.5, 1.0);
		this.addRoomsOptionsToPlanner((OOMDPPlanner)agent);
		
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
		this.addRoomsOptionsToPlanner(planner);
		
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
		this.addRoomsOptionsToPlanner(planner);
		
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
		this.addRoomsOptionsToPlanner(planner);
		
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
		this.addRoomsOptionsToPlanner(planner);
		planner.planFromState(initialState);
		
		
		//create a Q-greedy policy from the planner
		Policy p = new GreedyQPolicy((QComputablePlanner)planner);
		
		//record the plan results to a file
		p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
		
	}
	
	
	public void addRoomsOptionsToPlanner(OOMDPPlanner planner){
		
		planner.addNonDomainReferencedAction(this.getRoomOption("blt", 0, 4, 0, 4, 1, 5));
		planner.addNonDomainReferencedAction(this.getRoomOption("blr", 0, 4, 0, 4, 5, 1));
		
		planner.addNonDomainReferencedAction(this.getRoomOption("tlr", 0, 4, 6, 10, 5, 8));
		planner.addNonDomainReferencedAction(this.getRoomOption("tlb", 0, 4, 6, 10, 1, 5));
		
		planner.addNonDomainReferencedAction(this.getRoomOption("trb", 6, 10, 5, 10, 8, 4));
		planner.addNonDomainReferencedAction(this.getRoomOption("trl", 6, 10, 5, 10, 5, 8));
		
		planner.addNonDomainReferencedAction(this.getRoomOption("brt", 6, 10, 0, 3, 8, 4));
		planner.addNonDomainReferencedAction(this.getRoomOption("brl", 6, 10, 6, 3, 5, 1));
		
		
	}
	
	
	public Option getRoomOption(String name, int leftBound, int rightBound, int bottomBound, int topBound, int hx, int hy){
		
		StateConditionTestIterable inRoom = new InRoomStateCheck(leftBound, rightBound, bottomBound, topBound);
		StateConditionTest atHallway = new AtPositionStateCheck(hx, hy);
		
		DiscreteMaskHashingFactory hashingFactory = new DiscreteMaskHashingFactory();
		hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList);
		
		//OOMDPPlanner planner = new BFS(domain, atHallway, hashingFactory);
		//PlannerDerivedPolicy p = new SDPlannerPolicy();
		//OOMDPPlanner planner = new ValueIteration(domain, new LocalSubgoalRF(inRoom, atHallway, 0., 0., 1.), new LocalSubgoalTF(inRoom, atHallway), 0.99, hashingFactory, 0.001, 50);
		OOMDPPlanner planner = new ValueIteration(domain, new LocalSubgoalRF(inRoom, atHallway), new LocalSubgoalTF(inRoom, atHallway), 0.99, hashingFactory, 0.001, 50);
		PlannerDerivedPolicy p = new GreedyDeterministicQPolicy();
		
		
		return new SubgoalOption(name, inRoom, atHallway, planner, p);
	}
	
	
	
	class InRoomStateCheck implements StateConditionTestIterable{

		int		leftBound;
		int		rightBound;
		int		bottomBound;
		int 	topBound;
		
		
		public InRoomStateCheck(int leftBound, int rightBound, int bottomBound, int topBound){
			this.leftBound = leftBound;
			this.rightBound = rightBound;
			this.bottomBound = bottomBound;
			this.topBound = topBound;
		}
		
		
		@Override
		public boolean satisfies(State s) {
			
			ObjectInstance agent = s.getObjectsOfTrueClass(GridWorldDomain.CLASSAGENT).get(0); //get the agent object
			
			int ax = agent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int ay = agent.getDiscValForAttribute(GridWorldDomain.ATTY);
			
			if(ax >= this.leftBound && ax <= this.rightBound && ay >= this.bottomBound && ay <= this.topBound){
				return true;
			}
			
			return false;
		}

		@Override
		public Iterator<State> iterator() {

			return new Iterator<State>() {

				int ax=leftBound;
				int ay=bottomBound;
				
				@Override
				public boolean hasNext() {
					
					if(ay <= topBound){
						return true;
					}
					
					return false;
				}

				@Override
				public State next() {
					
					State s = GridWorldDomain.getOneAgentNLocationState(domain, 0);
					GridWorldDomain.setAgent(s, ax, ay);
					
					ax++;
					if(ax > rightBound){
						ax = leftBound;
						ay++;
					}
					
					return s;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
			
		}

		@Override
		public void setStateContext(State s) {
			//do not need to do anything here
		}
		
		
		
		
	}
	
	
	class AtPositionStateCheck implements StateConditionTest{

		int x;
		int y;
			
		public AtPositionStateCheck(int x, int y){
			this.x = x;
			this.y = y;
		}
		
		@Override
		public boolean satisfies(State s) {

			ObjectInstance agent = s.getObjectsOfTrueClass(GridWorldDomain.CLASSAGENT).get(0); //get the agent object
			
			int ax = agent.getDiscValForAttribute(GridWorldDomain.ATTX);
			int ay = agent.getDiscValForAttribute(GridWorldDomain.ATTY);
			
			if(ax == this.x && ay == this.y){
				return true;
			}
			
			return false;
		}
			
	}

}
