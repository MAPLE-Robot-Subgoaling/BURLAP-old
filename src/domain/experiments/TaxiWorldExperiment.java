package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
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
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import domain.taxiworld.TaxiWorldDomain;

public class TaxiWorldExperiment {
    public static Map<StateHashTuple, List<QValue>> runTaxiLearning(
	    PolicyBlocksPolicy policy, StateHashFactory hf, int[][] passPos,
	    int episodes, String filepath, double qInit, boolean log)
	    throws IOException {
	return runTaxiLearning(policy, hf, passPos, new ArrayList<Option>(),
		episodes, filepath, qInit, log);
    }

    public static Map<StateHashTuple, List<QValue>> runTaxiLearning(
	    PolicyBlocksPolicy policy, StateHashFactory hf, int[][] passPos,
	    Option o, int episodes, String filepath, double qInit, boolean log)
	    throws IOException {
	List<Option> os = new ArrayList<Option>();
	os.add(o);
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath,
		qInit, log);
    }

    public static Map<StateHashTuple, List<QValue>> runTaxiLearning(
	    PolicyBlocksPolicy policy, StateHashFactory hf, int[][] passPos,
	    List<? extends Option> os, int episodes, String filepath,
	    double qInit, boolean log) throws IOException {
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath,
		qInit, log, null);
    }

    public static Map<StateHashTuple, List<QValue>> runTaxiLearning(
	    PolicyBlocksPolicy policy, StateHashFactory hf, int[][] passPos,
	    List<? extends Option> os, int episodes, String filepath,
	    double qInit, boolean log,
	    List<Map<StateHashTuple, List<QValue>>> qInitPolicies)
	    throws IOException {
	long sTime = System.currentTimeMillis();
	if (log) {
	    System.out.println("Starting " + filepath);
	}
	System.out.println(passPos.length + " passengers.");
	TaxiWorldDomain.MAXPASS = passPos.length;
	IOQLearning Q;

	Q = new IOQLearning(TaxiWorldDomain.DOMAIN, new UniformCostRF(),
		TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf, qInit,
		TaxiWorldDomain.LEARNINGRATE, Integer.MAX_VALUE);
	if (qInitPolicies != null) {
	    Q.addSourcePolicies(qInitPolicies);
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

	}
	if (log) {
	    bS.close();
	    bR.close();
	    bO.close();
	    System.out.println(filepath + " finished in "
		    + (System.currentTimeMillis() - sTime) / 1000.0
		    + " seconds.");
	}

	return Q.getQPolicy();
    }

    public static Entry<List<PolicyBlocksPolicy>, List<Map<StateHashTuple, List<QValue>>>> driveBaseLearning(
	    StateHashFactory hf, int[][][] positions, int episodes,
	    double epsilon, String basepath) throws IOException {
	List<PolicyBlocksPolicy> toMerge = new ArrayList<PolicyBlocksPolicy>();
	List<Map<StateHashTuple, List<QValue>>> qMapping = new ArrayList<Map<StateHashTuple, List<QValue>>>();
	int c = 0;

	for (int[][] passengers : positions) {
	    c++;
	    long time = System.currentTimeMillis();
	    System.out.println(TaxiWorldDomain.MAXPASS);
	    System.out.println(passengers);
	    System.out.println(positions);
	    TaxiWorldDomain.MAXPASS = passengers.length;
	    new TaxiWorldDomain().generateDomain();
	    PolicyBlocksPolicy policy = new PolicyBlocksPolicy(epsilon);
	    System.out.println("Starting policy " + c + ": MAXPASS="
		    + TaxiWorldDomain.MAXPASS);

	    qMapping.add(runTaxiLearning(policy, hf, passengers, episodes,
		    basepath + c, 0.0, false));
	    System.out.println("Finished policy: " + c + " in "
		    + (System.currentTimeMillis() - time) / 1000.0
		    + " seconds.");
	    toMerge.add(policy);
	}

	// TODO
	// Don't need to return the entry, can just return the
	// List<PolicyBlocksPolicy> b/c PolicyBlocksPolicy.qplanner can be
	// casted to IOQLearning
	return new AbstractMap.SimpleEntry<List<PolicyBlocksPolicy>, List<Map<StateHashTuple, List<QValue>>>>(
		toMerge, qMapping);
    }

    public static void main(String args[]) throws IOException {
	String path = "/home/hanicho1/taxi/";
	// String path = "C:\\Users\\denizen\\Desktop\\Data\\";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path, 6);
	    path = oldPath;
	}
    }

    public static void driver(String path, int targetPassNum)
	    throws IOException {
	TaxiWorldDomain.MAXPASS = 5;
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
	int episodes = 25000;
	long startTime = System.currentTimeMillis();
	// Offset must always be one, or there will be value errors with
	// ATTCARRY
	// MAXPASS must never be set higher than max, ATTCARRY will have issues
	// as well
	// If MAXPASS must be set higher, the domain must be regenerated

	Random rand = new Random();
	int[][][] passes = new int[50][][];
	// [0..9] -> 1 passenger
	// [10..19] -> 2 passenger
	// ...
	// [40..49] -> 5 passenger
	for (int i = 0; i < 5; i++) {
	    for (int j = 0; j < 10; j++) {
		TaxiWorldDomain.MAXPASS = j;
		new TaxiWorldDomain().generateDomain();
		int[][] pass = TaxiWorldDomain.getRandomSpots(i + 1);
		passes[(i * 10) + j] = pass;
	    }
	}

	System.out.println("Target passenger number: " + targetPassNum);
	TaxiWorldDomain.MAXPASS = targetPassNum;
	new TaxiWorldDomain().generateDomain();
	int[][] targetPass = TaxiWorldDomain
		.getRandomSpots(TaxiWorldDomain.MAXPASS);

	PolicyBlocksPolicy tempP = new PolicyBlocksPolicy(epsilon);
	runTaxiLearning(tempP, hf, targetPass, episodes, path, 0.0, false);
	double qInit = getLowestQ(tempP);
	tempP = null;

	for (int i = 0; i < 7; i++) {
	    int[][][] passengers = new int[10][][];
	    for (int j = 0; j < 10; j++) {
		if (i < 4) {
		    // 0[1,5], 1[1,4], 2[1,3], 3[1,2]
		    int r = rand.nextInt(50 - (i * 10));
		    passengers[j] = passes[r];
		    System.out.println(r);
		    System.out.println(passes[r]);
		    System.out.println(passengers[j]);
		    System.out.println();
		} else {
		    // 4[2, 5], 5[3, 5], 6[4, 5]
		    int k = 10 + (i - 4) * 10;
		    int l = 49 - k;
		    int r = k + rand.nextInt(l);
		    System.out.println(r);
		    passengers[j] = passes[r];
		    System.out.println(passes[r]);
		    System.out.println(passengers[j]);
		    System.out.println();
		}
	    }

	    for (int[][] p : passengers) {
		for (int[] pp : p) {
		    System.out.println(pp[0] + ", " + pp[1]);
		}
		System.out.println();
	    }
	    Entry<List<PolicyBlocksPolicy>, List<Map<StateHashTuple, List<QValue>>>> toMerge = driveBaseLearning(
		    hf, passengers, episodes, epsilon, path);
	    List<Entry<AbstractedPolicy, Double>> merged = AbstractedPolicy
		    .powerMerge(hf, toMerge.getKey(), 3, 1);
	    List<Entry<AbstractedPolicy, Double>> naiveMerged = AbstractedPolicy
		    .naivePowerMerge(hf, toMerge.getKey(), 3, 1);

	    if (merged != null && merged.size() > 0) {
		System.out.println("Running PPB with option candidate of size "
			+ merged.get(0).getKey().size() + ".");
		PolicyBlocksPolicy ppbPolicy = new PolicyBlocksPolicy(epsilon);
		AbstractedOption ppbOption = new AbstractedOption(hf, merged
			.get(0).getKey().getPolicy(),
			TaxiWorldDomain.DOMAIN.getActions(), epsilon, "PPB");
		runTaxiLearning(ppbPolicy, hf, targetPass, ppbOption, episodes,
			path + "/" + i + "/" + "PPB", qInit, true);
	    } else {
		System.out.println("No PPB option candiates available.");
		PolicyBlocksPolicy ppbPolicy = new PolicyBlocksPolicy(epsilon);
		runTaxiLearning(ppbPolicy, hf, targetPass, episodes, path + "/"
			+ i + "/" + "PPB", qInit, true);
	    }
	    merged = null;

	    // Q-Value based policy transfer
	    System.out.println("Running Q-Value based policy transfer");
	    PolicyBlocksPolicy qptPolicy = new PolicyBlocksPolicy(epsilon);
	    runTaxiLearning(qptPolicy, hf, targetPass, new ArrayList<Option>(),
		    episodes, path + "/" + i + "/" + "Q-Value", qInit, true,
		    toMerge.getValue());

	    List<PolicyBlocksPolicy> sourcePs = new ArrayList<PolicyBlocksPolicy>();
	    sourcePs.addAll(toMerge.getKey());
	    toMerge = null;

	    List<String> objects = new ArrayList<String>(targetPassNum + 1);
	    objects.add("agent");
	    objects.add("passenger1");
	    objects.add("passenger2");
	    objects.add("passenger3");
	    objects.add("passenger4");
	    objects.add("passenger5");
	    objects.add("passenger6");
	    List<String> objPass = new ArrayList<String>(objects);
	    objPass.remove("agent");

	    List<List<String>> oisToRemove = AbstractedPolicy
		    .permutations(objPass);
	    Collections.shuffle(oisToRemove);
	    int c = 1;
	    for (List<String> oiToRemove : oisToRemove) {
		System.out.println("To remove: " + oiToRemove + "\n");
		List<AbstractedOption> topOptions = new ArrayList<AbstractedOption>(
			20);
		for (PolicyBlocksPolicy p : sourcePs) {
		    List<String> tempObjects = new ArrayList<String>(objects);
		    State sp = AbstractedPolicy.sampleState(p);
		    for (int b = 0; b < targetPassNum
			    - sp.getObjectsOfTrueClass("passenger").size(); b++) {
			tempObjects.remove(oiToRemove.get(b));
		    }

		    System.out.println(tempObjects);
		    topOptions.add(new AbstractedOption(hf, p.getPolicy(),
			    TaxiWorldDomain.DOMAIN.getActions(), termProb,
			    true, tempObjects, "TOPs"));
		}

		PolicyBlocksPolicy topTempP = new PolicyBlocksPolicy(epsilon);
		runTaxiLearning(topTempP, hf, targetPass, topOptions, episodes,
			path + "/" + i + "/" + "TOPs-" + c, qInit, true);

		if (naiveMerged != null && naiveMerged.size() > 0) {
		    // Random PolicyBlocks
		    System.out
			    .println("Running naive PolicyBlocks with option of size "
				    + naiveMerged.get(0).getKey().size());
		    List<String> pbObjects = new ArrayList<String>(objects);
		    State pbS = AbstractedPolicy.sampleState(naiveMerged.get(0)
			    .getKey());
		    for (int b = 0; b < targetPassNum
			    - pbS.getObjectsOfTrueClass("passenger").size(); b++) {
			pbObjects.remove(oiToRemove.get(b));
		    }
		    System.out.println(pbObjects);
		    AbstractedOption rpbOption = new AbstractedOption(hf,
			    naiveMerged.get(0).getKey().getPolicy(),
			    TaxiWorldDomain.DOMAIN.getActions(), termProb,
			    true, pbObjects, "RPB-" + c);
		    PolicyBlocksPolicy rpbTempP = new PolicyBlocksPolicy(
			    epsilon);
		    runTaxiLearning(rpbTempP, hf, targetPass, rpbOption,
			    episodes, path + "/" + i + "/" + "RPB-" + c, qInit,
			    true);
		} else {
		    PolicyBlocksPolicy rpbTempP = new PolicyBlocksPolicy(
			    epsilon);
		    runTaxiLearning(rpbTempP, hf, targetPass,
			    new ArrayList<Option>(), episodes, path + "/" + i
				    + "/" + "RPB-" + c, qInit, true);
		}

		if (c == 6) {
		    break;
		}
		c++;
	    }
	    naiveMerged = null;
	    objects = null;
	    oisToRemove = null;
	    objPass = null;

	    // Portable TOPs
	    List<AbstractedOption> ptopOptions = new ArrayList<AbstractedOption>(
		    20);
	    for (PolicyBlocksPolicy p : sourcePs) {
		ptopOptions
			.add(new AbstractedOption(hf, p.getPolicy(),
				TaxiWorldDomain.DOMAIN.getActions(), termProb,
				"PTOPs"));
	    }
	    PolicyBlocksPolicy ptopsPolicy = new PolicyBlocksPolicy(epsilon);
	    runTaxiLearning(ptopsPolicy, hf, targetPass, ptopOptions, episodes,
		    path + "/" + i + "/" + "PTOPs", qInit, true);
	    ptopsPolicy = null;
	    ptopOptions = null;
	    toMerge = null;

	    // Q-Learning
	    PolicyBlocksPolicy qlPolicy = new PolicyBlocksPolicy(epsilon);
	    runTaxiLearning(qlPolicy, hf, targetPass, episodes, path + "/" + i
		    + "/" + "Q-Learning", qInit, true);

	    // Perfect
	    AbstractedOption perOption = new AbstractedOption(hf,
		    qlPolicy.getPolicy(), TaxiWorldDomain.DOMAIN.getActions(),
		    termProb, "Perfect");
	    PolicyBlocksPolicy perPolicy = new PolicyBlocksPolicy(epsilon);
	    runTaxiLearning(perPolicy, hf, targetPass, perOption, episodes,
		    path + "/" + i + "/" + "Perfect", qInit, true);
	}

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
