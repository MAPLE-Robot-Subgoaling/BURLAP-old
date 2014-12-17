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

import burlap.behavior.policyblocks.AbstractedOption;
import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.GoalConditionTF;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;
import domain.blockdude.BlockDudeDomain;
import domain.blockdude.BlockDudeDomain.DomainData;

public class BlockDudeExperiment {
    public static long runBlockDudeTopLearning(StateHashFactory hf, Option o,
	    char[][] level, int episodes, double epsilon, int maxsteps,
	    String name) {
	long cumul = 0;
	long time = System.currentTimeMillis();
	System.out.println("Learning base policy " + name + ".");
	final DomainData dd = BlockDudeDomain.createDomain(level);
	QLearning Q = new QLearning(dd.d, new UniformCostRF(),
		new GoalConditionTF(new StateConditionTest() {
		    private PropositionalFunction pf = dd.d
			    .getPropFunction(BlockDudeDomain.PFATEXIT);

		    @Override
		    public boolean satisfies(State s) {
			return pf.isTrue(s, new String[] { "agent0", "exit0" });
		    }
		}), 0.95, hf, 0.2, 0.99, maxsteps - 1);

	PolicyBlocksPolicy policy = new PolicyBlocksPolicy(Q, epsilon);
	Q.setLearningPolicy(policy);
	Q.addNonDomainReferencedAction(o);
	EpisodeAnalysis analyzer = new EpisodeAnalysis();
	for (int i = 0; i < episodes; i++) {
	    analyzer = Q.runLearningEpisodeFrom(dd.s);
	    cumul += analyzer.numTimeSteps();
	}

	System.out.println("Finished base policy " + name + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");

	return cumul;
    }

    public static PolicyBlocksPolicy runBlockDudeBaseLearning(
	    StateHashFactory hf, char[][] level, int episodes, double epsilon,
	    int maxsteps, String name) {
	long time = System.currentTimeMillis();
	System.out.println("Learning base policy " + name + ".");
	final DomainData dd = BlockDudeDomain.createDomain(level);
	QLearning Q = new QLearning(dd.d, new UniformCostRF(),
		new GoalConditionTF(new StateConditionTest() {
		    private PropositionalFunction pf = dd.d
			    .getPropFunction(BlockDudeDomain.PFATEXIT);

		    @Override
		    public boolean satisfies(State s) {
			return pf.isTrue(s, new String[] { "agent0", "exit0" });
		    }
		}), 0.95, hf, 0.2, 0.99, maxsteps - 1);

	PolicyBlocksPolicy policy = new PolicyBlocksPolicy(Q, epsilon);
	Q.setLearningPolicy(policy);

	for (int i = 0; i < episodes; i++) {
	    Q.runLearningEpisodeFrom(dd.s);
	}

	System.out.println("Finished base policy " + name + " in "
		+ (System.currentTimeMillis() - time) / 1000.0 + " seconds.");

	return policy;
    }

    public static PolicyBlocksPolicy runBlockDudeOptionLearning(
	    StateHashFactory hf, Option o, char[][] level, int episodes,
	    double epsilon, int maxsteps, String path) throws IOException {
	return runBlockDudeOptionLearning(hf,
		AbstractedPolicy.singletonList(o), level, episodes, epsilon,
		maxsteps, path);
    }

    public static PolicyBlocksPolicy runBlockDudeOptionLearning(
	    StateHashFactory hf, List<? extends Option> os, char[][] level,
	    int episodes, double epsilon, int maxsteps, String path)
	    throws IOException {
	final DomainData dd = BlockDudeDomain.createDomain(level);
	QLearning Q = new QLearning(dd.d, new UniformCostRF(),
		new GoalConditionTF(new StateConditionTest() {
		    private PropositionalFunction pf = dd.d
			    .getPropFunction(BlockDudeDomain.PFATEXIT);

		    @Override
		    public boolean satisfies(State s) {
			return pf.isTrue(s, new String[] { "agent0", "exit0" });
		    }
		}), 0.95, hf, 0.2, 0.99, maxsteps - 1);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}
	PolicyBlocksPolicy policy = new PolicyBlocksPolicy(Q, epsilon);
	Q.setLearningPolicy(policy);

	long cumulS = 0;
	long cumulR = 0;
	File fS = new File(path + "-Steps.csv");
	File fR = new File(path + "-Reward.csv");
	BufferedWriter bS = new BufferedWriter(new FileWriter(fS));
	bS.write("Episode,Steps\n");
	BufferedWriter bR = new BufferedWriter(new FileWriter(fR));
	bR.write("Episode,Reward\n");

	for (int i = 0; i < episodes; i++) {
	    EpisodeAnalysis analyzer = new EpisodeAnalysis();
	    analyzer = Q.runLearningEpisodeFrom(dd.s);

	    if (i % 500 == 0) {
		System.out.println("Episode " + i + ": "
			+ analyzer.numTimeSteps());
	    }
	    cumulS += analyzer.numTimeSteps();
	    cumulR += ExperimentUtils.sum(analyzer.rewardSequence);

	    bS.write((i + 1) + "," + cumulS + "\n");
	    bR.write((i + 1) + "," + cumulR + "\n");

	    // analyzer.writeToFile(String.format("output/e%03d", i), sp);
	}

	bS.close();
	bR.close();

	return policy;
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

    public static AbstractedOption craftOption(int episodes, double epsilon,
	    int stateCap) {
	char[][] lvl = { { ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', 'b', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', '<' },
		{ 'g', ' ', ' ', ' ', 't', 't', 't' },
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ' } };

	DomainData dd = BlockDudeDomain.createDomain(lvl);
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(BlockDudeDomain.CLASSAGENT,
		dd.d.getObjectClass(BlockDudeDomain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(BlockDudeDomain.CLASSBLOCK,
		dd.d.getObjectClass(BlockDudeDomain.CLASSBLOCK).attributeList);
	hf.setAttributesForClass(BlockDudeDomain.CLASSEXIT,
		dd.d.getObjectClass(BlockDudeDomain.CLASSEXIT).attributeList);

	PolicyBlocksPolicy p = runBlockDudeBaseLearning(hf, lvl, episodes,
		epsilon, stateCap, "craft");

	return new AbstractedOption(hf, p.policy, dd.d.getActions(), 0.,
		"Crafted");
    }

    public static void main(String[] args) throws IOException {
	String path = "/home/hanicho1/blockdude/";
	for (int i = 1; i <= 20; i++) {
	    String oldPath = path;
	    path = path + i + "/";
	    driver(path);
	    path = oldPath;
	}
    }

    public static void driver(String path) throws IOException {
	char[][] lvla = { { 't', 'g', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 'b', ' ', ' ', ' ', ' ', '<', ' ' },
		{ ' ', ' ', 'b', 'b', ' ', ' ', ' ', 't', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', 't', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ' } };

	char[][] lvlb = { { 't', 'g', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', ' ' },
		{ ' ', ' ', 'b', 'b', ' ', ' ', ' ', 't', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', 't', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ' } };

	char[][] lvlc = { { 't', 'g', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', 'b', ' ', ' ', ' ', '<', ' ' },
		{ ' ', ' ', ' ', 'b', 'b', ' ', ' ', 't', ' ' },
		{ ' ', ' ', 't', 't', 't', 'b', 't', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ' } };

	char[][] lvld = { { 't', 'g', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', '<', ' ' },
		{ ' ', ' ', ' ', 'b', ' ', 'b', 'b', 't', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', 't', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ' } };

	char[][] lvle = { { 't', 'g', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', ' ' },
		{ ' ', ' ', 'b', 'b', ' ', ' ', 'b', 't', ' ' },
		{ ' ', ' ', 't', 't', 't', 'b', 't', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ' } };

	char[][] lvlt = { { 't', 'g', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 'b', ' ', ' ', ' ', 'b', '<', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', 't', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ' } };

	double epsilon = 0.5;
	int episodes = 5000;
	int stateCap = 50;
	long startTime = System.currentTimeMillis();
	DomainData dd = BlockDudeDomain.createDomain(lvla);
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(BlockDudeDomain.CLASSAGENT,
		dd.d.getObjectClass(BlockDudeDomain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(BlockDudeDomain.CLASSBLOCK,
		dd.d.getObjectClass(BlockDudeDomain.CLASSBLOCK).attributeList);
	hf.setAttributesForClass(BlockDudeDomain.CLASSEXIT,
		dd.d.getObjectClass(BlockDudeDomain.CLASSEXIT).attributeList);

	List<PolicyBlocksPolicy> toMerge = new ArrayList<PolicyBlocksPolicy>();

	toMerge.add(runBlockDudeBaseLearning(hf, lvla, episodes, epsilon,
		stateCap, "A"));
	toMerge.add(runBlockDudeBaseLearning(hf, lvlb, episodes, epsilon,
		stateCap, "B"));
	toMerge.add(runBlockDudeBaseLearning(hf, lvlc, episodes, epsilon,
		stateCap, "C"));
	toMerge.add(runBlockDudeBaseLearning(hf, lvld, episodes, epsilon,
		stateCap, "D"));
	toMerge.add(runBlockDudeBaseLearning(hf, lvle, episodes, epsilon,
		stateCap, "E"));

	for (PolicyBlocksPolicy merge : toMerge) {
	    removePlatforms(merge);
	}

	long mTime = System.currentTimeMillis();
	System.out.println("Starting merging.");
	List<Entry<AbstractedPolicy, Double>> merged = AbstractedPolicy
		.powerMerge(hf, toMerge, 3, 1);
	System.out.println(merged.size());
	System.out.println(merged.get(0).getKey().size() + ": "
		+ merged.get(0).getValue());
	AbstractedOption o1 = new AbstractedOption(hf, merged.get(0).getKey()
		.getPolicy(), dd.d.getActions(), 0., "one");
	System.out.println("Merging complete. Took "
		+ (System.currentTimeMillis() - mTime) / 1000.0 + " seconds.");

	PolicyBlocksPolicy qPolicy = runBlockDudeOptionLearning(hf,
		new ArrayList<Option>(), lvlt, episodes, epsilon, stateCap,
		path + "Q-Learning");

	runBlockDudeOptionLearning(hf, o1, lvlt, episodes, epsilon, stateCap,
		path + "P-MODAL");

	AbstractedOption oR = generateRandomOption(hf, dd.d, dd.d.getActions(),
		qPolicy.policy.keySet());
	runBlockDudeOptionLearning(hf, oR, lvlt, episodes, epsilon, stateCap,
		path + "Random Option");

	AbstractedOption oC = new AbstractedOption(hf, qPolicy.policy,
		dd.d.getActions(), 0., "Crafted");
	// craftOption(episodes, 0.0, stateCap);
	System.out.println(oC.size());
	runBlockDudeOptionLearning(hf, oC, lvlt, episodes, epsilon, stateCap,
		path + "Perfect Policy Option");

	List<Entry<AbstractedOption, Long>> topOs = new ArrayList<Entry<AbstractedOption, Long>>();
	for (PolicyBlocksPolicy merge : toMerge) {
	    AbstractedOption tempO = new AbstractedOption(hf, merge.policy,
		    dd.d.getActions(), 0.0, "top");
	    topOs.add(new AbstractMap.SimpleEntry<AbstractedOption, Long>(
		    tempO, runBlockDudeTopLearning(hf, tempO, lvlt, episodes,
			    epsilon, stateCap, "TOP")));
	}
	Collections.sort(topOs,
		new Comparator<Entry<AbstractedOption, Long>>() {
		    @Override
		    public int compare(Entry<AbstractedOption, Long> arg0,
			    Entry<AbstractedOption, Long> arg1) {
			return arg0.getValue().compareTo(arg1.getValue());
		    }
		});

	System.out.println(topOs.get(0).getValue() + " "
		+ topOs.get(1).getValue());
	int maxTopOs = toMerge.size() / 3;
	for (int t = 1; t <= maxTopOs; t++) {
	    runBlockDudeOptionLearning(hf, topOs.get(t - 1).getKey(), lvlt,
		    episodes, epsilon, stateCap, path + "Transfer Option-" + t);
	}

	System.out.println("Starting PolicyBlocks merge.");
	long pTime = System.currentTimeMillis();
	List<Entry<AbstractedPolicy, Double>> vanillaAbs = AbstractedPolicy
		.naivePowerMerge(hf, toMerge, 3, Integer.MAX_VALUE);
	System.out.println("Finished PolicyBlocks merge in "
		+ (System.currentTimeMillis() - pTime) / 1000.0 + " seconds.");
	AbstractedOption pbo = new AbstractedOption(hf, vanillaAbs.get(0)
		.getKey().getPolicy(), dd.d.getActions(), 0.0, "PolicyBlocks");
	runBlockDudeOptionLearning(hf, pbo, lvlt, episodes, epsilon, stateCap,
		path + "PolicyBlocks");

	System.out.println("Experiment finished. Took "
		+ (System.currentTimeMillis() - startTime) / 1000.0
		+ " seconds.");
    }

    public static void removePlatforms(PolicyBlocksPolicy p) {
	for (StateHashTuple sh : p.policy.keySet()) {
	    for (ObjectInstance oi : sh.s.getObjectsOfTrueClass("platform")) {
		sh.s.removeObject(oi.getName());
		sh.computeHashCode();
	    }
	}
	for (StateHashTuple sh : p.qpolicy.keySet()) {
	    for (ObjectInstance oi : sh.s.getObjectsOfTrueClass("platform")) {
		sh.s.removeObject(oi.getName());
		sh.computeHashCode();
	    }
	}
    }
}
