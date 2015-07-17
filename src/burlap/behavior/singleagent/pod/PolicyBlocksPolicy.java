package burlap.behavior.singleagent.pod;

import java.util.HashMap;
import java.util.Map;

import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.PsiEpsilonGreedy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * The policy used in POD and PolicyBlocks. Contains an internal reference to
 * its tabular policy.
 * 
 */
public class PolicyBlocksPolicy extends PsiEpsilonGreedy {
    protected Map<StateHashTuple, GroundedAction> policy;

    public PolicyBlocksPolicy(double epsilon) {
	this(null, epsilon, 0.0);
	psiOff();
    }

    public PolicyBlocksPolicy(Map<StateHashTuple, GroundedAction> p,
	    double epsilon) {
	this(epsilon);
	setPolicy(p);
    }

    public PolicyBlocksPolicy(QLearning qplanner, double epsilon) {
	this(qplanner, epsilon, 0.0);
	psiOff();
    }

    public PolicyBlocksPolicy(double epsilon, double psi) {
	this(null, epsilon, psi);
    }

    public PolicyBlocksPolicy(QLearning qplanner, double epsilon, double psi) {
	super(qplanner, epsilon, psi);
	policy = new HashMap<StateHashTuple, GroundedAction>();
    }

    /**
     * Adds a new entry to the policy
     * 
     * @param s
     * @param a
     */
    public void addEntry(State s, GroundedAction a) {
	policy.put(((OOMDPPlanner) qplanner).stateHash(s), a);
    }

    @Override
    public AbstractGroundedAction getAction(State s) {
	policy.put(((OOMDPPlanner) qplanner).stateHash(s),
		(GroundedAction) super.getCorrectAction(s));

	return super.getAction(s);
    }

    /**
     * Size of the policy
     * 
     * @return {@link #policy.size()}
     */
    public int size() {
	return policy.size();
    }

    /**
     * Gets the tabular policy (Q-value free)
     * 
     * @return {@link #policy}
     */
    public Map<StateHashTuple, GroundedAction> getPolicy() {
	return policy;
    }

    private void setPolicy(Map<StateHashTuple, GroundedAction> p) {
	policy.putAll(p);
    }

    /**
     * Gets the QLearning agent
     * 
     * @return {@link #super.qplanner}
     */
    public QLearning getLearner() {
	return (QLearning) qplanner;
    }
}
