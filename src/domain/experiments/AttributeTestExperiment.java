package domain.experiments;

import java.util.ArrayList;
import java.util.List;

import domain.attributetest.AttributeTestDomain;
import domain.taxiworld.TaxiWorldDomain;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.IOQLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.pod.PolicyBlocksPolicy;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class AttributeTestExperiment {
    protected static final double DISCOUNT = 0.995;
    protected static final double LEARNING_RATE = 0.995;

    public static PolicyBlocksPolicy runTrial(List<? extends Option> options,
	    int blocks, double qInit, int episodes, String path) {
	long sTime = System.currentTimeMillis();
	boolean log = path.equals("") ? false : true;
	System.out.println("Starting " + blocks + " block trial for "
		+ episodes + " episodes.");

	AttributeTestDomain atd = new AttributeTestDomain(30, 30);
	Domain d = atd.generateDomain();

	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(AttributeTestDomain.CLASSAGENT,
		d.getObjectClass(AttributeTestDomain.CLASSAGENT).attributeList);
	hf.setAttributesForClass(AttributeTestDomain.CLASSBLOCK,
		d.getObjectClass(AttributeTestDomain.CLASSBLOCK).attributeList);
	hf.setAttributesForClass(AttributeTestDomain.CLASSGOAL,
		d.getObjectClass(AttributeTestDomain.CLASSGOAL).attributeList);

	IOQLearning Q;
	Q = new IOQLearning(d, new UniformCostRF(), new SinglePFTF(
		d.getPropFunction(AttributeTestDomain.PFGOAL)), DISCOUNT, hf,
		qInit, LEARNING_RATE, Integer.MAX_VALUE);

	PolicyBlocksPolicy ret = new PolicyBlocksPolicy(Q, 0.05);
	Q.setLearningPolicy(ret);

	for (Option option : options) {
	    Q.addNonDomainReferencedAction(option);
	}

	for (int e = 0; e < episodes; e++) {
	    State s = AttributeTestDomain
		    .getCleanState(d, 1, 1, 25, 25, blocks);

	    EpisodeAnalysis eps = new EpisodeAnalysis();
	    System.out.println(s);
	    AttributeTestDomain.printState(s);
	    eps = Q.runLearningEpisodeFrom(s);
	    AttributeTestDomain.printState(eps.stateSequence
		    .get(eps.stateSequence.size() - 1));
	}

	System.out.println("Finished " + blocks + " block trial for "
		+ episodes + " episodes in "
		+ (System.currentTimeMillis() - sTime) / 1000.0 + " seconds.");

	return ret;
    }

    public static void main(String[] args) {
	PolicyBlocksPolicy p1 = runTrial(new ArrayList<Option>(), 3, 0., 1, "");
    }
}
