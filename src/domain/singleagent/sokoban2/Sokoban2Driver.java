package domain.singleagent.sokoban2;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;
import domain.singleagent.sokoban2.Sokoban2Domain;
import domain.singleagent.sokoban2.Sokoban2Visualizer;

public class Sokoban2Driver {
	
	public static final double LEARNINGRATE = 0.99;
	public static final double DISCOUNTFACTOR = 0.95;
	public static final double LAMBDA = 1.0;
	public static LearningAgent Q;
	public static OOMDPPlanner planner;
	public static EpisodeAnalysis analyzer;
	public static RewardFunction rf;
	public static TerminalFunction tf;

	public static void main(String[] args) {
		Sokoban2Domain dgen = new Sokoban2Domain();
		Domain domain = dgen.generateDomain();
		State s = Sokoban2Domain.getCleanState(domain, 3, 2, 3);

		Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
		Sokoban2Domain.setRoom(s, 1, 8, 0, 4, 4, "green");
		Sokoban2Domain.setRoom(s, 2, 8, 4, 4, 8, "blue");

		Sokoban2Domain.setDoor(s, 0, 4, 6, 4, 6);
		Sokoban2Domain.setDoor(s, 1, 4, 2, 4, 2);

		Sokoban2Domain.setAgent(s, 6, 6);
		Sokoban2Domain.setBlock(s, 0, 6, 5, "backpack", "red");
		Sokoban2Domain.setBlock(s, 1, 2, 2, "backpack", "blue");
		Sokoban2Domain.setBlock(s, 2, 2, 3, "backpack", "green");
		
		Visualizer v = Sokoban2Visualizer.getVisualizer();
		VisualExplorer exp = new VisualExplorer(domain, v, s);
		
		exp.addKeyAction("w", Sokoban2Domain.ACTIONNORTH);
		exp.addKeyAction("s", Sokoban2Domain.ACTIONSOUTH);
		exp.addKeyAction("d", Sokoban2Domain.ACTIONEAST);
		exp.addKeyAction("a", Sokoban2Domain.ACTIONWEST);
		
		exp.initGUI();
	}

}
