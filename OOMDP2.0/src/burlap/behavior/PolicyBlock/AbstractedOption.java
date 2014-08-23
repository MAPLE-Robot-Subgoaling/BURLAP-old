package burlap.behavior.PolicyBlock;

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
    private Map<StateHashTuple, GroundedAction> policy;
    private Map<StateHashTuple, List<GroundedAction>> abstractedPolicy;
    private List<List<String>> ocombs;
    private Map<String, Integer> gci;
    private StateHashFactory hf;
    private Set<GroundedAction> actions;
    private Set<List<StateHashTuple>> visited;
    private Random rand;
    private boolean abstractionGenerated = false;
    private boolean ocombsGenerated = false;

    public AbstractedOption(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> policy, String name) {
	this.policy = policy;
	this.hf = hf;
	super.name = "AO-" + name;
	this.parameterClasses = new String[0];
	this.parameterOrderGroup = new String[0];
	this.visited = new HashSet<List<StateHashTuple>>();
	this.rand = new Random();
	this.actions = new HashSet<GroundedAction>();
	this.abstractedPolicy = new HashMap<StateHashTuple, List<GroundedAction>>();
	this.gci = new HashMap<String, Integer>();
	this.ocombs = new ArrayList<List<String>>();
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

	List<StateHashTuple> states = getAbstractedStates(incoming);
	GroundedAction ga = getActionFromAbstractions(states);
	if (ga == null || visited.contains(states)) {
	    visited.clear();
	    // TODO return rand.nextDouble();
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

	List<StateHashTuple> states = getAbstractedStates(incoming);
	if (visited.contains(states)) {
	    visited.clear();
	    return null;
	}

	visited.add(states);
	// System.out.println(getActionFromAbstractions(states) +
	// " selected for state: " + incoming);
	return getActionFromAbstractions(states);
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State incoming,
	    String[] params) {
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}

	List<ActionProb> aprobs = new ArrayList<ActionProb>();
	GroundedAction ga = oneStepActionSelection(incoming, params);
	for (GroundedAction a : actions) {
	    if (a.equals(ga)) {
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
    public boolean applicableInState(State incoming, String[] params) {
	if (!abstractionGenerated) {
	    generateAbstraction(incoming);
	}

	List<StateHashTuple> states = getAbstractedStates(incoming);

	if (visited.contains(states)) {
	    visited.clear();
	    return false;
	}

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
	    // Weighted dice roll for selection
	    ga = gas.get(rand.nextInt(gas.size()));

	    // Most common for selection
	    /*
	     * Map<GroundedAction, Integer> weights = new
	     * HashMap<GroundedAction, Integer>(); for (GroundedAction ga: gas)
	     * { weights.put(ga, weights.containsKey(ga) ? weights.get(ga) + 1 :
	     * 1); } GroundedAction ga = null; int max = 0; for
	     * (Entry<GroundedAction, Integer> e: weights.entrySet()) { if
	     * (e.getValue() > max) { ga = e.getKey(); max = e.getValue() } }
	     */
	}

	// After the selection is made, it is cached for the future
	if (ga != null) {
	    for (StateHashTuple state : definedFor) {
		List<GroundedAction> aList = new ArrayList<GroundedAction>();
		aList.add(ga);
		abstractedPolicy.put(state, aList);
	    }
	}

	return ga;
    }

    private List<StateHashTuple> getAbstractedStates(State incoming) {
	if (!ocombsGenerated) {
	    this.ocombs = AbstractedPolicy.generateAllCombinations(incoming,
		    gci);
	}

	List<StateHashTuple> states = new ArrayList<StateHashTuple>();

	for (List<String> ocomb : ocombs) {
	    states.add(hf.hashState(AbstractedPolicy.formState(incoming, ocomb)));
	}

	return states;
    }

    private void generateAbstraction(State incoming) {
	if (!abstractionGenerated) {
	    State withRespectTo = policy.keySet().iterator().next().s;
	    List<State> ss = new ArrayList<State>();
	    ss.add(incoming);
	    ss.add(withRespectTo);
	    this.gci = AbstractedPolicy.greatestCommonIntersection(ss);
	    List<List<String>> ocombs = AbstractedPolicy
		    .generateAllCombinations(withRespectTo, gci);

	    for (Entry<StateHashTuple, GroundedAction> e : policy.entrySet()) {
		if (!actions.contains(e.getValue())) {
		    actions.add(e.getValue());
		}
		for (List<String> ocomb : ocombs) {
		    State newS = AbstractedPolicy
			    .formState(e.getKey().s, ocomb);
		    List<GroundedAction> aList;
		    if (!abstractedPolicy.containsKey(hf.hashState(newS))) {
			aList = new ArrayList<GroundedAction>();
			aList.add(e.getValue());
			abstractedPolicy.put(hf.hashState(newS), aList);
		    } else {
			aList = abstractedPolicy.get(hf.hashState(newS));
			aList.add(e.getValue());
			abstractedPolicy.put(hf.hashState(newS), aList);
		    }
		}
	    }

	    abstractionGenerated = true;
	}
    }

    public void resetOption() {
	abstractionGenerated = false;
	ocombsGenerated = false;
	abstractedPolicy = new HashMap<StateHashTuple, List<GroundedAction>>();
	ocombs = new ArrayList<List<String>>();
	visited.clear();
    }

    public int size() {
	return policy.size();
    }
}
