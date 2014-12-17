package examples;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase;
import burlap.behavior.singleagent.vfa.cmac.CMACFeatureDatabase.TilingArrangement;
import burlap.domain.singleagent.lunarlander.LLStateParser;
import burlap.domain.singleagent.lunarlander.LLVisualizer;
import burlap.domain.singleagent.lunarlander.LunarLanderDomain;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.visualizer.Visualizer;

public class VFAExample {

    protected LunarLanderDomain lld;
    protected Domain domain;
    protected RewardFunction rf;
    protected TerminalFunction tf;
    protected StateParser sp;
    protected State initialState;

    /**
     * @param args
     */
    public static void main(String[] args) {

	VFAExample example = new VFAExample();
	String outputPath = "output"; // directory to record results

	example.runCMACVFA(outputPath);
	example.visualize(outputPath);

    }

    public VFAExample() {

	lld = new LunarLanderDomain();
	domain = lld.generateDomain();
	rf = new LLRF(domain);
	tf = new SinglePFTF(domain.getPropFunction(LunarLanderDomain.PFONPAD));
	sp = new LLStateParser(domain);

	initialState = LunarLanderDomain.getCleanState(domain, 1);
	LunarLanderDomain.setAgent(initialState, 0., 5.0, 0.0);
	LunarLanderDomain.setPad(initialState, 75., 95., 0., 10.);

    }

    public void visualize(String outputPath) {
	Visualizer v = LLVisualizer.getVisualizer(lld);
	EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v,
		domain, sp, outputPath);
    }

    public void runCMACVFA(String outputPath) {

	if (!outputPath.endsWith("/")) {
	    outputPath = outputPath + "/";
	}

	int nTilings = 5;
	CMACFeatureDatabase cmac = new CMACFeatureDatabase(nTilings,
		TilingArrangement.RANDOMJITTER);
	double resolution = 10.;
	cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
		domain.getAttribute(LunarLanderDomain.AATTNAME),
		2 * lld.getAngmax() / resolution);
	cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
		domain.getAttribute(LunarLanderDomain.XATTNAME),
		(lld.getXmax() - lld.getXmin()) / resolution);
	cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
		domain.getAttribute(LunarLanderDomain.YATTNAME),
		(lld.getYmax() - lld.getYmin()) / resolution);
	cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
		domain.getAttribute(LunarLanderDomain.VXATTNAME),
		2 * lld.getVmax() / resolution);
	cmac.addSpecificationForAllTilings(LunarLanderDomain.AGENTCLASS,
		domain.getAttribute(LunarLanderDomain.VYATTNAME),
		2 * lld.getVmax() / resolution);

	double defaultQ = 0.5;

	ValueFunctionApproximation vfa = cmac.generateVFA(defaultQ / nTilings);

	GradientDescentSarsaLam agent = new GradientDescentSarsaLam(domain, rf,
		tf, 0.99, vfa, 0.02, 10000, 0.5);

	for (int i = 0; i < 5000; i++) {
	    EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState); // run
									     // learning
									     // episode
	    ea.writeToFile(String.format("%se%04d", outputPath, i), sp); // record
									 // episode
									 // to a
									 // file
	    System.out.println(i + ": " + ea.numTimeSteps()); // print the
							      // performance of
							      // this episode
	}

    }

    class LLRF implements RewardFunction {

	double goalReward = 1000.0;
	double collisionReward = -100.0;
	double defaultReward = -1.0;

	PropositionalFunction onGround;
	PropositionalFunction touchingSurface;
	PropositionalFunction touchingPad;
	PropositionalFunction onPad;

	public LLRF(Domain domain) {

	    this.onGround = domain
		    .getPropFunction(LunarLanderDomain.PFONGROUND);
	    this.touchingSurface = domain
		    .getPropFunction(LunarLanderDomain.PFTOUCHSURFACE);
	    this.touchingPad = domain.getPropFunction(LunarLanderDomain.PFTPAD);
	    this.onPad = domain.getPropFunction(LunarLanderDomain.PFONPAD);

	}

	public LLRF(Domain domain, double goalReward, double collisionReward,
		double defaultReward) {
	    this.goalReward = goalReward;
	    this.collisionReward = collisionReward;
	    this.defaultReward = defaultReward;

	    this.onGround = domain
		    .getPropFunction(LunarLanderDomain.PFONGROUND);
	    this.touchingSurface = domain
		    .getPropFunction(LunarLanderDomain.PFTOUCHSURFACE);
	    this.touchingPad = domain.getPropFunction(LunarLanderDomain.PFTPAD);
	    this.onPad = domain.getPropFunction(LunarLanderDomain.PFONPAD);
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {

	    if (sprime.somePFGroundingIsTrue(onPad)) {
		return goalReward;
	    }

	    if (sprime.somePFGroundingIsTrue(onGround)
		    || sprime.somePFGroundingIsTrue(touchingPad)
		    || sprime.somePFGroundingIsTrue(touchingSurface)) {
		return collisionReward;
	    }

	    return defaultReward;
	}

    }

}
