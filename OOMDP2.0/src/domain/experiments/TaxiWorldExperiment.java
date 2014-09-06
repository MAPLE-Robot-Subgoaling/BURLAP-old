package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import domain.taxiworld.TaxiWorldDomain;
import burlap.behavior.policyblocks.AbstractedOption;
import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class TaxiWorldExperiment {
    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, int episodes,
	    String filepath, boolean log) throws IOException {
	return runTaxiLearning(policy, hf, passPos, new ArrayList<Option>(),
		episodes, filepath, log);
    }

    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, Option o, int episodes,
	    String filepath, boolean log) throws IOException {
	List<Option> os = new ArrayList<Option>();
	os.add(o);
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath, log);
    }

    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, List<? extends Option> os,
	    int episodes, String filepath, boolean log) throws IOException {
	TaxiWorldDomain.MAXPASS = passPos.length;
	QLearning Q = new QLearning(TaxiWorldDomain.DOMAIN, TaxiWorldDomain.rf,
		TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		TaxiWorldDomain.GAMMA, TaxiWorldDomain.LEARNINGRATE,
		Integer.MAX_VALUE);

	State s = TaxiWorldDomain.getCleanState();
	Q.setLearningPolicy(policy);
	policy.setPlanner(Q);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}

	BufferedWriter bS = null;
	BufferedWriter bR = null;
	if (log) {
	    File fS = new File(filepath + "-Steps.csv");
	    File fR = new File(filepath + "-Reward.csv");
	    bS = new BufferedWriter(new FileWriter(fS));
	    bR = new BufferedWriter(new FileWriter(fR));
	    bS.write("Episode,Steps\n");
	    bR.write("Episode,Reward\n");
	}
	long[] cumulArr = new long[episodes];
	long cumul = 0;
	long cumulR = 0;

	for (int i = 0; i < episodes; i++) {
	    TaxiWorldDomain.setAgent(s, 4, 5);
	    TaxiWorldDomain.setGoal(3, 5);

	    for (int j = 1; j <= TaxiWorldDomain.MAXPASS; j++) {
		TaxiWorldDomain.setPassenger(s, j, passPos[j - 1][0],
			passPos[j - 1][1]);
	    }

	    EpisodeAnalysis analyzer = new EpisodeAnalysis();
	    analyzer = Q.runLearningEpisodeFrom(s);
	    cumul += analyzer.numTimeSteps();
	    cumulR += ExperimentUtils.sum(analyzer.rewardSequence);
	    if (log) {
		bS.write((i + 1) + "," + cumul + "\n");
		bR.write((i + 1) + "," + cumulR + "\n");
	    }
	    cumulArr[i] = cumul;
	}
	if (log) {
	    bS.close();
	    bR.close();
	}

	return cumulArr;
    }

    /**
     * Generates a random options defined over a random chunk of the state space
     * 
     * @param hf
     * @param actionSpace
     * @param stateSpace
     * @return a AbstractedOption randomly initialized
     */
    public static AbstractedOption generateRandomOption(StateHashFactory hf,
	    List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
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
	return new AbstractedOption(hf, policy,
		TaxiWorldDomain.DOMAIN.getActions(), "random");
    }

    /**
     * This method generates an option to have the agent take a cyclic path in
     * the TaxiWorldDomain. Acts as a sanity check to make sure that options
     * wont cycle forever
     * 
     * @param hf
     * @param actionSpace
     * @param stateSpace
     * @return an option representing a cyclic path
     */
    public static AbstractedOption generateCyclicOption(StateHashFactory hf,
	    List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
	Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
	Action n = null;
	Action s = null;
	Action e = null;
	Action w = null;
	for (Action a : actionSpace) {
	    if (a.getName().equals(TaxiWorldDomain.ACTIONNORTH)) {
		n = a;
	    } else if (a.getName().equals(TaxiWorldDomain.ACTIONSOUTH)) {
		s = a;
	    } else if (a.getName().equals(TaxiWorldDomain.ACTIONEAST)) {
		e = a;
	    } else if (a.getName().equals(TaxiWorldDomain.ACTIONWEST)) {
		w = a;
	    }
	}

	if (n == null || s == null || e == null || w == null) {
	    throw new NullPointerException(
		    "Action space doesn't define the correct actions for this domain.");
	}
	// (1, 5) -> e; (2, 5) -> n; (2, 6) -> w; (1,6) -> s
	for (StateHashTuple st : stateSpace) {
	    int x = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT)
		    .getDiscValForAttribute(TaxiWorldDomain.ATTX);
	    int y = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT)
		    .getDiscValForAttribute(TaxiWorldDomain.ATTY);
	    if (x == 1 && y == 5) {
		policy.put(st, new GroundedAction(e, ""));
	    } else if (x == 2 && y == 5) {
		policy.put(st, new GroundedAction(n, ""));
	    } else if (x == 2 && y == 6) {
		policy.put(st, new GroundedAction(w, ""));
	    } else if (x == 1 && y == 6) {
		policy.put(st, new GroundedAction(s, ""));
	    }
	}

	return new AbstractedOption(hf, policy,
		TaxiWorldDomain.DOMAIN.getActions(), "cyclic");
    }

    public static List<PolicyBlocksPolicy> driveBaseLearning(
	    StateHashFactory hf, int[][][] positions, int episodes,
	    double epsilon, String basepath) throws IOException {
	List<PolicyBlocksPolicy> toMerge = new ArrayList<PolicyBlocksPolicy>();
	int c = 0;

	for (int[][] passengers : positions) {
	    c++;
	    long time = System.currentTimeMillis();
	    TaxiWorldDomain.MAXPASS = passengers.length;
	    new TaxiWorldDomain().generateDomain();
	    PolicyBlocksPolicy policy = new PolicyBlocksPolicy(epsilon);
	    System.out.println("Starting policy " + c + ": MAXPASS="
		    + TaxiWorldDomain.MAXPASS);
	    System.out.println(runTaxiLearning(policy, hf, passengers,
		    episodes, basepath + c, false)[episodes - 1]);
	    System.out.println("Finished policy: " + c + " in "
		    + (System.currentTimeMillis() - time) / 1000.0
		    + " seconds.");
	    toMerge.add(policy);
	}

	return toMerge;
    }

    public static void main(String args[]) throws IOException {
	String path = "/home/nick/burlap-stuff/taxi/1pass/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 1);
	    path = oldPath;
	}

	System.gc();
	path = "/home/nick/burlap-stuff/taxi/2pass/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 2);
	    path = oldPath;
	}

	System.gc();
	path = "/home/nick/burlap-stuff/taxi/3pass/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 3);
	    path = oldPath;
	}
	
	System.gc();
	path = "/home/nick/burlap-stuff/taxi/4pass/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 4);
	    path = oldPath;
	}
	
	System.gc();
	path = "/home/nick/burlap-stuff/taxi/5pass/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 5);
	    path = oldPath;
	}
    }

    public static void driver(String path, int targetPassNum)
	    throws IOException {
	TaxiWorldDomain.MAXPASS = 3;
	int max = TaxiWorldDomain.MAXPASS;
	new TaxiWorldDomain().generateDomain();
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(
		TaxiWorldDomain.CLASSAGENT,
		TaxiWorldDomain.DOMAIN
			.getObjectClass(TaxiWorldDomain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(
		TaxiWorldDomain.CLASSPASS,
		TaxiWorldDomain.DOMAIN
			.getObjectClass(TaxiWorldDomain.CLASSPASS).attributeList);
	double epsilon = 0.5;
	int episodes = 10000;
	long startTime = System.currentTimeMillis();
	// Offset must always be one, or there will be value errors with
	// ATTCARRY
	// MAXPASS must never be set higher than max, ATTCARRY will have issues
	// as well
	// If MAXPASS must be set higher, the domain must be regenerated

	int[][][] passengers = new int[10][][];
	for (int i = 0; i < 10; i++) {
	    int j = new Random().nextInt(max) + 1;
	    TaxiWorldDomain.MAXPASS = j;
	    new TaxiWorldDomain().generateDomain();
	    int[][] pass = TaxiWorldDomain
		    .getRandomSpots(TaxiWorldDomain.MAXPASS);
	    passengers[i] = pass;
	}

	List<PolicyBlocksPolicy> toMerge = driveBaseLearning(hf, passengers,
		episodes, epsilon, path);
	long uTime = System.currentTimeMillis();
	int depth = 3;
	System.out.println("Starting union merge with depth " + depth + ".");
	List<Entry<AbstractedPolicy, Double>> merged = AbstractedPolicy
		.powerMerge(hf, toMerge, depth, Integer.MAX_VALUE);
	System.out
		.println("Finished union merge; took "
			+ ((System.currentTimeMillis() - uTime) / 60000.0)
			+ " minutes");

	System.out.println(merged.size() + " options generated.");
	List<AbstractedOption> ops = new ArrayList<AbstractedOption>();
	int numOptions = 1;
	// TODO maybe add a heuristic for only letting in options that score
	// above a threshold (e.g. >= 0.3)
	for (int i = 0; i < numOptions; i++) {
	    System.out.println("Option number " + (i + 1) + " of size "
		    + merged.get(i).getKey().size() + " and score "
		    + merged.get(i).getValue() + " added.");
	    ops.add(new AbstractedOption(hf,
		    merged.get(i).getKey().getPolicy(), TaxiWorldDomain.DOMAIN
			    .getActions(), "" + i));
	}

	long lTime = System.currentTimeMillis();
	TaxiWorldDomain.MAXPASS = targetPassNum;
	new TaxiWorldDomain().generateDomain();
	int[][] targetPasses = new int[][] { { 9, 2 }, { 11, 10 }, { 6, 7 },
		{ 1, 6 }, { 10, 5 } };
	int[][] targetPass = new int[TaxiWorldDomain.MAXPASS][2];
	for (int i = 0; i < TaxiWorldDomain.MAXPASS; i++) {
	    targetPass[i][0] = targetPasses[i][0];
	    targetPass[i][1] = targetPasses[i][1];
	    System.out.print("(" + targetPass[i][0] + ", " + targetPass[i][1]
		    + ") ");
	}
	System.out.println();

	PolicyBlocksPolicy pmodalP = new PolicyBlocksPolicy(epsilon);
	String name = "P-MODAL";
	System.out.println("Starting policy " + name + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(pmodalP, hf, targetPass, ops,
		episodes, path + name, true)[episodes - 1]);
	System.out.println("Finished policy: " + name + " in "
		+ (System.currentTimeMillis() - lTime) / 1000.0 + " seconds.");
	lTime = System.currentTimeMillis();
	// TODO Experiments: Q-learning; Find what number of options is optimal;
	// Find what number of source policies is optimal; SARSA(\); Random
	// Option; Hand Crafted Option; Flat Policy Option

	PolicyBlocksPolicy qlearnP = new PolicyBlocksPolicy(epsilon);
	name = "Q-Learning";
	System.out.println("Starting policy " + name + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(qlearnP, hf, targetPass, episodes,
		path + name, true)[episodes - 1]);
	System.out.println("Finished policy: " + name + " in "
		+ (System.currentTimeMillis() - lTime) / 1000.0 + " seconds.");
	lTime = System.currentTimeMillis();

	PolicyBlocksPolicy randomP = new PolicyBlocksPolicy(epsilon);
	name = "Random Option";
	System.out.println("Starting policy " + name + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out
		.println(runTaxiLearning(
			randomP,
			hf,
			targetPass,
			generateRandomOption(hf,
				TaxiWorldDomain.DOMAIN.getActions(),
				qlearnP.policy.keySet()), episodes, path + name, true)[episodes - 1]);
	System.out.println("Finished policy: " + name + " in "
		+ (System.currentTimeMillis() - lTime) / 1000.0 + " seconds.");
	lTime = System.currentTimeMillis();

	AbstractedOption qO = new AbstractedOption(hf, qlearnP.policy,
		TaxiWorldDomain.DOMAIN.getActions(), "crafted");
	PolicyBlocksPolicy craftP = new PolicyBlocksPolicy(epsilon);
	name = "Handcrafted";
	System.out.println("Starting policy " + name + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(craftP, hf, targetPass, qO,
		episodes, path + name, true)[episodes - 1]);
	System.out.println("Finished policy: " + name + " in "
		+ (System.currentTimeMillis() - lTime) / 1000.0 + " seconds.");
	lTime = System.currentTimeMillis();

	System.out.println("Experiment finished. Took a total of "
		+ ((System.currentTimeMillis() - startTime) / 60000.0)
		+ " minutes.");
    }
}
