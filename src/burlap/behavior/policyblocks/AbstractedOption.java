package burlap.behavior.policyblocks;

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
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

@Deprecated
public class AbstractedOption extends Option {
    private Map<StateHashTuple, GroundedAction> policy;
    private Map<StateHashTuple, List<GroundedAction>> abstractedPolicy;
    private List<List<String>> ocombs;
    private List<String> combToUse;
    private Map<String, Integer> gcg;
    private StateHashFactory hf;
    private List<Action> actions;
    private Set<StateHashTuple> visited;
    private Random rand;
    private double termProb;
    private boolean abstractionGenerated;
    private boolean roll;

    public AbstractedOption(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> policy, List<Action> actions,
	    double termProb, String name) {
	this(hf, policy, actions, termProb, true, null, name);
    }

    public AbstractedOption(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> policy, List<Action> actions,
	    double termProb, List<String> combToUse, String name) {
	this(hf, policy, actions, termProb, true, combToUse, name);
    }

    public AbstractedOption(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> policy, List<Action> actions,
	    double termProb, boolean roll, List<String> combToUse, String name) {
	if (policy.isEmpty()) {
	    throw new IllegalArgumentException("Empty policy provided.");
	} else if (actions.isEmpty()) {
	    throw new IllegalArgumentException("No actions provided.");
	} else if (termProb > 1 || termProb < 0) {
	    throw new IllegalArgumentException(
		    "Invalid termination probability");
	}

	this.policy = policy;
	this.hf = hf;
	super.name = "AO-" + name;
	this.visited = new HashSet<StateHashTuple>();
	this.rand = new Random();
	this.actions = actions;
	this.abstractedPolicy = new HashMap<StateHashTuple, List<GroundedAction>>();
	this.gcg = new HashMap<String, Integer>();
	this.ocombs = new ArrayList<List<String>>();
	this.termProb = termProb;
	this.abstractionGenerated = false;
	this.roll = roll;
	this.combToUse = combToUse;
    }

    @Override
    public boolean isMarkov() {
	return true;
    }

    @Override
    public boolean usesDeterministicTermination() {
	return termProb == 0.;
    }

    @Override
    public boolean usesDeterministicPolicy() {
	return true;
    }

    @Override
    public double probabilityOfTermination(State incoming, String[] params) {
	List<StateHashTuple> states = getAbstractedStates(incoming);
	GroundedAction ga = getActionFromAbstractions(states);

	if (ga == null || visited.contains(hf.hashState(incoming))) {
	    visited.clear();
	    return 1.;
	}

	return termProb;
    }

    @Override
    public void initiateInStateHelper(State s, String[] params) {
	// Nothing to do here
    }

    @Override
    public GroundedAction oneStepActionSelection(State incoming, String[] params) {
	List<StateHashTuple> states = getAbstractedStates(incoming);

	if (visited.contains(hf.hashState(incoming))) {
	    visited.clear();
	    return null;
	}

	visited.add(hf.hashState(incoming));
	return getActionFromAbstractions(states);
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State incoming,
	    String[] params) {
	List<ActionProb> aprobs = new ArrayList<ActionProb>();
	GroundedAction ga = oneStepActionSelection(incoming, params);

	for (Action a : actions) {
	    if (a.equals(ga.action)) {
		// If the action selection is in the set of actions stored,
		// return 1.
		ActionProb p = new ActionProb(ga, 1.);
		aprobs.add(p);
	    } else {
		// Otherwise, return 0.
		ActionProb p = new ActionProb(ga, 0.);
		aprobs.add(p);
	    }
	}

	return aprobs;
    }

    @Override
    public boolean applicableInState(State incoming, String[] params) {
	if (visited.contains(hf.hashState(incoming))) {
	    visited.clear();
	    return false;
	}

	List<StateHashTuple> states = getAbstractedStates(incoming);
	return getActionFromAbstractions(states) != null;
    }

