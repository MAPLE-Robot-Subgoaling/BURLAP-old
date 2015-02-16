package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import burlap.behavior.policyblocks.AbstractedOption;
import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.IOQLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.values.UnsetValueException;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import domain.singleagent.sokoban2.Sokoban2Domain;

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

	    if (bx == 0 || bx == 1 || bx == 7 || bx == 8) {
		return true;
	    } else if (by == 0 || by == 1 || by == 8 || by == 9) {
		return true;
	    } else if (bx == 3 || bx == 4 || bx == 5) {
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

	String[] colors = new String[] { "blue", "magenta", "red" };

	for (int x = leftb; x <= rightb; x++) {
	    for (int y = bottomb; y <= topb; y++) {
		for (int c = 0; c < colors.length; c++) {
		    State sprime = s.copy();
		    Sokoban2Domain.setBlock(sprime, 0, x, y, "backpack",
			    colors[c]);

		    if (!badState(sprime)) {
			List<Entry<Integer, Integer>> temp = new ArrayList<Entry<Integer, Integer>>();
			if (!open.containsKey(colors[c])) {
			    temp.add(new AbstractMap.SimpleEntry<Integer, Integer>(
				    x, y));
			    open.put(colors[c], temp);
			} else {
			    temp = open.get(colors[c]);
			    temp.add(new AbstractMap.SimpleEntry<Integer, Integer>(
				    x, y));
			    open.put(colors[c], temp);
			}
		    }
		}
	    }
	}

	return open;
    }

    public static Map<Entry<Integer, Integer>, String> genBlocks(State s,
	    int numBlocks) {
	Random rand = new Random();
	Map<String, List<Entry<Integer, Integer>>> open = getOpenSpots(s);
	Map<Entry<Integer, Integer>, String> ret = new HashMap<Entry<Integer, Integer>, String>(
		numBlocks);

	// first red, second blue, third magenta
	for (int i = 0; i < numBlocks; i++) {
	    String color = "";
	    if (i == 0) {
		color = "red";
	    } else if (i == 1) {
		color = "blue";
	    } else if (i == 2) {
		color = "magenta";
	    } else {
		throw new IllegalArgumentException("Only 3 blocks allowed.");
	    }
	    
	    Entry<Integer, Integer> pos = open.get(color).get(
		    rand.nextInt(open.get(color).size()));

	    ret.put(new AbstractMap.SimpleEntry<Integer, Integer>(pos.getKey(),
		    pos.getValue()), color);
	}

	System.out.println(ret);
	return ret;
    }

    public static PolicyBlocksPolicy runSokobanOptionLearning(
	    StateHashFactory hf, Map<Entry<Integer, Integer>, String> blocks,
	    Option o, int episodes, double epsilon, int maxSteps, double qInit,
	    boolean record, String path) throws IOException {
	return runSokobanOptionLearning(hf, blocks,
		AbstractedPolicy.singletonList(o), episodes, epsilon, maxSteps,
		qInit, record, path);
    }

    public static PolicyBlocksPolicy runSokobanOptionLearning(
	    StateHashFactory hf, Map<Entry<Integer, Integer>, String> blocks,
	    List<? extends Option> os, int episodes, double epsilon,
	    int maxSteps, double qInit, boolean record, String path)
	    throws IOException {
	long time = System.currentTimeMillis();
	System.out.println("Starting option policy " + path + " with "
		+ blocks.size() + " blocks.");
	PolicyBlocksPolicy p = new PolicyBlocksPolicy(epsilon);

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	State s = Sokoban2Domain.getCleanState(domain, 3, 3, blocks.size());

	Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
	Sokoban2Domain.setRoom(s, 1, 9, 0, 4, 4, "blue");
	Sokoban2Domain.setRoom(s, 2, 9, 4, 4, 8, "magenta");

	Sokoban2Domain.setDoor(s, 0, 4, 1, 4, 3);
	Sokoban2Domain.setDoor(s, 1, 4, 5, 4, 7);
	Sokoban2Domain.setDoor(s, 2, 8, 4, 7, 4);
	Sokoban2Domain.setAgent(s, 1, 1);

	BufferedWriter bS = null;
	BufferedWriter bR = null;
	BufferedWriter bO = null;
	if (record) {
	    File fS = new File(path + "-Steps.csv");
	    File fR = new File(path + "-Reward.csv");
	    File fO = new File(path + "-Options.csv");
	    bS = new BufferedWriter(new FileWriter(fS));
	    bR = new BufferedWriter(new FileWriter(fR));
	    bO = new BufferedWriter(new FileWriter(fO));
	    bS.write("Episode,Steps\n");
	    bR.write("Episode,Reward\n");
	    bO.write("Episode,Usage\n");
	}
	long cumulS = 0;
	long cumulR = 0;

	int b = 0;
	for (Entry<Entry<Integer, Integer>, String> e : blocks.entrySet()) {
	    Sokoban2Domain.setBlock(s, b, e.getKey().getKey(), e.getKey()
		    .getValue(), "backpack", e.getValue());
	    b++;
	}

	RewardFunction rf = new UniformCostRF();
	TerminalFunction tf = new SinglePFTF(
		domain.getPropFunction(Sokoban2Domain.PFATGOAL));
	QLearning Q = new IOQLearning(domain, rf, tf,
		Sokoban2Domain.DISCOUNTFACTOR, hf, qInit,
		Sokoban2Domain.LEARNINGRATE, p, maxSteps);
	p.setPlanner(Q);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}

	for (int i = 1; i <= episodes; i++) {
	    double primTaken = 0.0;
	    double optiTaken = 0.0;
	    EpisodeAnalysis analyzer = Q.runLearningEpisodeFrom(s);
	    cumulS += analyzer.numTimeSteps();
	    cumulR += ExperimentUtils.sum(analyzer.rewardSequence);
	    for (GroundedAction a : analyzer.actionSequence) {
		if (domain.getActions().contains(a.action)) {
		    primTaken++;
		} else {
		    optiTaken++;
		}
	    }

	    if (record) {
		bS.write(i + "," + cumulS + "\n");
		bR.write(i + "," + cumulR + "\n");
		bO.write(i + "," + (optiTaken / (primTaken + optiTaken)) + "\n");
	    }
	}

	if (record) {
	    bS.close();
	    bR.close();
	    bO.close();
	}

	System.out.println("Finished option policy " + path + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");

	return p;
    }

    public static PolicyBlocksPolicy runSokobanBaseLearning(
	    StateHashFactory hf, int numBlocks, int episodes, double epsilon,
	    int maxSteps, double qInit, String path) {
	long time = System.currentTimeMillis();
	System.out.println("Starting base policy " + path + " with "
		+ numBlocks + " blocks.");
	PolicyBlocksPolicy p = new PolicyBlocksPolicy(epsilon);

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	State s = Sokoban2Domain.getCleanState(domain, 3, 3, numBlocks);

	Random rand = new Random();
	Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
	Sokoban2Domain.setRoom(s, 1, 9, 0, 4, 4, "blue");
	Sokoban2Domain.setRoom(s, 2, 9, 4, 4, 8, "magenta");

	Sokoban2Domain.setDoor(s, 0, 4, 1, 4, 3);
	Sokoban2Domain.setDoor(s, 1, 4, 5, 4, 7);
	Sokoban2Domain.setDoor(s, 2, 8, 4, 7, 4);
	Sokoban2Domain.setAgent(s, 1, 1);

	Map<String, List<Entry<Integer, Integer>>> open = getOpenSpots(s);

	for (int i = 0; i < numBlocks; i++) {
	    String color = "";
	    if (i == 0) {
		color = "red";
	    } else if (i == 1) {
		color = "blue";
	    } else if (i == 2) {
		color = "magenta";
	    } else {
		throw new IllegalArgumentException();
	    }

	    System.out.println(color);
	    Entry<Integer, Integer> pos = open.get(color).get(
		    rand.nextInt(open.get(color).size()));

	    Sokoban2Domain.setBlock(s, i, pos.getKey(), pos.getValue(),
		    "backpack", color);
	}

	RewardFunction rf = new UniformCostRF();
	TerminalFunction tf = new SinglePFTF(
		domain.getPropFunction(Sokoban2Domain.PFATGOAL));
	QLearning Q = new IOQLearning(domain, rf, tf,
		Sokoban2Domain.DISCOUNTFACTOR, hf, qInit,
		Sokoban2Domain.LEARNINGRATE, p, maxSteps);
	p.setPlanner(Q);

	for (int i = 1; i <= episodes; i++) {
	    Q.runLearningEpisodeFrom(s);
	}

	System.out.println("Finished base policy " + path + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");
	return p;
    }

    public static void main(String[] args) throws IOException {
	String path = "/home/hanicho1/sokoban/ftm2/";

	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path += i + "/";
	    driver(path);
	    path = oldPath;
	}
    }

    public static void driver(String path) throws IOException {
	int episodes = 10000;
	double epsilon = 0.025;
	double termProb = 0.025;
	int maxSteps = Integer.MAX_VALUE;
	double qInit;
	int sourcePolicies = 20;
	int targetNum = 3;

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();

	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(Sokoban2Domain.CLASSAGENT,
		domain.getObjectClass(Sokoban2Domain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(Sokoban2Domain.CLASSBLOCK,
		domain.getObjectClass(Sokoban2Domain.CLASSAGENT).attributeList);

	List<PolicyBlocksPolicy> toMerge = new ArrayList<PolicyBlocksPolicy>(
		sourcePolicies);

	Random rand = new Random();
	System.out.println("Running " + sourcePolicies + " base trials.");
	for (int i = 1; i <= sourcePolicies; i++) {
	    toMerge.add(runSokobanBaseLearning(hf, rand.nextInt(2) + 1,
		    episodes, epsilon, maxSteps, 0.0, path));
	}

	State s = Sokoban2Domain.getCleanState(domain, 3, 3, targetNum);
	Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
	Sokoban2Domain.setRoom(s, 1, 9, 0, 4, 4, "blue");
	Sokoban2Domain.setRoom(s, 2, 9, 4, 4, 8, "magenta");
	Sokoban2Domain.setDoor(s, 0, 4, 1, 4, 3);
	Sokoban2Domain.setDoor(s, 1, 4, 5, 4, 7);
	Sokoban2Domain.setDoor(s, 2, 8, 4, 7, 4);
	Sokoban2Domain.setAgent(s, 1, 1);

	Map<Entry<Integer, Integer>, String> targetBlocks = genBlocks(s,
		targetNum);

	while (targetBlocks.size() < targetNum) {
	    targetBlocks = genBlocks(s, targetNum);
	}

	System.out.println("Target blocks:");
	for (Entry<Entry<Integer, Integer>, String> e : targetBlocks.entrySet()) {
	    System.out.println(e.getValue() + " : [" + e.getKey().getKey()
		    + ", " + e.getKey().getValue() + "]");
	}

	PolicyBlocksPolicy initPolicy = runSokobanOptionLearning(hf,
		targetBlocks, new ArrayList<Option>(), episodes, epsilon,
		maxSteps, 0.0, false, path + "qInit");
	qInit = getLowestQ(initPolicy);
	System.out.println("qInit set to " + qInit);
	// Q-Learning
	// StateHashFactory hf, Map<Entry<Integer, Integer>, String> blocks,
	// List<Option> os,
	// int episodes, double epsilon, int maxSteps, double qInit, boolean
	// record, String path
	PolicyBlocksPolicy qPolicy = runSokobanOptionLearning(hf, targetBlocks,
		new ArrayList<Option>(), episodes, epsilon, maxSteps, qInit,
		true, path + "Q-Learning");

	// Perfect
	AbstractedOption qOption = new AbstractedOption(hf,
		qPolicy.getPolicy(), domain.getActions(), termProb, "Perfect");
	runSokobanOptionLearning(hf, targetBlocks, qOption, episodes, epsilon,
		maxSteps, qInit, true, path + "Perfect");
	qOption = null;
	qPolicy = null;

	System.gc();

	List<String> objects = new ArrayList<String>(10);
	objects.add("room0");
	objects.add("room1");
	objects.add("room2");
	objects.add("door0");
	objects.add("door1");
	objects.add("door2");
	objects.add("block0");
	objects.add("block1");
	objects.add("block2");
	objects.add("agent0");

	System.out.println("Starting P-MODAL power merge.");
	long pmT = System.currentTimeMillis();
	List<Entry<AbstractedPolicy, Double>> merged = AbstractedPolicy
		.powerMerge(hf, toMerge, 3, 1);
	System.out.println("Finished P-MODAL power merge in "
		+ (System.currentTimeMillis() - pmT) / 1000.0 + " seconds.");
	if (merged == null || merged.size() == 0) {
	    System.out.println("No options generated.");
	} else {
	    System.out.println("Top option of size "
		    + merged.get(0).getKey().size() + ".");
	}
	pmT = System.currentTimeMillis();

	System.out.println("Starting naive power merge.");
	List<Entry<AbstractedPolicy, Double>> naiveMerged = AbstractedPolicy
		.naivePowerMerge(hf, toMerge, 3, 1);
	System.out.println("Finished naive power merge in "
		+ (System.currentTimeMillis() - pmT) / 1000.0 + " seconds.");
	if (naiveMerged == null || naiveMerged.size() == 0) {
	    System.out.println("No options generated.");
	} else {
	    System.out.println("Top option of size "
		    + naiveMerged.get(0).getKey().size() + ".");
	}
	pmT = System.currentTimeMillis();

	// Random TOPs
	// 6 ways (3 perm 2)
	List<String> blocksT = new ArrayList<String>(3);
	blocksT.add("block0");
	blocksT.add("block1");
	blocksT.add("block2");
	List<List<String>> oisToRemove = AbstractedPolicy.permutations(blocksT);
	int c = 1;
	for (List<String> oiToRemove : oisToRemove) {
	    System.out.println("To remove: " + oiToRemove + "\n");
	    List<AbstractedOption> topOptions = new ArrayList<AbstractedOption>(
		    sourcePolicies);
	    for (PolicyBlocksPolicy p : toMerge) {
		List<String> tempObjects = new ArrayList<String>(objects);
		State sp = AbstractedPolicy.sampleState(p);
		for (int b = 0; b < targetNum
			- sp.getObjectsOfTrueClass("block").size(); b++) {
		    tempObjects.remove(oiToRemove.get(b));
		}
		System.out.println(tempObjects);
		topOptions.add(new AbstractedOption(hf, p.getPolicy(), domain
			.getActions(), termProb, true, tempObjects, "TOPs"));
	    }

	    runSokobanOptionLearning(hf, targetBlocks, topOptions, episodes,
		    epsilon, maxSteps, qInit, true, path + "TOPs-" + c);

	    if (naiveMerged != null && naiveMerged.size() > 0) {
		// Random PolicyBlocks
		List<String> pbObjects = new ArrayList<String>(objects);
		State pbS = AbstractedPolicy.sampleState(naiveMerged.get(0)
			.getKey());
		for (int b = 0; b < targetNum
			- pbS.getObjectsOfTrueClass("block").size(); b++) {
		    pbObjects.remove(oiToRemove.get(b));
		}
		System.out.println(pbObjects);
		AbstractedOption rpbOption = new AbstractedOption(hf,
			naiveMerged.get(0).getKey().getPolicy(),
			domain.getActions(), termProb, true, pbObjects, "RPB-"
				+ c);
		runSokobanOptionLearning(hf, targetBlocks, rpbOption, episodes,
			epsilon, maxSteps, qInit, true, path + "RPB-" + c);
	    } else {
		runSokobanOptionLearning(hf, targetBlocks,
			new ArrayList<Option>(), episodes, epsilon, maxSteps,
			qInit, true, path + "RPB-" + c);
	    }

	    c++;
	}
	naiveMerged = null;
	System.gc();

	// Portable TOPs
	List<AbstractedOption> ptopOptions = new ArrayList<AbstractedOption>(
		sourcePolicies);
	for (PolicyBlocksPolicy p : toMerge) {
	    ptopOptions.add(new AbstractedOption(hf, p.getPolicy(), domain
		    .getActions(), termProb, "PTOPs"));
	}
	runSokobanOptionLearning(hf, targetBlocks, ptopOptions, episodes,
		epsilon, maxSteps, qInit, true, path + "PTOPs");
	ptopOptions = null;
	toMerge = null;
	System.gc();

	// Portable PolicyBlocks
	if (merged != null && merged.size() > 0) {
	    AbstractedOption ppbOption = new AbstractedOption(hf, merged.get(0)
		    .getKey().getPolicy(), domain.getActions(), termProb, "PPB");
	    runSokobanOptionLearning(hf, targetBlocks, ppbOption, episodes,
		    epsilon, maxSteps, qInit, true, path + "PPB");
	} else {
	    runSokobanOptionLearning(hf, targetBlocks, new ArrayList<Option>(),
		    episodes, epsilon, maxSteps, qInit, true, path + "PPB");
	}

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

    public static double getLowestQ(PolicyBlocksPolicy p) {
	double minQ = Integer.MAX_VALUE;

	for (StateHashTuple sh : p.getPolicy().keySet()) {
	    for (QValue curQ : p.qplanner.getQs(sh.s)) {
		if (curQ.q < minQ) {
		    minQ = curQ.q;
		}
	    }
	}

	return minQ;
    }
}
