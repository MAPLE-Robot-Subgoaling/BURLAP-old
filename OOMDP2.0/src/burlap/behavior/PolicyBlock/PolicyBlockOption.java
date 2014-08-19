package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class PolicyBlockOption extends Option {
    public Map<StateHashTuple, GroundedAction> policy;
    private StateHashFactory hashFactory;
    private List<Action> actions;
    private Set<StateHashTuple> visited;

    public PolicyBlockOption(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> policy, List<Action> actions) {
	this.policy = policy;
	hashFactory = hf;
	this.actions = actions;
	super.name = "PolicyBlockOption";
	this.parameterClasses = new String[0];
	this.parameterOrderGroup = new String[0];
	visited = new HashSet<StateHashTuple>();
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
    public double probabilityOfTermination(State s, String[] params) {
	if (policy.get(hashFactory.hashState(s)) == null
		|| visited.contains(hashFactory.hashState(s))) {
	    visited.clear();
	    return 1.;
	}

	return 0.;
    }

    @Override
    public void initiateInStateHelper(State s, String[] params) {
    }

    @Override
    public GroundedAction oneStepActionSelection(State s, String[] params) {
	if (visited.contains(hashFactory.hashState(s))) {
	    visited.clear();
	    return null;
	}

	visited.add(hashFactory.hashState(s));
	return policy.get(hashFactory.hashState(s));
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State s,
	    String[] params) {
	GroundedAction ga = policy.get(hashFactory.hashState(s));
	List<ActionProb> aprobs = new ArrayList<ActionProb>();
	for (Action a : actions) {
	    if (ga.action.equals(a)) {
		ActionProb p = new ActionProb(
			new GroundedAction(a, a.getName()), 1.);
		aprobs.add(p);
	    } else {
		ActionProb p = new ActionProb(
			new GroundedAction(a, a.getName()), 0.);
		aprobs.add(p);
	    }
	}

	return aprobs;
    }

    @Override
    public boolean applicableInState(State s, String[] params) {
	if (visited.contains(hashFactory.hashState(s))) {
	    visited.clear();
	    return false;
	}

	return policy.get(hashFactory.hashState(s)) != null;
    }

    public int size() {
	return policy.size();
    }
}
