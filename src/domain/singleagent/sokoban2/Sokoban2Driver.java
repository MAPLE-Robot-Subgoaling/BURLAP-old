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
	State s = Sokoban2Domain.getCleanState(domain, 5, 4, 3);

	// top left bottom right
	Sokoban2Domain.setRoom(s, 0, 10, 0, 0, 20, "red");
	Sokoban2Domain.setRoom(s, 1, 10, 20, 0, 40, "green");
	Sokoban2Domain.setRoom(s, 2, 20, 0, 10, 10, "blue");
	Sokoban2Domain.setRoom(s, 3, 20, 10, 10, 20, "magenta");
	Sokoban2Domain.setRoom(s, 4, 20, 20, 10, 40, "yellow");

	Sokoban2Domain.setDoor(s, 0, 10, 2, 10, 8);
	Sokoban2Domain.setDoor(s, 1, 10, 12, 10, 18);
	Sokoban2Domain.setDoor(s, 2, 10, 22, 10, 38);
	Sokoban2Domain.setDoor(s, 3, 8, 20, 2, 20);

	Sokoban2Domain.setAgent(s, 1, 1);
	Sokoban2Domain.setBlock(s, 0, 6, 5, "backpack", "red");
	Sokoban2Domain.setBlock(s, 1, 3, 4, "backpack", "blue");
	Sokoban2Domain.setBlock(s, 2, 2, 3, "backpack", "green");

	Visualizer v = Sokoban2Visualizer.getVisualizer("img");
	VisualExplorer exp = new VisualExplorer(domain, v, s);

	exp.addKeyAction("w", Sokoban2Domain.ACTIONNORTH);
	exp.addKeyAction("s", Sokoban2Domain.ACTIONSOUTH);
	exp.addKeyAction("d", Sokoban2Domain.ACTIONEAST);
	exp.addKeyAction("a", Sokoban2Domain.ACTIONWEST);

	exp.initGUI();
    }

}
