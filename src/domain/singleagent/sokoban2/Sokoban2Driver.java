package domain.singleagent.sokoban2;

import domain.blockdude.BlockDudeVisualizer;
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
	Sokoban2Domain.setRoom(s, 0, 5, 0, 0, 10, "red");
	Sokoban2Domain.setRoom(s, 1, 5, 10, 0, 20, "green");
	Sokoban2Domain.setRoom(s, 2, 10, 0, 5, 5, "blue");
	Sokoban2Domain.setRoom(s, 3, 10, 5, 5, 10, "magenta");
	Sokoban2Domain.setRoom(s, 4, 10, 10, 5, 20, "yellow");

	Sokoban2Domain.setDoor(s, 0, 5, 2, 5, 3);
	Sokoban2Domain.setDoor(s, 1, 5, 7, 5, 8);
	Sokoban2Domain.setDoor(s, 2, 5, 12, 5, 18);
	Sokoban2Domain.setDoor(s, 3, 3, 10, 2, 10);
	Sokoban2Domain.setAgent(s, 1, 1);
	Sokoban2Domain.setBlock(s, 0, 6, 5, "backpack", "red");
	Sokoban2Domain.setBlock(s, 1, 3, 4, "backpack", "blue");
	Sokoban2Domain.setBlock(s, 2, 2, 3, "backpack", "green");

	Visualizer v = Sokoban2Visualizer.getVisualizer("img");
	VisualExplorer exp = new VisualExplorer(domain, v, s);
	exp.enableEpisodeRecording("'", "]");

	exp.addKeyAction("w", Sokoban2Domain.ACTIONNORTH);
	exp.addKeyAction("s", Sokoban2Domain.ACTIONSOUTH);
	exp.addKeyAction("d", Sokoban2Domain.ACTIONEAST);
	exp.addKeyAction("a", Sokoban2Domain.ACTIONWEST);

	exp.initGUI();
    }

}
