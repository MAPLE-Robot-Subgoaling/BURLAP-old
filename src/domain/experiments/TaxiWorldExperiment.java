package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import burlap.behavior.policyblocks.AbstractedOption;
import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.IOQLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import domain.taxiworld.TaxiWorldDomain;

public class TaxiWorldExperiment {
    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, int episodes,
	    String filepath, boolean log, boolean intraOption)
	    throws IOException {
	return runTaxiLearning(policy, hf, passPos, new ArrayList<Option>(),
		episodes, filepath, log, intraOption);
    }

    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, Option o, int episodes,
	    String filepath, boolean log, boolean intraOption)
	    throws IOException {
	List<Option> os = new ArrayList<Option>();
	os.add(o);
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath,
		log, intraOption);
    }

    public static long[] runTaxiLearning(PolicyBlocksPolicy policy,
	    StateHashFactory hf, int[][] passPos, List<? extends Option> os,
	    int episodes, String filepath, boolean log, boolean intraOption)
	    throws IOException {

	long sTime = System.currentTimeMillis();
	if (log) {
	    System.out.println("Starting " + filepath);
	}
	TaxiWorldDomain.MAXPASS = passPos.length;
	QLearning Q;
	if (intraOption) {
	    Q = new IOQLearning(TaxiWorldDomain.DOMAIN, new UniformCostRF(),
		    TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		    0.0, TaxiWorldDomain.LEARNINGRATE, Integer.MAX_VALUE);
	} else {
	    Q = new QLearning(TaxiWorldDomain.DOMAIN, new UniformCostRF(),
		    TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		    0.0, TaxiWorldDomain.LEARNINGRATE, Integer.MAX_VALUE);
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
	    // Q.setPsi(Q.getPsi() * 0.95);
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
		    episodes, basepath + c, false, false)[episodes - 1]);
	    System.out.println("Finished policy: " + c + " in "
		    + (System.currentTimeMillis() - time) / 1000.0
		    + " seconds.");
	    toMerge.add(policy);
	}

	return toMerge;
    }

    public static void main(String args[]) throws IOException {
	String path = "/home/hanicho1/io-exp/";
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
	TaxiWorldDomain.MAXPASS = 5;
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
	int episodes = 30000;
	long startTime = System.currentTimeMillis();
	// Offset must always be one, or there will be value errors with
	// ATTCARRY
	// MAXPASS must never be set higher than max, ATTCARRY will have issues
	// as well
	// If MAXPASS must be set higher, the domain must be regenerated

	int[][][] passengers = new int[20][][];
	for (int i = 0; i < 20; i++) {
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
	int depth = 3;

	// Entry<AbstractedPolicy, Double> absP = null;
	Entry<AbstractedPolicy, Double> absGP = null;

	PolicyBlocksPolicy qPBP = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy pqPBP = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy pPBP = new PolicyBlocksPolicy(epsilon);
	PolicyBlocksPolicy gpPBP = new PolicyBlocksPolicy(epsilon);

	// Merging

	/*
	 * System.out.println("Starting power merge with depth " + depth + ".");
	 * 
	 * List<Entry<AbstractedPolicy, Double>> absPs = AbstractedPolicy
	 * .powerMerge(hf, toMerge, depth, 1, true, false); if (absPs.size() !=
	 * 0) { absP = absPs.get(0); System.out.println("Size: " +
	 * absP.getKey().size()); System.out.println("Score: " +
	 * absP.getValue()); }
	 * 
	 * System.out.println("Finished power merge with time " +
	 * (System.currentTimeMillis() - uTime) / 1000.0 + " seconds.");
	 * 
	 * uTime = System.currentTimeMillis();
	 */
	System.out.println("Starting greedy power merge with depth " + depth
		+ ".");

	List<Entry<AbstractedPolicy, Double>> absGPs = AbstractedPolicy
		.powerMerge(hf, toMerge, depth, 1, true, true);
	if (absGPs.size() != 0) {
	    absGP = absGPs.get(0);
	    System.out.println("Size: " + absGP.getKey().size());
	    System.out.println("Score: " + absGP.getValue());
	}

	System.out.println("Finished greedy power merge with time "
		+ (System.currentTimeMillis() - uTime) / 1000.0 + " seconds.");

	// Learning

	TaxiWorldDomain.MAXPASS = targetPassNum;
	new TaxiWorldDomain().generateDomain();
	int[][] targPassengers = TaxiWorldDomain.getRandomSpots(targetPassNum);

	// Q-Learning
	runTaxiLearning(qPBP, hf, targPassengers, episodes,
		path + "Q-Learning", true, false);

	// Perfect Option
	AbstractedOption qO = new AbstractedOption(hf, qPBP.getPolicy(),
		TaxiWorldDomain.DOMAIN.getActions(), termProb, "Q-Learning");
	runTaxiLearning(pqPBP, hf, targPassengers, qO, episodes, path
		+ "Perfect", true, false);
	runTaxiLearning(pqPBP, hf, targPassengers, qO, episodes, path
		+ "IOPerfect", true, true);

	// Non-IO G-P-MODAL
	if (absGP != null) {
	    AbstractedOption pO = new AbstractedOption(hf, absGP.getKey()
		    .getPolicy(), TaxiWorldDomain.DOMAIN.getActions(),
		    termProb, "P-MODAL");
	    runTaxiLearning(pPBP, hf, targPassengers, pO, episodes, path
		    + "P-MODAL", true, false);
	} else {
	    System.out.println("P-MODAL has no available options!");
	    runTaxiLearning(pPBP, hf, targPassengers, episodes, path
		    + "P-MODAL", true, false);
	}

	// IO P-MODAL
	if (absGP != null) {
	    AbstractedOption gpO = new AbstractedOption(hf, absGP.getKey()
		    .getPolicy(), TaxiWorldDomain.DOMAIN.getActions(),
		    termProb, "IOP-MODAL");
	    runTaxiLearning(gpPBP, hf, targPassengers, gpO, episodes, path
		    + "IOP-MODAL", true, true);
	} else {
	    System.out.println("IO P-MODAL has no available options!");
	    runTaxiLearning(gpPBP, hf, targPassengers, episodes, path
		    + "IOP-MODAL", true, true);
	}

	System.out.println("Experiment finished! Took "
		+ (System.currentTimeMillis() - startTime) / 1000.0
		+ " seconds.");
    }
}
