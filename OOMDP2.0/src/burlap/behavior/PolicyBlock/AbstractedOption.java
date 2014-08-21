package burlap.behavior.PolicyBlock;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedOption extends Option {
    public Map<StateHashTuple, GroundedAction> policy;
    public Map<StateHashTuple, Entry<Integer, List<GroundedAction>>> abstractedPolicy;
    private StateHashFactory hashFactory;
    private Set<GroundedAction> actions;
    private Set<StateHashTuple> visited;
    private State withRespectTo;
    private Random rand;
    private boolean abstractionGenerated = false;

    public AbstractedOption(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> policy) {
	this.policy = policy;
	this.hashFactory = hf;
	this.actions = new HashSet<GroundedAction>();
	super.name = "AbstractedOption";
	this.parameterClasses = new String[0];
	this.parameterOrderGroup = new String[0];
	this.visited = new HashSet<StateHashTuple>();
	this.rand = new Random();
	this.abstractedPolicy = new HashMap<StateHashTuple, Entry<Integer, List<GroundedAction>>>();
	this.withRespectTo = policy.keySet().iterator().next().s;
    }

    @Override
    public boolean isMarkov() {
	return true;
    }

    @Override
    public boolean usesDeterministicTermination() {
	return true;
    }

    @Override
    public boolean usesDeterministicPolicy() {
	return true;
    }

    @Override
    public double probabilityOfTermination(State incoming, String[] params) {
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}
	State abs = AbstractedPolicy.findLimitingState(
		AbstractedPolicy.generateLCIMappingState(incoming),
		AbstractedPolicy.generateLCIMappingState(withRespectTo),
		incoming, withRespectTo);
	Entry<Integer, List<GroundedAction>> tempE = abstractedPolicy
		.get(hashFactory.hashState(abs));
	if (tempE == null || visited.contains(hashFactory.hashState(abs))) {
	    visited.clear();
	    return 1.;
	}

	return 0.;
    }

    @Override
    public void initiateInStateHelper(State s, String[] params) {
	// Nothing to do here
    }

    @Override
    public GroundedAction oneStepActionSelection(State incoming, String[] params) {
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}
	State abs = AbstractedPolicy.findLimitingState(
		AbstractedPolicy.generateLCIMappingState(incoming),
		AbstractedPolicy.generateLCIMappingState(withRespectTo),
		incoming, withRespectTo);
	if (visited.contains(hashFactory.hashState(abs))) {
	    visited.clear();
	    return null;
	}

	visited.add(hashFactory.hashState(abs));
	Entry<Integer, List<GroundedAction>> tempE = abstractedPolicy
		.get(hashFactory.hashState(abs));

	// not == because of object equality
	// If the action selection has not been initialized, make a
	// selection from the list of actions defined for that state.
	if (tempE.getKey().equals(-1)) {
	    // Weighted dice roll
	    int index = rand.nextInt(tempE.getValue().size());
	    AbstractMap.SimpleEntry<Integer, List<GroundedAction>> newE = new AbstractMap.SimpleEntry<Integer, List<GroundedAction>>(
		    index, tempE.getValue());
	    abstractedPolicy.put(hashFactory.hashState(abs), newE);
	    tempE = newE;

	    // Most common action
	    /*
	     * List<GroundedAction> gas = abstractedPolicy.get(
	     * hashFactory.hashState(abs)).getValue(); Map<GroundedAction,
	     * Integer> weights = new HashMap<GroundedAction, Integer>();
	     * 
	     * for (GroundedAction ga : gas) { // Updates the weight map by
	     * incrementing the counter weights.put(ga, weights.get(ga) == null
	     * ? 1 : weights.get(ga) + 1); } Entry<GroundedAction, Integer>
	     * mostCommon = null; for (Entry<GroundedAction, Integer> weight :
	     * weights.entrySet()) { // Finds the most common grounded action if
	     * (mostCommon == null || weight.getValue() > mostCommon.getValue())
	     * { mostCommon = weight; } } AbstractMap.SimpleEntry<Integer,
	     * List<GroundedAction>> newE = new AbstractMap.SimpleEntry<Integer,
	     * List<GroundedAction>>( gas.indexOf(mostCommon.getKey()), gas);
	     * abstractedPolicy.put(hashFactory.hashState(abs), newE); tempE =
	     * newE;
	     */
	}

	return tempE.getValue().get(tempE.getKey());
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State incoming,
	    String[] params) {
	// TODO add stochastic termination
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}
	List<ActionProb> aprobs = new ArrayList<ActionProb>();
	GroundedAction ga = oneStepActionSelection(incoming, params);
	for (GroundedAction a : actions) {
	    if (ga != null && ga.action.equals(a)) {
		// If the action selection is in the set of actions stored,
		// return 1.
		ActionProb p = new ActionProb(a, 1.);
		aprobs.add(p);
	    } else {
		// Otherwise, return 0.
		ActionProb p = new ActionProb(a, 0.);
		aprobs.add(p);
	    }
	}

	return aprobs;
    }

    @Override
    /**
     * Abstracts the incoming state with respect to an arbitrary state in the option, and then checks for existence in the option.
     */
    public boolean applicableInState(State incoming, String[] params) {
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}
	State absIncoming = AbstractedPolicy.findLimitingState(
		AbstractedPolicy.generateLCIMappingState(incoming),
		AbstractedPolicy.generateLCIMappingState(withRespectTo),
		incoming, withRespectTo);

	if (visited.contains(hashFactory.hashState(absIncoming))) {
	    visited.clear();
	    return false;
	}

	return abstractedPolicy.get(hashFactory.hashState(absIncoming)) != null;
    }

    private void generateAbstraction(State incoming) {
	if (!abstractionGenerated) {
	    for (Entry<StateHashTuple, GroundedAction> e : policy.entrySet()) {
		State curState = AbstractedPolicy.findLimitingState(
			AbstractedPolicy.generateLCIMappingState(e.getKey().s),
			AbstractedPolicy.generateLCIMappingState(incoming),
			e.getKey().s, incoming);
		GroundedAction curGA = e.getValue();

		if (!actions.contains(curGA)) {
		    actions.add(curGA);
		}

		if (abstractedPolicy.get(hashFactory.hashState(curState)) == null) {
		    List<GroundedAction> tempArr = new ArrayList<GroundedAction>();
		    tempArr.add(curGA);
		    // -1 initialization means the selection hasn't been made
		    AbstractMap.SimpleEntry<Integer, List<GroundedAction>> tempE = new AbstractMap.SimpleEntry<Integer, List<GroundedAction>>(
			    -1, tempArr);
		    abstractedPolicy
			    .put(hashFactory.hashState(curState), tempE);
		} else {
		    Entry<Integer, List<GroundedAction>> tempE = abstractedPolicy
			    .get(hashFactory.hashState(curState));
		    List<GroundedAction> tempArr = tempE.getValue();
		    tempArr.add(curGA);
		    tempE.setValue(tempArr);
		    abstractedPolicy
			    .put(hashFactory.hashState(curState), tempE);
		}
	    }

	    abstractionGenerated = true;
	}
    }

    public void resetOption() {
	abstractionGenerated = false;
	abstractedPolicy = new HashMap<StateHashTuple, Entry<Integer, List<GroundedAction>>>();
    }

    public int size() {
	return policy.size();
    }
}
