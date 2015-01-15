package domain.experiments;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.learning.tdmethods.IOQLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.values.UnsetValueException;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;
import domain.singleagent.sokoban2.Sokoban2Domain;
import domain.singleagent.sokoban2.Sokoban2RF;
import domain.singleagent.sokoban2.Sokoban2Visualizer;

public class SokobanExperiment {
    public static boolean badState(State sprime) {
	List<ObjectInstance> blocks = sprime
		.getObjectsOfTrueClass(Sokoban2Domain.CLASSBLOCK);
	// Note: this is extremely specific to the level described
	for (int i = 0; i < blocks.size(); i++) {
	    int bx;
	    int by;

	    try {
		bx = blocks.get(i).getDiscValForAttribute(Sokoban2Domain.ATTX);
		by = blocks.get(i).getDiscValForAttribute(Sokoban2Domain.ATTY);
	    } catch (UnsetValueException e) {
		break;
	    }
	    for (int j = 0; j < blocks.size(); j++) {
		if (blocks.get(j) == blocks.get(i)) {
		    continue;
		}
		try {
		    int jx = blocks.get(j).getDiscValForAttribute(
			    Sokoban2Domain.ATTX);
		    int jy = blocks.get(j).getDiscValForAttribute(
			    Sokoban2Domain.ATTY);

		    if (jx == bx || jy == by) {
			return true;
		    }
		} catch (UnsetValueException e) {
		}
	    }

	    ObjectInstance room = Sokoban2Domain.roomContainingPoint(sprime,
		    bx, by);
	    String bc = blocks.get(i).getStringValForAttribute(
		    Sokoban2Domain.ATTCOLOR);
	    String rc = room.getStringValForAttribute(Sokoban2Domain.ATTCOLOR);
	    if (bc.equals(rc)) {
		return true;
	    }

	    if (bx == 0 || bx == 1 || bx == 19 || bx == 20) {
		return true;
	    } else if (by == 0 || by == 1 || by == 9 || by == 10) {
		return true;
	    } else if (by == 4 || by == 5 || by == 6) {
		return true;
	    } else if (bx == 4 || bx == 5 || bx == 6) {
		return true;
	    } else if (bx == 9 || bx == 10 || bx == 11) {
		return true;
	    }

	    boolean hasColor = false;
	    for (ObjectInstance oi : sprime
		    .getObjectsOfTrueClass(Sokoban2Domain.CLASSROOM)) {
		if (oi.getStringValForAttribute(Sokoban2Domain.ATTCOLOR)
			.equals(bc)) {
		    hasColor = true;
		}
	    }
	    if (!hasColor) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Returns color -> list of open positions [x, y]
     * 
     * @param s
     * @return
     */
    public static Map<String, List<Entry<Integer, Integer>>> getOpenSpots(
	    State s) {
	Map<String, List<Entry<Integer, Integer>>> open = new HashMap<String, List<Entry<Integer, Integer>>>();
	Integer topb = null;
	Integer leftb = null;
	Integer rightb = null;
	Integer bottomb = null;

	// Defining the boundaries of the grid
	for (ObjectInstance oi : s
		.getObjectsOfTrueClass(Sokoban2Domain.CLASSROOM)) {
	    int topt, leftt, rightt, bottomt;

	    topt = oi.getDiscValForAttribute(Sokoban2Domain.ATTTOP);
	    leftt = oi.getDiscValForAttribute(Sokoban2Domain.ATTLEFT);
	    rightt = oi.getDiscValForAttribute(Sokoban2Domain.ATTRIGHT);
	    bottomt = oi.getDiscValForAttribute(Sokoban2Domain.ATTBOTTOM);

	    if (topb == null || topt > topb) {
		topb = topt;
	    }
	    if (leftb == null || leftt < leftb) {
		leftb = leftt;
	    }
	    if (rightb == null || rightt > rightb) {
		rightb = rightt;
	    }
	    if (bottomb == null || bottomt < bottomb) {
		bottomb = bottomt;
	    }
	}

	for (int x = leftb; x <= rightb; x++) {
	    for (int y = bottomb; y <= topb; y++) {
		for (int c = 0; c < Sokoban2Domain.COLORS.length; c++) {
		    State sprime = s.copy();
		    Sokoban2Domain.setBlock(sprime, 0, x, y, "backpack",
			    Sokoban2Domain.COLORS[c]);

		    if (!badState(sprime)) {
			List<Entry<Integer, Integer>> temp = new ArrayList<Entry<Integer, Integer>>();
			if (!open.containsKey(Sokoban2Domain.COLORS[c])) {
			    temp.add(new AbstractMap.SimpleEntry<Integer, Integer>(
				    x, y));
			    open.put(Sokoban2Domain.COLORS[c], temp);
			} else {
			    temp = open.get(Sokoban2Domain.COLORS[c]);
			    temp.add(new AbstractMap.SimpleEntry<Integer, Integer>(
				    x, y));
			    open.put(Sokoban2Domain.COLORS[c], temp);
			}
		    }
		}
	    }
	}

	return open;
    }

    public static PolicyBlocksPolicy runSokobanBaseLearning(
	    StateHashFactory hf, int numBlocks, int episodes, double epsilon,
	    double goodReward, String path) {
	long time = System.currentTimeMillis();
	System.out.println("Starting base policy " + path + ".");
	PolicyBlocksPolicy p = new PolicyBlocksPolicy(epsilon);

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	State s = Sokoban2Domain.getCleanState(domain, 5, 4, numBlocks);

	Random rand = new Random();
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

	Map<String, List<Entry<Integer, Integer>>> open = getOpenSpots(s);

	for (int i = 0; i < numBlocks; i++) {
	    List<String> colors = new ArrayList<String>(open.keySet());
	    String color = colors.get(rand.nextInt(colors.size()));

	    Entry<Integer, Integer> pos = open.get(color).get(
		    rand.nextInt(open.get(color).size()));

	    Sokoban2Domain.setBlock(s, i, pos.getKey(), pos.getValue(),
		    "backpack", color);
	}

	RewardFunction rf = new Sokoban2RF(goodReward);
	TerminalFunction tf = new SinglePFTF(
		domain.getPropFunction(Sokoban2Domain.PFATGOAL));
	QLearning Q = new IOQLearning(domain, rf, tf,
		Sokoban2Domain.DISCOUNTFACTOR, hf, -1.0,
		Sokoban2Domain.LEARNINGRATE, p, Integer.MAX_VALUE);
	p.setPlanner(Q);

	Visualizer v = Sokoban2Visualizer.getVisualizer("img");
	VisualExplorer exp = new VisualExplorer(domain, v, s);
	exp.initGUI();
	for (int i = 1; i <= episodes; i++) {
	    Q.runLearningEpisodeFrom(s).numTimeSteps();
	}

	System.out.println("Finished base policy " + path + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");
	return p;
    }

    public static void main(String[] args) throws IOException {
	String path = "/home/hanicho1/sokoban/";

	for (int i = 1; i <= 1; i++) {
	    String oldPath = path;
	    path += i + "/";
	    driver(path);
	    path = oldPath;
	}
    }

    public static void driver(String path) throws IOException {
	int episodes = 1000;
	double epsilon = 0.01;
	double reward = 1;

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(Sokoban2Domain.CLASSAGENT,
		domain.getObjectClass(Sokoban2Domain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(Sokoban2Domain.CLASSBLOCK,
		domain.getObjectClass(Sokoban2Domain.CLASSAGENT).attributeList);

	runSokobanBaseLearning(hf, 2, episodes, epsilon, reward, path);
    }

    public static void removeRooms(PolicyBlocksPolicy p) {
	for (StateHashTuple sh : p.getPolicy().keySet()) {
	    for (ObjectInstance oi : sh.s.getAllObjects()) {
		if (oi.getName().equals(Sokoban2Domain.CLASSROOM)
			|| oi.getName().equals(Sokoban2Domain.CLASSDOOR)) {
		    sh.s.removeObject(oi);
		    sh.computeHashCode();
		}
	    }
	}
    }
}
