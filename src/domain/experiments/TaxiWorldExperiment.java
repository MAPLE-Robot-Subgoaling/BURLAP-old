package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import domain.taxiworld.TaxiWorldDomain;

public class TaxiWorldExperiment {
    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, int episodes,
	    String filepath, boolean log, boolean intraOption, double qInit)
	    throws IOException {
	return runTaxiLearning(policy, hf, passPos, new ArrayList<Option>(),
		episodes, filepath, log, intraOption, qInit);
    }

    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, Option o, int episodes,
	    String filepath, boolean log, boolean intraOption, double qInit)
	    throws IOException {
	List<Option> os = new ArrayList<Option>();
	os.add(o);
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath,
		log, intraOption, qInit);
    }

    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, List<? extends Option> os,
	    int episodes, String filepath, boolean log, boolean intraOption,
	    double qInit) throws IOException {

	long sTime = System.currentTimeMillis();
	if (log) {
	    System.out.println("Starting " + filepath);
	}
	TaxiWorldDomain.MAXPASS = passPos.length;
	QLearning Q;

	// TODO analytically get the right q-init
	if (intraOption) {
	    Q = new IOQLearning(TaxiWorldDomain.DOMAIN, new UniformCostRF(),
		    TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		    qInit, TaxiWorldDomain.LEARNINGRATE, Integer.MAX_VALUE);
	} else {
	    Q = new QLearning(TaxiWorldDomain.DOMAIN, new UniformCostRF(),
		    TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		    qInit, TaxiWorldDomain.LEARNINGRATE, Integer.MAX_VALUE);
	}
	State s = TaxiWorldDomain.getCleanState();
	Q.setLearningPolicy(policy);
	policy.setPlanner(Q);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}

	BufferedWriter bS = null;
	BufferedWriter bR = null;
	BufferedWriter bO = null;
	if (log) {
	    File fS = new File(filepath + "-Steps.csv");
	    File fR = new File(filepath + "-Reward.csv");
	    File fO = new File(filepath + "-Options.csv");
	    bS = new BufferedWriter(new FileWriter(fS));
	    bR = new BufferedWriter(new FileWriter(fR));
	    bO = new BufferedWriter(new FileWriter(fO));
	    bS.write("Episode,Steps\n");
	    bR.write("Episode,Reward\n");
	    bO.write("Episode,Usage\n");
	}
	long[] cumulArr = new long[episodes];
	long cumul = 0;
	long cumulR = 0;

	for (int i = 0; i < episodes; i++) {
	    double primTaken = 0.;
	    double optiTaken = 0.;
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
	    for (GroundedAction a : analyzer.actionSequence) {
		if (!TaxiWorldDomain.DOMAIN.getActions().contains(a.action)) {
		    optiTaken++;
		} else {
		    primTaken++;
		}
	    }

	    if (log) {
		bS.write((i + 1) + "," + cumul + "\n");
		bR.write((i + 1) + "," + cumulR + "\n");
		bO.write((i + 1) + "," + (optiTaken / (primTaken + optiTaken))
			+ "\n");
	    }

	    cumulArr[i] = cumul;
	}
	if (log) {
	    bS.close();
	    bR.close();
	    bO.close();
	    System.out.println(filepath + " finished in "
		    + (System.currentTimeMillis() - sTime) / 1000.0
		    + " seconds.");
	}

	return cumulArr;
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
		    episodes, basepath + c, false, false, 0.0)[episodes - 1]);
	    System.out.println("Finished policy: " + c + " in "
		    + (System.currentTimeMillis() - time) / 1000.0
		    + " seconds.");
	    toMerge.add(policy);
	}

	return toMerge;
    }

    public static void main(String args[]) throws IOException {
	String path = "/home/hanicho1/depth-exp/";
	// String path = "C:\\Users\\denizen\\Desktop\\Data\\";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 7);
	    path = oldPath;
	}
    }

    public static void driver(String path, int targetPassNum)
	    throws IOException {
	TaxiWorldDomain.MAXPASS = 4;
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

	double termProb = 0.05;
	double epsilon = 0.05;
	int episodes = 100000;
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
		episodes, 0.1, path);
	long uTime = System.currentTimeMillis();
	int depth = 10;

	PolicyBlocksPolicy qPBP = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy pqPBP = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy pPBP3 = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy pPBP5 = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy pPBP10 = new PolicyBlocksPolicy(epsilon);

	// Merging
	System.out.println("Starting greedy power merge with depth " + depth
		+ ".");

	Map<Integer, List<Entry<AbstractedPolicy, Double>>> absGPs = AbstractedPolicy
		.powerMergeCache(hf, toMerge, depth, 1, true, true, true);
	System.out.println("Created " + absGPs.size() + " options.");
	toMerge = null;

	System.out.println("Finished greedy power merge with time "
		+ (System.currentTimeMillis() - uTime) / 1000.0 + " seconds.");

	TaxiWorldDomain.MAXPASS = targetPassNum;
	new TaxiWorldDomain().generateDomain();
	int[][] targPassengers = TaxiWorldDomain.getRandomSpots(targetPassNum);

	// Run an additional learning episode to get the proper qInit value
	PolicyBlocksPolicy qInitPolicy = new PolicyBlocksPolicy(epsilon);
	runTaxiLearning(qInitPolicy, hf, targPassengers, episodes, path
		+ "Q-Init-Policy", false, true, 0.0);
	double qInit = getLowestQ(qInitPolicy);
	qInitPolicy = null;
	System.out.println("Found Q-value initialization of " + qInit + ".");

	// Q-Learning
	runTaxiLearning(qPBP, hf, targPassengers, episodes,
		path + "Q-Learning", true, true, qInit);

	// Perfect Option
	AbstractedOption qT = new AbstractedOption(hf, qPBP.getPolicy(),
		TaxiWorldDomain.DOMAIN.getActions(), termProb, "Q-Learning");
	qPBP = null;

	runTaxiLearning(pqPBP, hf, targPassengers, qT, episodes, path
		+ "Perfect", true, true, qInit);
	pqPBP = null;
	qT = null;

	// P-MODAL
	AbstractedOption oPBP3;
	AbstractedOption oPBP5;
	AbstractedOption oPBP10;
	
	if (absGPs.get(3).size() != 0) {
	    oPBP3 = new AbstractedOption(hf, absGPs.get(3).get(0).getKey().getPolicy(), TaxiWorldDomain.DOMAIN.getActions(), termProb, path);
	    runTaxiLearning(pPBP3, hf, targPassengers, oPBP3, episodes, path + "P-MODAL-3", true, true, qInit);
	} else {
	    System.out.println("Depth of 3 has no available options.");
	    runTaxiLearning(pPBP3, hf, targPassengers, new ArrayList<AbstractedOption>(), episodes, path + "P-MODAL-3", true, true, qInit);
	}
	oPBP3 = null;
	pPBP3 = null;
	
	if (absGPs.get(5).size() != 0) {
	    oPBP5 = new AbstractedOption(hf, absGPs.get(5).get(0).getKey().getPolicy(), TaxiWorldDomain.DOMAIN.getActions(), termProb, path);
	    runTaxiLearning(pPBP5, hf, targPassengers, oPBP5, episodes, path + "P-MODAL-5", true, true, qInit);
	} else {
	    System.out.println("Depth of 5 has no available options.");
	    runTaxiLearning(pPBP5, hf, targPassengers, new ArrayList<AbstractedOption>(), episodes, path + "P-MODAL-5", true, true, qInit);
	}
	oPBP5 = null;
	pPBP5 = null;
	
	if (absGPs.get(10).size() != 0) {
	    oPBP10 = new AbstractedOption(hf, absGPs.get(10).get(0).getKey().getPolicy(), TaxiWorldDomain.DOMAIN.getActions(), termProb, path);
	    runTaxiLearning(pPBP10, hf, targPassengers, oPBP10, episodes, path + "P-MODAL-10", true, true, qInit);
	} else {
	    System.out.println("Depth of 10 has no available options.");
	    runTaxiLearning(pPBP10, hf, targPassengers, new ArrayList<AbstractedOption>(), episodes, path + "P-MODAL-10", true, true, qInit);
	}
	oPBP10 = null;
	pPBP10 = null;
	
	System.out.println("Experiment finished! Took "
		+ (System.currentTimeMillis() - startTime) / 1000.0
		+ " seconds.");
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
