package burlap.michael;

import java.util.Iterator;


import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.LocalSubgoalRF;
import burlap.behavior.singleagent.options.LocalSubgoalTF;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.StateConditionTestIterable;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteMaskHashingFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class MyOptionsExample extends TestBehavior {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MyOptionsExample example = new MyOptionsExample();
		String outputPath = "output"; //directory to record results
		
		
		//uncomment the example you want to see (and comment-out the rest)
		
		example.QLearningExample(outputPath);
		//example.SarsaLearningExample(outputPath);
		//example.BFSExample(outputPath);
		//example.DFSExample(outputPath);
		//example.AStarExample(outputPath);
//		example.ValueIterationExample(outputPath);
		
		
		//run the visualizer
		example.visualize(outputPath);
	}
	
	
	public MyOptionsExample(){
		super();
		
		//override initial state goal location to be on a hallway where options can be most exploited
//		GridWorldDomain.setLocation(initialState, 0, 5, 8);
	}
	
	
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



	public void addRoomsOptionsToPlanner(OOMDPPlanner planner){
		
//		planner.addNonDomainReferencedAction(this.getRoomOption("blt", 0, 4, 0, 4, 1, 5));
//		planner.addNonDomainReferencedAction(this.getRoomOption("blr", 0, 4, 0, 4, 5, 1));
//		
//		planner.addNonDomainReferencedAction(this.getRoomOption("tlr", 0, 4, 6, 10, 5, 8));
//		planner.addNonDomainReferencedAction(this.getRoomOption("tlb", 0, 4, 6, 10, 1, 5));
//		
//		planner.addNonDomainReferencedAction(this.getRoomOption("trb", 6, 10, 5, 10, 8, 4));
//		planner.addNonDomainReferencedAction(this.getRoomOption("trl", 6, 10, 5, 10, 5, 8));
//		
//		planner.addNonDomainReferencedAction(this.getRoomOption("brt", 6, 10, 0, 3, 8, 4));
//		planner.addNonDomainReferencedAction(this.getRoomOption("brl", 6, 10, 6, 3, 5, 1));
		
		planner.addNonDomainReferencedAction(this.getRoomOption("blt", 0, 4, 0, 4, 3, 5));
		
		planner.addNonDomainReferencedAction(this.getRoomOption("tlr", 0, 4, 6, 11, 6, 8));
		planner.addNonDomainReferencedAction(this.getRoomOption("tlb", 0, 4, 6, 11, 3, 5));
		
		planner.addNonDomainReferencedAction(this.getRoomOption("trl", 6, 10, 0, 11, 6, 8));
		
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
