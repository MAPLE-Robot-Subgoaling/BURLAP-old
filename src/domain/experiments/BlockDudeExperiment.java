package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.GoalConditionTF;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import domain.blockdude.BlockDudeDomain;
import domain.blockdude.BlockDudeDomain.DomainData;

public class BlockDudeExperiment {
    public static PolicyBlocksPolicy runBlockDudeBaseLearning(
	    StateHashFactory hf, char[][] level, int episodes, double epsilon,
	    int maxsteps, double qInit, String name) {
	long time = System.currentTimeMillis();
	System.out.println("Learning base policy " + name + ".");
	final DomainData dd = BlockDudeDomain.createDomain(level);
	QLearning Q = new IOQLearning(dd.d, new UniformCostRF(),
		new GoalConditionTF(new StateConditionTest() {
		    private PropositionalFunction pf = dd.d
			    .getPropFunction(BlockDudeDomain.PFATEXIT);

		    @Override
		    public boolean satisfies(State s) {
			return pf.isTrue(s, new String[] { "agent0", "exit0" });
		    }
		}), 0.9995, hf, qInit, 0.999, maxsteps - 1);

	PolicyBlocksPolicy policy = new PolicyBlocksPolicy(Q, epsilon);
	Q.setLearningPolicy(policy);

	State start = dd.s.copy();
	for (int i = 0; i < episodes; i++) {
	    Q.runLearningEpisodeFrom(start);
	}

	System.out.println("Finished base policy " + name + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");

	return policy;
    }

    public static PolicyBlocksPolicy runBlockDudeOptionLearning(
	    StateHashFactory hf, Option o, char[][] level, int episodes,
	    double epsilon, int maxsteps, double qInit, String path)
	    throws IOException {
	return runBlockDudeOptionLearning(hf,
		AbstractedPolicy.singletonList(o), level, episodes, epsilon,
		maxsteps, qInit, path);
    }

    public static PolicyBlocksPolicy runBlockDudeOptionLearning(
	    StateHashFactory hf, List<? extends Option> os, char[][] level,
	    int episodes, double epsilon, int maxsteps, double qInit,
	    String path) throws IOException {
	final DomainData dd = BlockDudeDomain.createDomain(level);
	QLearning Q = new IOQLearning(dd.d, new UniformCostRF(),
		new GoalConditionTF(new StateConditionTest() {
		    private PropositionalFunction pf = dd.d
			    .getPropFunction(BlockDudeDomain.PFATEXIT);

		    @Override
		    public boolean satisfies(State s) {
			return pf.isTrue(s, new String[] { "agent0", "exit0" });
		    }
		}), 0.9995, hf, qInit, 0.999, maxsteps - 1);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}
	PolicyBlocksPolicy policy = new PolicyBlocksPolicy(Q, epsilon);
	Q.setLearningPolicy(policy);

	long cumulS = 0;
	long cumulR = 0;
	File fS = new File(path + "-Steps.csv");
	File fR = new File(path + "-Reward.csv");
	File fO = new File(path + "-Options.csv");
	BufferedWriter bS = new BufferedWriter(new FileWriter(fS));
	bS.write("Episode,Steps\n");
	BufferedWriter bR = new BufferedWriter(new FileWriter(fR));
	bR.write("Episode,Reward\n");
	BufferedWriter bO = new BufferedWriter(new FileWriter(fO));
	bO.write("Episode,Usage\n");

	State start = dd.s.copy();
	for (int i = 0; i < episodes; i++) {
	    double optiTaken = 0.0;
	    double primTaken = 0.0;
	    EpisodeAnalysis analyzer = new EpisodeAnalysis();
	    analyzer = Q.runLearningEpisodeFrom(start);

	    for (GroundedAction a : analyzer.actionSequence) {
		if (!a.action.isPrimitive()) {
		    optiTaken++;
		} else {
		    primTaken++;
		}
	    }
	    cumulS += analyzer.numTimeSteps();
	    cumulR += ExperimentUtils.sum(analyzer.rewardSequence);

	    bS.write((i + 1) + "," + cumulS + "\n");
	    bR.write((i + 1) + "," + cumulR + "\n");
	    bO.write((i + 1) + "," + (optiTaken / (primTaken + optiTaken))
		    + "\n");
	}

	bS.close();
	bR.close();
	bO.close();

	return policy;
    }

