package burlap.behavior.PolicyBlock;

import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

public class PQLearning extends QLearning {

	public PQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
			double gamma, StateHashFactory hashingFactory, double qInit,
			double learningRate) {
		super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate);
	}
	
	public PQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
			double gamma, StateHashFactory hashingFactory, double qInit,
			double learningRate, int maxEpisodeSize) {
		super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
				maxEpisodeSize);
	}
	
	public PQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
			double gamma, StateHashFactory hashingFactory, double qInit,
			double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
				learningPolicy, maxEpisodeSize);
	}
	
	public PQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
			double gamma, StateHashFactory hashingFactory,
			ValueFunctionInitialization qInit, double learningRate,
			Policy learningPolicy, int maxEpisodeSize) {
		super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
				learningPolicy, maxEpisodeSize);
	}
	
	public Map<StateHashTuple, QLearningStateNode> getQRepresentation() {
		return super.qIndex;
	}
}
