package examples;

import domain.gridworld.GridWorldDomain;
import domain.gridworld.GridWorldStateParser;
import domain.gridworld.GridWorldVisualizer;
import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.behavior.EpisodeSequenceVisualizer;
import oomdptb.behavior.learning.LearningAgent;
import oomdptb.behavior.learning.tdmethods.QLearning;
import oomdptb.behavior.planning.statehashing.DiscreteStateHashFactory;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.StateParser;
import oomdptb.oomdp.TerminalFunction;
import oomdptb.oomdp.common.SinglePFTF;
import oomdptb.oomdp.common.UniformCostRF;
import oomdptb.oomdp.visualizer.Visualizer;

public class BasicBehavior {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		GridWorldDomain gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms(); //will use the standard four rooms layout
		Domain domain = gwdg.generateDomain();
		StateParser sp = new GridWorldStateParser(domain); //for writing states to a file
		
		RewardFunction rf = new UniformCostRF(); //reward always returns -1 (no positive reward on goal state either; but since the goal state ends action it will still be favored)
		TerminalFunction tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION)); //ends when the agent reaches a location
		
		//set up the initial state
		State initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
		GridWorldDomain.setAgent(initialState, 0, 0);
		GridWorldDomain.setLocation(initialState, 0, 10, 10);
		
		//set up the state hashing system
		DiscreteStateHashFactory hashingFactory = new DiscreteStateHashFactory();
		hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT, domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList); //optional code line; uses only the agent position to perform hash calculations instead of the agent and all locations
		
		//creating the learning algorithm object
		LearningAgent agent = new QLearning(domain, rf, tf, 0.99, hashingFactory, 0., 0.9);
		
		//run learning will printing out episodic performance and recording the episode to a file
		for(int i = 0; i < 100; i++){
			EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState);
			ea.writeToFile(String.format("output/e%03d", i), sp);
			System.out.println(i + ": " + ea.numTimeSteps());
		}
		
		//visualize the results
		Visualizer v = GridWorldVisualizer.getVisualizer(domain, gwdg.getMap());
		EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, "output");
		

	}

}
