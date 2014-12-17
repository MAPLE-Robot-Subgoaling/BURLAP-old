package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.PlannerDerivedPolicy;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

/**
 * A greedy policy that breaks ties by randomly choosing an action amongst the
 * tied actions. This class requires a QComputablePlanner
 * 
 * @author James MacGlashan
 * 
 */
public class GreedyQPolicy extends Policy implements PlannerDerivedPolicy {

    protected QComputablePlanner qplanner;
    protected Random rand;

    public GreedyQPolicy() {
	qplanner = null;
	rand = RandomFactory.getMapped(0);
    }

    /**
     * Initializes with a QComputablePlanner
     * 
     * @param planner
     *            the QComputablePlanner to use
     */
    public GreedyQPolicy(QComputablePlanner planner) {
	qplanner = planner;
	rand = RandomFactory.getMapped(0);
    }

    @Override
    public void setPlanner(OOMDPPlanner planner) {

	if (!(planner instanceof QComputablePlanner)) {
	    throw new RuntimeErrorException(new Error(
		    "Planner is not a QComputablePlanner"));
	}

	this.qplanner = (QComputablePlanner) planner;
    }

    @Override
    public AbstractGroundedAction getAction(State s) {
	List<QValue> qValues = this.qplanner.getQs(s);
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
	return maxActions.get(rand.nextInt(maxActions.size())).a;
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State s) {
	List<QValue> qValues = this.qplanner.getQs(s);
	int numMax = 1;
	double maxQ = qValues.get(0).q;
	for (int i = 1; i < qValues.size(); i++) {
	    QValue q = qValues.get(i);
	    if (q.q == maxQ) {
		numMax++;
	    } else if (q.q > maxQ) {
		numMax = 1;
		maxQ = q.q;
	    }
	}

	List<ActionProb> res = new ArrayList<Policy.ActionProb>();
	double uniformMax = 1. / (double) numMax;
	for (int i = 0; i < qValues.size(); i++) {
	    QValue q = qValues.get(i);
	    double p = 0.;
	    if (q.q == maxQ) {
		p = uniformMax;
	    }
	    ActionProb ap = new ActionProb(q.a, p);
	    res.add(ap);
	}

	return res;
    }

    @Override
    public boolean isStochastic() {
	return true; // although the policy is greedy, it randomly selects
		     // between tied actions
    }

    @Override
    public boolean isDefinedFor(State s) {
	return true; // can always find q-values with default value
    }

}
