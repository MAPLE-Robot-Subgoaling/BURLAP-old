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
import domain.blockdude.BlockDudeStateParser;

public class BlockDudeExperiment {
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
	    StateHashFactory hf, List<Option> os, char[][] level, int episodes,
	    double epsilon, int maxsteps, String path) throws IOException {
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
	return new AbstractedOption(hf, policy, d.getActions(), "random");
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

	return new AbstractedOption(hf, p.policy, dd.d.getActions(), "Crafted");
    }

    public static void main(String[] args) throws IOException {
	String path = "C:/Users/Allison/Desktop/burlap/";

	char[][] lvla = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlb = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlc = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 'b', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvld = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvle = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'b', 'b', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', 'b', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlf = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlg = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 'b', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'b', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', 'b', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlh = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvli = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 'b', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlj = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 'b', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlt = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', 'b', ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', 'b', '<', 'b' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', 'b', 't', ' ', ' ', ' ', 'b', ' ', 't', ' ', 't', 't', 't' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 'b', 'b', ' ', ' ', 't', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ' } };
	
	double epsilon = 0.4;
	int episodes = 5000;
	int stateCap = 300;
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
	// toMerge.add(runBlockDudeBaseLearning(hf, lvlf, episodes, epsilon,
	// stateCap, "F"));
	// toMerge.add(runBlockDudeBaseLearning(hf, lvlg, episodes, epsilon,
	// stateCap, "G"));
	// toMerge.add(runBlockDudeBaseLearning(hf, lvlh, episodes, epsilon,
	// stateCap, "H"));
	// toMerge.add(runBlockDudeBaseLearning(hf, lvli, episodes, epsilon,
	// stateCap, "I"));
	// toMerge.add(runBlockDudeBaseLearning(hf, lvlj, episodes, epsilon,
	// stateCap, "J"));

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
		.getPolicy(), dd.d.getActions(), "one");
	System.out.println("Merging complete. Took "
		+ (System.currentTimeMillis() - mTime) / 1000.0 + " seconds.");

	PolicyBlocksPolicy qPolicy =
	runBlockDudeOptionLearning(hf,
		new ArrayList<Option>(), lvlt, episodes, epsilon, stateCap,
		path + "Q-Learning");
	
	runBlockDudeOptionLearning(hf, o1, lvlt, episodes, epsilon, stateCap,
		path + "P-MODAL");
	
	AbstractedOption oR = generateRandomOption(hf, dd.d, dd.d.getActions(),
		qPolicy.policy.keySet());
	runBlockDudeOptionLearning(hf, oR, lvlt, episodes, epsilon, stateCap,
		path + "Random");
	
	AbstractedOption oC = new AbstractedOption(hf, qPolicy.policy, dd.d.getActions(), "Crafted");
	// craftOption(episodes, 0.0, stateCap);
	System.out.println(oC.size());
	runBlockDudeOptionLearning(hf, oC, lvlt, episodes, epsilon, stateCap,
		path + "Hand Crafted");

	System.out.println("Experiment finished. Took "
		+ (System.currentTimeMillis() - startTime) / 1000.0
		+ " seconds.");
    }

    public static void removePlatforms(PolicyBlocksPolicy p) {
	for (StateHashTuple sh : p.policy.keySet()) {
	    for (ObjectInstance oi : sh.s.getObjectsOfTrueClass("platform")) {
		sh.s.removeObject(oi.getName());
	    }
	}
    }
}
