package burlap.michael;

import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.statehashing.DiscreteStateHashFactory;

public class TestBehavior {
	
	GridWorldDomain				gwdg;
	Domain						domain;
	StateParser					sp;
	RewardFunction				rf;
	TerminalFunction			tf;
	StateConditionTest			goalCondition;
	State						initialState;
	DiscreteStateHashFactory	hashingFactory;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestBehavior tb = new TestBehavior();
		tb.QLearningExample("output");
		tb.visualize("output");
	}
	
	public TestBehavior(){
		//create the domain
		gwdg = new GridWorldDomain(12, 12);
		gwdg.makeEmptyMap();
		
//		gwdg.horizontalWall(0, 11, 0);
//		gwdg.horizontalWall(0, 11, 11);
//		gwdg.verticalWall(0, 11, 0);
//		gwdg.verticalWall(0, 11, 11);
		
		gwdg.horizontalWall(0, 2, 5);
		gwdg.horizontalWall(4, 5, 5);
		
		gwdg.verticalWall(0, 7, 6);
		gwdg.verticalWall(9, 11, 6);
		
		domain = gwdg.generateDomain();
		
		//create the state parser
		sp = new GridWorldStateParser(domain); 
		
		//define the task
		rf = new UniformCostRF(); 
		tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION)); 
		goalCondition = new TFGoalCondition(tf);
		
		//set up the initial state of the task
		initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 7, 0);
		
		//set up the state hashing system
		hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, 
					domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList); 
	}
	
	public void visualize(String outputPath){
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, gwdg.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
	}
	
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

}
