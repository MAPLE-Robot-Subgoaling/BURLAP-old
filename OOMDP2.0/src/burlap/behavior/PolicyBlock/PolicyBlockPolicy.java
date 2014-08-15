package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * For PolicyBlocks:
 * 
 * @author Tenji Tembo
 * 
 *         This class replicates a policy defined by the intersection of a
 *         StateHashTuple and it's corresponding grounded action. Note that the
 *         reward function and termination function are used to write the policy
 *         to an episode analysis object for visualization. This is a
 *         Deterministic Policy Object, no probability associated with this
 *         item.
 * 
 */
public class PolicyBlockPolicy extends EpsilonGreedy {
    public Map<StateHashTuple, GroundedAction> policy;
    public Map<StateHashTuple, List<QValue>> qpolicy;

    public PolicyBlockPolicy(double epsilon) {
	this(null, epsilon);
    }

    public PolicyBlockPolicy(QLearning qplanner, double epsilon) {
	super(qplanner, epsilon);
	policy = new HashMap<StateHashTuple, GroundedAction>();
	qpolicy = new HashMap<StateHashTuple, List<QValue>>();
    }

    public void addEntry(State s, GroundedAction a) {
	policy.put(((OOMDPPlanner) qplanner).stateHash(s), a);
    }

    /**
     * For getting the learned state-action mapping offline If Q-values are
     * equal for two actions, it picks the first action
     * 
     * @param s
     *            - the state
     * @return the action corresponding to the state
     */
    public AbstractGroundedAction getCorrectAction(State s) {
	List<QValue> qValues = super.qplanner.getQs(s);
	List<QValue> maxActions = new ArrayList<QValue>();
	maxActions.add(qValues.get(0));

	double maxQ = qValues.get(0).q;
	for (int i = 1; i < qValues.size(); i++) {
	    QValue q = qValues.get(i);
	    if (q.q == maxQ) {
		maxActions.add(q);
	    } else if (q.q > maxQ) {
		maxActions.clear();
		maxActions.add(q);
		maxQ = q.q;
	    }
	}

	return maxActions.get(0).a;
    }

    @Override
    public AbstractGroundedAction getAction(State s) {
	List<QValue> qValues = super.qplanner.getQs(s);
	qpolicy.put(((OOMDPPlanner) qplanner).stateHash(s), qValues);
	AbstractGroundedAction corr = getCorrectAction(s);
	policy.put(((OOMDPPlanner) qplanner).stateHash(s),
		(GroundedAction) corr);

	double roll = rand.nextDouble();
	if (roll <= epsilon) {
	    return qValues.get(rand.nextInt(qValues.size())).a;
	}

	return corr;
    }
}