    /**
     * Gets the best action from the abstracted policy using the provided states
     * 
     * @param states
     * @return the action best associated with these states
     */
    private GroundedAction getActionFromAbstractions(List<StateHashTuple> states) {
	List<GroundedAction> gas = new ArrayList<GroundedAction>();
	List<StateHashTuple> definedFor = new ArrayList<StateHashTuple>();

	for (StateHashTuple state : states) {
	    if (!abstractedPolicy.containsKey(state)) {
		continue;
	    }

	    List<GroundedAction> curGAs = abstractedPolicy.get(state);
	    gas.addAll(curGAs);
	    definedFor.add(state);
	}

	GroundedAction ga = null;

	if (gas.size() > 0) {
	    if (gas.size() == 1) {
		return gas.get(0);
	    }

	    if (roll) {
		// Weighted dice roll for selection
		ga = gas.get(rand.nextInt(gas.size()));
	    } else {
		// Most common for selection
		Map<GroundedAction, Integer> weights = new HashMap<GroundedAction, Integer>();
		for (GroundedAction gac : gas) {
		    weights.put(gac,
			    weights.containsKey(gac) ? weights.get(gac) + 1 : 1);
		}

		int max = 0;
		for (Entry<GroundedAction, Integer> e : weights.entrySet()) {
		    if (e.getValue() > max) {
			ga = e.getKey();
			max = e.getValue();
		    }
		}
	    }

	    // After the selection is made, it is cached for the future
	    for (StateHashTuple state : definedFor) {
		List<GroundedAction> aList = new ArrayList<GroundedAction>();
		aList.add(ga);
		abstractedPolicy.put(state, aList);
	    }
	}

	return ga;
    }

    private List<StateHashTuple> getAbstractedStates(State incoming) {
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}

	List<StateHashTuple> states = new ArrayList<StateHashTuple>();

	if (combToUse != null) {
	    states.add(hf.hashState(AbstractedPolicy.formState(incoming,
		    combToUse)));
	} else {
	    for (List<String> ocomb : ocombs) {
		states.add(hf.hashState(AbstractedPolicy.formState(incoming,
			ocomb)));
	    }
	}

	return states;
    }

    private void generateAbstraction(State incoming) {
	State withRespectTo = policy.keySet().iterator().next().s;
	List<State> ss = new ArrayList<State>();
	ss.add(incoming);
	ss.add(withRespectTo);
	this.gcg = AbstractedPolicy.greatestCommonGeneralization(ss);
	this.ocombs = AbstractedPolicy.generateAllCombinations(withRespectTo,
		gcg);

	for (Entry<StateHashTuple, GroundedAction> e : policy.entrySet()) {
	    if (!actions.contains(e.getValue().action)) {
		// If the incoming action is not in the target domain's
		// actions space, omit it.
		continue;
	    }

	    // Map the action to the target domain
	    GroundedAction curGA = new GroundedAction(actions.get(actions
		    .indexOf(e.getValue().action)), e.getValue().params);

	    /*
	     * if (combToUse != null) { State newS = AbstractedPolicy
	     * .formState(e.getKey().s, combToUse);
	     * 
	     * List<GroundedAction> aList; if
	     * (!abstractedPolicy.containsKey(hf.hashState(newS))) { aList = new
	     * ArrayList<GroundedAction>(); aList.add(curGA);
	     * abstractedPolicy.put(hf.hashState(newS), aList); } else { // TODO
	     * look into this
	     * abstractedPolicy.get(hf.hashState(newS)).add(curGA); } } else {
	     */
	    for (List<String> ocomb : ocombs) {
		State newS = AbstractedPolicy.formState(e.getKey().s, ocomb);
		List<GroundedAction> aList;
		if (!abstractedPolicy.containsKey(hf.hashState(newS))) {
		    aList = new ArrayList<GroundedAction>();
		    aList.add(curGA);
		    abstractedPolicy.put(hf.hashState(newS), aList);
		} else {
		    abstractedPolicy.get(hf.hashState(newS)).add(curGA);
		}
	    }
	    // }
	}

	abstractionGenerated = true;
    }

    public void resetOption() {
	resetAbstraction();
	hf = null;
	actions = new ArrayList<Action>();
	visited.clear();
    }

    public void resetAbstraction() {
	abstractionGenerated = false;
	abstractedPolicy = new HashMap<StateHashTuple, List<GroundedAction>>();
	gcg = new HashMap<String, Integer>();
	ocombs = new ArrayList<List<String>>();
    }

    public void setHashFactory(StateHashFactory hf) {
	this.hf = hf;
    }

    public void setActions(List<Action> actions) {
	if (actions.isEmpty()) {
	    throw new RuntimeException("No actions provided.");
	}

	this.actions = actions;
    }

    public int size() {
	return policy.size();
    }

    public void setRoll(boolean roll) {
	this.roll = roll;
    }
}
