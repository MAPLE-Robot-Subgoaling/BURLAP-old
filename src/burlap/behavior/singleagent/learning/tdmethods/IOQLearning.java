package burlap.behavior.singleagent.learning.tdmethods;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.behavior.policyblocks.AbstractedPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class IOQLearning extends QLearning {
    protected List<Entry<Map<StateHashTuple, List<QValue>>, List<List<String>>>> sourcePolicies = new ArrayList<Entry<Map<StateHashTuple, List<QValue>>, List<List<String>>>>();
    protected Map<Integer, Byte> statesSeen = new HashMap<Integer, Byte>();
    public boolean policyReuse = false;

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

    public void addSourcePolicy(Map<StateHashTuple, List<QValue>> sourcePolicy) {
	sourcePolicies
		.add(new AbstractMap.SimpleEntry<Map<StateHashTuple, List<QValue>>, List<List<String>>>(
			sourcePolicy, null));
	policyReuse = true;
    }

    public void addSourcePolicies(List<Map<StateHashTuple, List<QValue>>> sourcePolicies) {
	for (Map<StateHashTuple, List<QValue>> sourcePolicy : sourcePolicies) {
	    addSourcePolicy(sourcePolicy);
	}
    }

    public void clearSourcePolicies() {
	sourcePolicies = new ArrayList<Entry<Map<StateHashTuple, List<QValue>>, List<List<String>>>>();
	policyReuse = false;
    }

    public Map<StateHashTuple, List<QValue>> getQPolicy() {
	Map<StateHashTuple, List<QValue>> qPolicy = new HashMap<StateHashTuple, List<QValue>>();
	
	for (Entry<StateHashTuple, QLearningStateNode> e: qIndex.entrySet()) {
	    List<QValue> qS = new ArrayList<QValue>();
	    qS.addAll(e.getValue().qEntry);
	    qPolicy.put(e.getKey(), qS);
	}
	 
	return qPolicy;
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
	    // Go through each source policy and check for a match
	    if (policyReuse && statesSeen.get(curState.hashCode()) != null) {
		System.out.println("WOOO");
		statesSeen.put(curState.s.hashCode(), (byte) 1);

		Map<AbstractGroundedAction, List<Double>> qValues = new HashMap<AbstractGroundedAction, List<Double>>();
		for (Entry<Map<StateHashTuple, List<QValue>>, List<List<String>>> sourcePolicy : sourcePolicies) {
		    if (sourcePolicy.getValue() == null) {
			List<State> ss = new ArrayList<State>();
			State withRespectTo = sourcePolicy.getKey().keySet()
				.iterator().next().s;
			ss.add(curState.s);
			ss.add(withRespectTo);
			sourcePolicy
				.setValue(AbstractedPolicy
					.generateAllCombinations(
						withRespectTo,
						AbstractedPolicy
							.greatestCommonGeneralization(ss)));
		    }

		    List<List<String>> ocombs = sourcePolicy.getValue();
		    for (List<String> ocomb : ocombs) {
			State newS = AbstractedPolicy.formState(curState.s,
				ocomb);
			List<QValue> vals = sourcePolicy.getKey().get(
				hashingFactory.hashState(newS));

			for (QValue val : vals) {
			    if (qValues.get(val.a) != null) {
				qValues.get(val.a).add(val.q);
			    } else {
				List<Double> qVals = new ArrayList<Double>();
				qVals.add(val.q);
				qValues.put(val.a, qVals);
			    }
			}
		    }
		}
		
		System.out.println(qIndex.get(curState));
		for (QValue Q : qIndex.get(curState).qEntry) {
		    // Average all of the Q-values (including the new domains initialization?)
		    double avg = Q.q;
		    for (Double qDub : qValues.get(Q.a)) {
			avg += qDub;
		    }
		    
		    avg /= (qValues.get(Q.a).size() + 1.0);
		    Q.q = avg;
		    // need to check to make sure this is persistent
		}
		System.out.println(qIndex.get(curState));
	    }
	    
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
		if (!updateAction.isPrimitive()) {
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
