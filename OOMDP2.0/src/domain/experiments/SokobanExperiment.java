package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import domain.singleagent.sokoban2.Sokoban2Domain;
import domain.singleagent.sokoban2.Sokoban2RF;
import burlap.behavior.policyblocks.AbstractedOption;
import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;

public class SokobanExperiment {
    public static PolicyBlocksPolicy runSokobanBaseLearning(
	    StateHashFactory hf, int[][] blockPos, int episodes,
	    double epsilon, double goodReward, String path) {
	long time = System.currentTimeMillis();
	System.out.println("Starting base policy " + path + ".");
	PolicyBlocksPolicy p = new PolicyBlocksPolicy(epsilon);

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	State s = Sokoban2Domain.getCleanState(domain, 3, 2, blockPos.length);

	Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
	Sokoban2Domain.setRoom(s, 1, 8, 0, 4, 4, "green");
	Sokoban2Domain.setRoom(s, 2, 8, 4, 4, 8, "blue");
	Sokoban2Domain.setDoor(s, 0, 4, 6, 4, 6);
	Sokoban2Domain.setDoor(s, 1, 4, 2, 4, 2);
	Sokoban2Domain.setAgent(s, 6, 6);

	for (int i = 0; i < blockPos.length; i++) {
	    Sokoban2Domain.setBlock(s, i, blockPos[i][0], blockPos[i][1],
		    "backpack", Sokoban2Domain.COLORS[blockPos[i][2]]);
	}
	RewardFunction rf = new Sokoban2RF(goodReward);
	TerminalFunction tf = new SinglePFTF(
		domain.getPropFunction(Sokoban2Domain.PFATGOAL));
	QLearning Q = new QLearning(domain, rf, tf,
		Sokoban2Domain.DISCOUNTFACTOR, hf, epsilon,
		Sokoban2Domain.LEARNINGRATE, p, (int) goodReward);
	p.setPlanner((OOMDPPlanner) Q);

	for (int i = 1; i <= episodes; i++) {
	    Q.runLearningEpisodeFrom(s).numTimeSteps();
	}

	System.out.println("Finished base policy " + path + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");
	return p;
    }

    public static Long runSokobanTopLearning(StateHashFactory hf, Option o,
	    int[][] blockPos, int episodes, double epsilon, double goodReward,
	    String path) {
	long time = System.currentTimeMillis();
	System.out.println("Starting top policy " + path + ".");
	long cumul = 0;
	PolicyBlocksPolicy p = new PolicyBlocksPolicy(epsilon);

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	State s = Sokoban2Domain.getCleanState(domain, 3, 2, blockPos.length);

	Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
	Sokoban2Domain.setRoom(s, 1, 8, 0, 4, 4, "green");
	Sokoban2Domain.setRoom(s, 2, 8, 4, 4, 8, "blue");
	Sokoban2Domain.setDoor(s, 0, 4, 6, 4, 6);
	Sokoban2Domain.setDoor(s, 1, 4, 2, 4, 2);
	Sokoban2Domain.setAgent(s, 6, 6);

	for (int i = 0; i < blockPos.length; i++) {
	    Sokoban2Domain.setBlock(s, i, blockPos[i][0], blockPos[i][1],
		    "backpack", Sokoban2Domain.COLORS[blockPos[i][2]]);
	}
	RewardFunction rf = new Sokoban2RF(goodReward);
	TerminalFunction tf = new SinglePFTF(
		domain.getPropFunction(Sokoban2Domain.PFATGOAL));
	QLearning Q = new QLearning(domain, rf, tf,
		Sokoban2Domain.DISCOUNTFACTOR, hf, epsilon,
		Sokoban2Domain.LEARNINGRATE, p, (int) goodReward);
	p.setPlanner((OOMDPPlanner) Q);
	Q.addNonDomainReferencedAction(o);

	EpisodeAnalysis analyzer = new EpisodeAnalysis();
	for (int i = 1; i <= episodes; i++) {
	    analyzer = Q.runLearningEpisodeFrom(s);
	    cumul += analyzer.numTimeSteps();
	}

	System.out.println("Finished top policy " + path + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");
	return cumul;
    }

    public static PolicyBlocksPolicy runSokobanOptionLearning(
	    StateHashFactory hf, Option o, int[][] blockPos, int episodes,
	    double epsilon, double goodReward, String path) throws IOException {
	return runSokobanOptionLearning(hf, AbstractedPolicy.singletonList(o),
		blockPos, episodes, epsilon, goodReward, path);
    }

    public static PolicyBlocksPolicy runSokobanOptionLearning(
	    StateHashFactory hf, List<? extends Option> os, int[][] blockPos,
	    int episodes, double epsilon, double goodReward, String path)
	    throws IOException {
	long time = System.currentTimeMillis();
	System.out.println("Starting option policy " + path + ".");
	PolicyBlocksPolicy p = new PolicyBlocksPolicy(epsilon);
	long cumulR = 0;
	long cumulS = 0;

	File fR = new File(path + "-Reward.csv");
	File fS = new File(path + "-Steps.csv");
	BufferedWriter bR = new BufferedWriter(new FileWriter(fR));
	BufferedWriter bS = new BufferedWriter(new FileWriter(fS));
	bR.write("Episode,Reward\n");
	bS.write("Episode,Steps\n");

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	State s = Sokoban2Domain.getCleanState(domain, 3, 2, blockPos.length);

	Sokoban2Domain.setRoom(s, 0, 4, 0, 0, 8, "red");
	Sokoban2Domain.setRoom(s, 1, 8, 0, 4, 4, "green");
	Sokoban2Domain.setRoom(s, 2, 8, 4, 4, 8, "blue");
	Sokoban2Domain.setDoor(s, 0, 4, 6, 4, 6);
	Sokoban2Domain.setDoor(s, 1, 4, 2, 4, 2);
	Sokoban2Domain.setAgent(s, 6, 6);

	for (int i = 0; i < blockPos.length; i++) {
	    Sokoban2Domain.setBlock(s, i, blockPos[i][0], blockPos[i][1],
		    "backpack", Sokoban2Domain.COLORS[blockPos[i][2]]);
	}
	RewardFunction rf = new Sokoban2RF(goodReward);
	TerminalFunction tf = new SinglePFTF(
		domain.getPropFunction(Sokoban2Domain.PFATGOAL));
	QLearning Q = new QLearning(domain, rf, tf,
		Sokoban2Domain.DISCOUNTFACTOR, hf, epsilon,
		Sokoban2Domain.LEARNINGRATE, p, (int) goodReward);
	p.setPlanner((OOMDPPlanner) Q);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}

	EpisodeAnalysis analyzer = new EpisodeAnalysis();
	for (int i = 1; i <= episodes; i++) {
	    analyzer = Q.runLearningEpisodeFrom(s);
	    cumulR += ExperimentUtils.sum(analyzer.rewardSequence);
	    cumulS += analyzer.numTimeSteps();

	    bR.write(i + "," + cumulR + "\n");
	    bS.write(i + "," + cumulS + "\n");
	}

	bR.close();
	bS.close();

	System.out.println("Finished option policy " + path + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");
	return p;
    }

    public static void main(String[] args) throws IOException {
	String path = "/home/hanicho1/sokoban/";

	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path += i + "/";
	    driver(path);
	    path = oldPath;
	}
    }

    public static void driver(String path) throws IOException {
	int episodes = 5000;
	double epsilon = 0.5;
	double reward = 1000;

	Sokoban2Domain dgen = new Sokoban2Domain();
	Domain domain = dgen.generateDomain();
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(Sokoban2Domain.CLASSAGENT,
		domain.getObjectClass(Sokoban2Domain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(Sokoban2Domain.CLASSBLOCK,
		domain.getObjectClass(Sokoban2Domain.CLASSAGENT).attributeList);

	int[][][] blocks = new int[5][][];
	// Colors can only be in [0: blue, 1: green, 3: red]
	blocks[0] = new int[][] { { 2, 2, 0 } };
	blocks[1] = new int[][] { { 2, 3, 1 } };
	blocks[2] = new int[][] { { 3, 2, 1 }, { 6, 5, 1 } };
	blocks[3] = new int[][] { { 6, 5, 3 }, { 5, 3, 0 }, { 2, 3, 1 } };
	blocks[4] = new int[][] { { 2, 6, 3 } };

	int[][] target = new int[][] { { 6, 3, 1 }, { 2, 6, 3 } };

	List<PolicyBlocksPolicy> toMerge = new ArrayList<PolicyBlocksPolicy>();
	int j = 0;
	for (int[][] block : blocks) {
	    j++;
	    toMerge.add(runSokobanBaseLearning(hf, block, episodes, epsilon,
		    reward, "base-" + j));
	}

	for (PolicyBlocksPolicy merge : toMerge) {
	    removeRooms(merge);
	}

	long time = System.currentTimeMillis();
	int depth = 3;
	System.out.println("Starting P-MODAL merge with depth " + depth + ".");
	List<Entry<AbstractedPolicy, Double>> mergeds = AbstractedPolicy
		.powerMerge(hf, toMerge, 3, 1);
	System.out.println("Finished P-MODAL merge with depth " + depth
		+ " in " + (System.currentTimeMillis() - time) / 1000.0
		+ " seconds");

	AbstractedOption pmo = null;
	try {
	    Entry<AbstractedPolicy, Double> merged = mergeds.get(0);
	    System.out.println("Generated P-MODAL option of size "
		    + merged.getKey().size() + " and score "
		    + merged.getValue() + ".");
	    pmo = new AbstractedOption(hf, merged.getKey().getPolicy(),
		    domain.getActions(), 0.0, "P-MODAL");
	} catch (Exception e) {
	    System.out.println("P-MODAL merge was null.");
	}

	time = System.currentTimeMillis();
	System.out.println("Starting PolicyBlocks merge with depth " + depth
		+ ".");
	List<Entry<AbstractedPolicy, Double>> pbmergeds = AbstractedPolicy
		.naivePowerMerge(hf, toMerge, 3, 1);
	System.out.println("Finished PolicyBlocks merge with depth " + depth
		+ " in " + (System.currentTimeMillis() - time) / 1000.0
		+ " seconds");

	AbstractedOption pbo = null;
	try {
	    Entry<AbstractedPolicy, Double> pbmerged = pbmergeds.get(0);
	    System.out.println("Generated PolicyBlocks option of size "
		    + pbmerged.getKey().size() + " and score "
		    + pbmerged.getValue() + ".");
	    pbo = new AbstractedOption(hf, pbmerged.getKey().getPolicy(),
		    domain.getActions(), 0.0, "PolicyBlocks");
	} catch (Exception e) {
	    System.out.println("PolicyBlocks merge was null.");
	}
	PolicyBlocksPolicy qp = runSokobanBaseLearning(hf, target, episodes,
		epsilon, reward, "Q-Learning");
	AbstractedOption ro = generateRandomOption(hf, domain,
		domain.getActions(), qp.policy.keySet());
	AbstractedOption po = new AbstractedOption(hf, qp.policy,
		domain.getActions(), 0.0, "perfect");

	// Q-Learning
	runSokobanOptionLearning(hf, new ArrayList<Option>(), target, episodes,
		epsilon, reward, path + "Q-Learning");
	// P-MODAL
	runSokobanOptionLearning(hf,
		pmo == null ? new ArrayList<AbstractedOption>()
			: AbstractedPolicy.singletonList(pmo), target,
		episodes, epsilon, reward, path + "P-MODAL");
	// PolicyBlocks
	runSokobanOptionLearning(hf,
		pbo == null ? new ArrayList<AbstractedOption>()
			: AbstractedPolicy.singletonList(pbo), target,
		episodes, epsilon, reward, path + "PolicyBlocks");
	// Random
	runSokobanOptionLearning(hf, ro, target, episodes, epsilon, reward,
		path + "Random Option");
	// Perfect Policy
	runSokobanOptionLearning(hf, po, target, episodes, epsilon, reward,
		path + "Perfect Policy Option");
	// TOP
	List<Entry<AbstractedOption, Long>> tops = new ArrayList<Entry<AbstractedOption, Long>>(
		toMerge.size());
	int numTops = toMerge.size() / 4;
	int t = 1;
	for (PolicyBlocksPolicy merge : toMerge) {
	    AbstractedOption tempO = new AbstractedOption(hf, merge.policy,
		    domain.getActions(), 0.0, "top-" + t);
	    tops.add(new AbstractMap.SimpleEntry<AbstractedOption, Long>(tempO,
		    runSokobanTopLearning(hf, tempO, target, episodes, epsilon,
			    reward, "TOP-" + t)));
	    t++;
	}
	Collections.sort(tops, new Comparator<Entry<AbstractedOption, Long>>() {
	    @Override
	    public int compare(Entry<AbstractedOption, Long> arg0,
		    Entry<AbstractedOption, Long> arg1) {
		return -arg0.getValue().compareTo(arg1.getValue());
	    }
	});

	for (t = 1; t <= numTops; t++) {
	    runSokobanOptionLearning(hf, tops.get(t - 1).getKey(), target,
		    episodes, epsilon, reward, path + "Transfer Option-" + t);
	}
    }

    public static AbstractedOption generateRandomOption(StateHashFactory hf,
	    Domain d, List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
	Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
	Random rand = new Random();
	int count = 0;
	int max = rand.nextInt((stateSpace.size()));

	// Samples from the state space
	for (StateHashTuple s : stateSpace) {
	    if (count >= max) {
		break;
	    }
	    policy.put(
		    s,
		    new GroundedAction(actionSpace.get(rand.nextInt(actionSpace
			    .size())), ""));
	    count++;
	}

	System.out.println(count);
	System.out.println(stateSpace.size());

	return new AbstractedOption(hf, policy, d.getActions(), 0., "random");
    }

    public static void removeRooms(PolicyBlocksPolicy p) {
	for (StateHashTuple sh : p.policy.keySet()) {
	    for (ObjectInstance oi : sh.s.getAllObjects()) {
		if (oi.getName().equals(Sokoban2Domain.CLASSROOM)
			|| oi.getName().equals(Sokoban2Domain.CLASSDOOR)) {
		    sh.s.removeObject(oi);
		    sh.computeHashCode();
		}
	    }
	}
	for (StateHashTuple sh : p.qpolicy.keySet()) {
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