    public static void main(String[] args) throws IOException,
	    InterruptedException {
	String path = "/home/hanicho1/blockdude/ftm/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path);
	    path = oldPath;
	}
    }

    public static void driver(String path) throws IOException,
	    InterruptedException {
	double epsilon = 0.025;
	double termProb = 0.025;
	double qInit;
	int episodes = 15000;
	int stateCap = 1000;
	long startTime = System.currentTimeMillis();
	DomainData dd = BlockDudeDomain.createDomain(BlockDudeDomain
		.genLevel(8));
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(BlockDudeDomain.CLASSAGENT,
		dd.d.getObjectClass(BlockDudeDomain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(BlockDudeDomain.CLASSBLOCK,
		dd.d.getObjectClass(BlockDudeDomain.CLASSBLOCK).attributeList);
	hf.setAttributesForClass(BlockDudeDomain.CLASSEXIT,
		dd.d.getObjectClass(BlockDudeDomain.CLASSEXIT).attributeList);

	List<PolicyBlocksPolicy> toMerge = new ArrayList<PolicyBlocksPolicy>();
	// 2-3->4
	Random rand = new Random();

	for (int i = 0; i < 20; i++) {
	    char[][] lvl = BlockDudeDomain.genLevel(rand.nextInt(2) + 6);
	    for (int j = 0; j < lvl.length; j++) {
		for (int k = 0; k < lvl[j].length; k++) {
		    System.out.print(lvl[j][k] + " ");
		}
		System.out.println();
	    }

	    toMerge.add(runBlockDudeBaseLearning(hf, lvl, episodes, epsilon,
		    stateCap, -10.0, "" + (i+1)));
	}

	char[][] toLvl = BlockDudeDomain.genLevel(8);
	dd = BlockDudeDomain.createDomain(toLvl);
	/*
	 * (StateHashFactory hf, List<? extends Option> os, char[][] level, int
	 * episodes, double epsilon, int maxsteps, double qInit, String path)
	 */
	PolicyBlocksPolicy qInitP = runBlockDudeOptionLearning(hf,
		new ArrayList<Option>(), toLvl, episodes, epsilon, stateCap,
		-10.0, "qInit");
	qInit = getLowestQ(qInitP);
	qInitP = null;

	// Q-Learning
	PolicyBlocksPolicy qPolicy = runBlockDudeOptionLearning(hf,
		new ArrayList<Option>(), toLvl, episodes, epsilon, stateCap,
		qInit, path+"Q-Learning");
	// Perfect
	AbstractedOption perOption = new AbstractedOption(hf,
		qPolicy.getPolicy(), dd.d.getActions(), termProb, "perfect");
	runBlockDudeOptionLearning(hf, perOption, toLvl, episodes, epsilon,
		stateCap, qInit, path+"Perfect");
	perOption = null;

	// Merging
	List<Entry<AbstractedPolicy, Double>> merged = AbstractedPolicy
		.powerMerge(hf, toMerge, 3, 1);
	List<Entry<AbstractedPolicy, Double>> naiveMerged = AbstractedPolicy
		.naivePowerMerge(hf, toMerge, 3, 1);

	// PPB
	if (merged != null && merged.get(0).getKey().size() > 0) {
	    System.out.println("Generated PPB of size "
		    + merged.get(0).getKey().size());
	    AbstractedOption ppbOption = new AbstractedOption(hf, merged.get(0)
		    .getKey().getPolicy(), dd.d.getActions(), termProb, "PPB");
	    runBlockDudeOptionLearning(hf, ppbOption, toLvl, episodes, epsilon,
		    stateCap, qInit, path+"PPB");
	} else {
	    System.out.println("No option candidates generated.");
	    runBlockDudeOptionLearning(hf, new ArrayList<Option>(), toLvl,
		    episodes, epsilon, stateCap, qInit, path+"PPB");
	}
	merged = null;

	// PTOPs
	List<AbstractedOption> ptopOptions = new ArrayList<AbstractedOption>(
		toMerge.size());
	for (PolicyBlocksPolicy p : toMerge) {
	    ptopOptions.add(new AbstractedOption(hf, p.getPolicy(), dd.d
		    .getActions(), termProb, "TOP"));
	}
	runBlockDudeOptionLearning(hf, ptopOptions, toLvl, episodes, epsilon,
		stateCap, qInit, path+"PTOPs");

	List<String> objects = new ArrayList<String>(10);
	objects.add("block0");
	objects.add("block1");
	objects.add("block2");
	objects.add("block3");
	objects.add("block4");
	objects.add("block5");
	objects.add("agent0");
	List<String> blockObjs = new ArrayList<String>(objects);
	blockObjs.remove("agent0");

	List<List<String>> oisToRemove = AbstractedPolicy
		.permutations(blockObjs);
	Collections.shuffle(oisToRemove);
	int c = 1;
	for (List<String> oiToRemove : oisToRemove) {
	    System.out.println("To remove: " + oiToRemove + "\n");
	    List<AbstractedOption> topOptions = new ArrayList<AbstractedOption>(
		    toMerge.size());
	    for (PolicyBlocksPolicy p : toMerge) {
		List<String> tempObjects = new ArrayList<String>(objects);
		State sp = AbstractedPolicy.sampleState(p);
		for (int b = 0; b < 8 - sp.getObjectsOfTrueClass("block")
			.size(); b++) {
		    tempObjects.remove(oiToRemove.get(b));
		}
		System.out.println(tempObjects);
		topOptions.add(new AbstractedOption(hf, p.getPolicy(), dd.d
			.getActions(), termProb, true, tempObjects, "TOPs"));
	    }

	    runBlockDudeOptionLearning(hf, topOptions, toLvl, episodes,
		    epsilon, stateCap, qInit, path+"TOPs" + c);

	    if (naiveMerged != null && naiveMerged.size() > 0) {
		// Random PolicyBlocks
		List<String> pbObjects = new ArrayList<String>(objects);
		State pbS = AbstractedPolicy.sampleState(naiveMerged.get(0)
			.getKey());
		for (int b = 0; b < 8 - pbS.getObjectsOfTrueClass("block")
			.size(); b++) {
		    pbObjects.remove(oiToRemove.get(b));
		}
		System.out.println(pbObjects);
		AbstractedOption rpbOption = new AbstractedOption(hf,
			naiveMerged.get(0).getKey().getPolicy(),
			dd.d.getActions(), termProb, true, pbObjects, "RPB-"
				+ c);

		runBlockDudeOptionLearning(hf, rpbOption, toLvl, episodes,
			epsilon, stateCap, qInit, path+"RPB" + c);
	    } else {
		runBlockDudeOptionLearning(hf, new ArrayList<Option>(), toLvl,
			episodes, epsilon, stateCap, qInit, path+"RPB" + c);
	    }

	    if (c == 6) {
		break;
	    }
	    c++;
	}

	System.out.println("Experiment finished. Took "
		+ (System.currentTimeMillis() - startTime) / 1000.0
		+ " seconds.");
    }

    public static void removePlatforms(PolicyBlocksPolicy p) {
	for (StateHashTuple sh : p.getPolicy().keySet()) {
	    for (ObjectInstance oi : sh.s.getObjectsOfTrueClass("platform")) {
		sh.s.removeObject(oi.getName());
		sh.computeHashCode();
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
