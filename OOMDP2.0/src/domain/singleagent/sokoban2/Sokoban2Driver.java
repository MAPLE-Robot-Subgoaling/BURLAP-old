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
		
		State s = Sokoban2Domain.getClassicState(domain);
		
		Visualizer v = Sokoban2Visualizer.getVisualizer();
		VisualExplorer exp = new VisualExplorer(domain, v, s);
		
		exp.addKeyAction("w", Sokoban2Domain.ACTIONNORTH);
		exp.addKeyAction("s", Sokoban2Domain.ACTIONSOUTH);
		exp.addKeyAction("d", Sokoban2Domain.ACTIONEAST);
		exp.addKeyAction("a", Sokoban2Domain.ACTIONWEST);
		
		exp.initGUI();
	}

}
