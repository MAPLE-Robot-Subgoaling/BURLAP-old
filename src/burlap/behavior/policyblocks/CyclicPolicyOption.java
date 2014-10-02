package burlap.behavior.policyblocks;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * This class is used in P-MODAL for applying options without grounding
 * This differs from PolicyDefinedSubgoalOption in that it has stochastic termination
 * and has bookkeeping to prevent the option for having a looping trajectory
 */
public class CyclicPolicyOption extends Option {
    private Policy policy;
    private Set<State> visited;
    private double termProb;

    public CyclicPolicyOption(Policy policy, double termProb, String name) {
	if (termProb > 1 || termProb < 0) {
	    throw new RuntimeException("Invalid termination probability");
	}

	super.name = "CPO-" + name;
	this.policy = policy;
	// Tree set to prevent states from hashing (hashing states doesn't work without StateHashTuple)
	this.visited = new TreeSet<State>();
	this.termProb = termProb;
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
	if (!policy.isDefinedFor(incoming) || visited.contains(incoming)) {
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
	return (GroundedAction) policy.getAction(incoming);
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State incoming,
	    String[] params) {
	return policy.getActionDistributionForState(incoming);
    }

    @Override
    public boolean applicableInState(State incoming, String[] params) {
	if (visited.contains(incoming)) {
	    visited.clear();
	    return false;
	}

	return policy.isDefinedFor(incoming);
    }
}
