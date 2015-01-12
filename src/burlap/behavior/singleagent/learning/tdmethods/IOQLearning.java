package burlap.behavior.singleagent.learning.tdmethods;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class IOQLearning extends QLearning {
    public IOQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate) {
	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate);
    }

    public IOQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate, int maxEpisodeSize) {
	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		maxEpisodeSize);
    }

    public IOQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate, int maxEpisodeSize, boolean usePsi) {
	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		maxEpisodeSize, usePsi);
    }

    public IOQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate, Policy learningPolicy, int maxEpisodeSize) {
	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		learningPolicy, maxEpisodeSize);
    }

    public IOQLearning(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory,
	    ValueFunctionInitialization qInit, double learningRate,
	    Policy learningPolicy, int maxEpisodeSize) {
	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		learningPolicy, maxEpisodeSize);
    }

    @Override
    public EpisodeAnalysis runLearningEpisodeFrom(State initialState,
	    int maxSteps) {
	this.toggleShouldAnnotateOptionDecomposition(shouldAnnotateOptions);
	EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
	StateHashTuple curState = this.stateHash(initialState);
	eStepCounter = 0;
	maxQChangeInLastEpisode = 0.;

	while (!tf.isTerminal(curState.s) && eStepCounter < maxSteps) {
	    GroundedAction absAction = (GroundedAction) learningPolicy
		    .getAction(curState.s);
	    GroundedAction primAction;

	    if (!absAction.action.isPrimitive()) {
		primAction = ((Option) absAction.action)
			.oneStepActionSelection(curState.s, absAction.params);
		if (primAction == null) {
		    QValue nullQ = this.getQ(curState, absAction);
		    // This action isn't defined for the option, lower its
		    // Q-Value to the current minimum
		    for (QValue Q : this.getQs(curState)) {
			if (Q.q < nullQ.q) {
			    nullQ.q = Q.q;
			}
		    }

		    nullQ.q--;
		    continue;
		}
	    } else {
		primAction = absAction;
	    }

	    StateHashTuple nextState = this.stateHash(primAction
		    .executeIn(curState.s));
	    double maxQ = 0.;

	    if (!tf.isTerminal(nextState.s)) {
		maxQ = this.getMaxQ(nextState);
	    }

	    // manage option specifics
	    double r = 0.;
	    double discount = this.gamma;

	    r = rf.reward(curState.s, primAction, nextState.s);
	    eStepCounter++;
	    ea.recordTransitionTo(nextState.s, absAction, r);

	    for (Action updateAction : domain.getActions()) {
		// Intra-option stuff
		GroundedAction updateGA;
		if (updateAction instanceof Option) {
		    updateGA = ((Option) updateAction).oneStepActionSelection(
			    curState.s, absAction.params);

		    if (updateGA == null
			    || !primAction.action.equals(updateGA.action)) {
			continue;
		    }
		} else {
		    if (!primAction.action.equals(updateAction)) {
			continue;
		    }

		    updateGA = primAction;
		}

		QValue curQ = this.getQ(curState, updateGA);
		double oldQ = curQ.q;

		// update Q-value
		curQ.q = curQ.q
			+ this.learningRate.pollLearningRate(curState.s,
				updateGA) * (r + (discount * maxQ) - curQ.q);

		double deltaQ = Math.abs(oldQ - curQ.q);
		if (deltaQ > maxQChangeInLastEpisode) {
		    maxQChangeInLastEpisode = deltaQ;
		}
	    }

	    // move on
	    curState = nextState;
	}

	if (episodeHistory.size() >= numEpisodesToStore) {
	    episodeHistory.poll();
	}

	episodeHistory.offer(ea);

	return ea;
    }
}
