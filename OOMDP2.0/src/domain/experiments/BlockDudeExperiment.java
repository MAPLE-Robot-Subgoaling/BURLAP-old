package domain.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import burlap.behavior.policyblocks.AbstractedOption;
import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
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
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;
import domain.blockdude.BlockDudeDomain;
import domain.blockdude.BlockDudeDomain.DomainData;
import domain.blockdude.BlockDudeStateParser;
import domain.blockdude.BlockDudeVisualizer;

public class BlockDudeExperiment {
    public static PolicyBlocksPolicy runBlockDudeLearning(StateHashFactory hf,
	    char[][] level, int episodes, double epsilon, int maxsteps) {
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

	BlockDudeStateParser sp = new BlockDudeStateParser(dd.d);

	Visualizer v = BlockDudeVisualizer.getVisualizer(dd.c.minx, dd.c.maxx,
		dd.c.miny, dd.c.maxy);

	for (int i = 0; i < episodes; i++) {
	    EpisodeAnalysis analyzer = new EpisodeAnalysis();
	    analyzer = Q.runLearningEpisodeFrom(dd.s);
	    if (analyzer.numTimeSteps() < maxsteps || i % 500 == 0) {
		System.out.println("Episode " + i + ": "
			+ analyzer.numTimeSteps());
	    }
	    analyzer.writeToFile(String.format("output/e%03d", i), sp);
	}

	// new EpisodeSequenceVisualizer(v, dd.d, sp, "output");
	return policy;
    }

    public static PolicyBlocksPolicy runBlockDudeOptionLearning(
	    StateHashFactory hf, Option o, char[][] level, int episodes,
	    double epsilon, int maxsteps) {
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

	Q.addNonDomainReferencedAction(o);
	PolicyBlocksPolicy policy = new PolicyBlocksPolicy(Q, epsilon);
	Q.setLearningPolicy(policy);

	BlockDudeStateParser sp = new BlockDudeStateParser(dd.d);

	Visualizer v = BlockDudeVisualizer.getVisualizer(dd.c.minx, dd.c.maxx,
		dd.c.miny, dd.c.maxy);

	for (int i = 0; i < episodes; i++) {
	    EpisodeAnalysis analyzer = new EpisodeAnalysis();
	    analyzer = Q.runLearningEpisodeFrom(dd.s);
	    if (analyzer.numTimeSteps() < maxsteps || i % 500 == 0) {
		System.out.println("Episode " + i + ": "
			+ analyzer.numTimeSteps());
	    }
	    analyzer.writeToFile(String.format("output/e%03d", i), sp);
	}

	new EpisodeSequenceVisualizer(v, dd.d, sp, "output");
	return policy;
    }

    public static void main(String[] args) {
	String path = "C:/Users/Allison/Desktop/";
	// 1500
	char[][] lvla = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', '<', ' ', ' ',
			' ', 't', ' ', ' ', 't', 't', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', 'b', ' ',
			't', ' ', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', 'b', 'b',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 't',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' } };
	// 3000
	char[][] lvlb = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			'<', 't', ' ', ' ', 't', 't', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ',
			't', ' ', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', 'b', 'b',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 't',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' } };
	// 500
	char[][] lvlc = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', 'b', ' ', ' ', ' ', ' ', ' ', ' ',
			'<', 't', 'b', ' ', 't', 't', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', ' ', ' ',
			't', ' ', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', 'b', 't', ' ', 'b', 'b',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 't',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' } };
	// 1000
	char[][] lvld = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 'b', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 'b', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			'>', 't', ' ', 'b', 't', 't', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', ' ', 't', 'b', ' ',
			't', ' ', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', ' ', 'b',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 't',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' } };
	// 1500
	char[][] lvle = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 'b', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 'b', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', 'b', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			'<', 't', ' ', ' ', 't', 't', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', 'b', ' ', ' ', 't', ' ', ' ',
			't', ' ', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', 'b', 't', ' ', 'b', 'b',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', ' ', 't',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' } };
	char[][] lvlt = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'b', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', 'b', 'b', ' ', '<', ' ' },
		{ ' ', ' ', 't', 't', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', 't', ' ', ' ', 't', 't', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ',
			't', ' ', 't', 't', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', 'b', 'b', ' ', 't', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 'g', ' ', ' ', ' ', 't', 't', 't', ' ', ' ', 'b', 't',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ' } };

	double epsilon = 0.0;
	int episodes = 1000;
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

	toMerge.add(runBlockDudeLearning(hf, lvla, 1000, 0.0, 200));
	System.gc();
	toMerge.add(runBlockDudeLearning(hf, lvla, 1000, 0.0, 200));
	System.gc();
	toMerge.add(runBlockDudeLearning(hf, lvla, 1000, 0.0, 200));
	System.gc();
	toMerge.add(runBlockDudeLearning(hf, lvlb, 2000, 0.0, 200));
	System.gc();
	toMerge.add(runBlockDudeLearning(hf, lvlb, 2000, 0.0, 200));
	System.gc();
	toMerge.add(runBlockDudeLearning(hf, lvla, 2000, 0.0, 200));
	System.gc();

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

	runBlockDudeLearning(hf, lvlc, 10000, 0.0, 200);
	runBlockDudeOptionLearning(hf, o1, lvlc, 10000, 0.0, 200);

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
