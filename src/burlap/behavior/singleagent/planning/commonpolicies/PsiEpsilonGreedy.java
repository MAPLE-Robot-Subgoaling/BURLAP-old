package burlap.behavior.singleagent.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * This class is an extension of PolicyBlocksPolicy that uses an additional psi
 * value when determining which action to choose.
 * 
 */
public class PsiEpsilonGreedy extends EpsilonGreedy {
    protected double psi;
    protected boolean psiOn = true;

    public PsiEpsilonGreedy(double epsilon, double psi) {
	super(epsilon);
	this.psi = psi;
    }

    public PsiEpsilonGreedy(QLearning planner, double epsilon, double psi) {
	super(planner, epsilon);
	this.psi = psi;
    }

    public double getPsi() {
	return psi;
    }

    public void setPsi(double psi) {
	this.psi = psi;
    }

    /**
     * For getting the learned state-action mapping offline. If Q-values are
     * equal for two actions, it picks randomly
     * 
     * @param s
     *            - the state
     * @return the action corresponding to the state
     */
    public AbstractGroundedAction getCorrectAction(State s) {
	if (qplanner == null) {
	    throw new RuntimeException("QPlanner not set.");
	}
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

	return maxActions.get(rand.nextInt(maxActions.size())).a;
    }

    /**
     * Gets an action according to EpsilonGreedy, ignoring psi
     * 
     * @param s
     * @return
     */
    private AbstractGroundedAction getEpsilonAction(State s) {
	List<QValue> qValues = qplanner.getQs(s);
	List<QValue> maxActions = new ArrayList<QValue>();

	if (rand.nextDouble() <= epsilon) {
	    return qValues.get(rand.nextInt(qValues.size())).a;
	}

	maxActions.add(qValues.get(0));
	for (int i = 1; i < qValues.size(); i++) {
	    if (qValues.get(i).q == maxActions.get(0).q) {
		maxActions.add(qValues.get(i));
	    } else if (qValues.get(i).q > maxActions.get(0).q) {
		maxActions.clear();
		maxActions.add(qValues.get(i));
	    }
	}

	return maxActions.get(rand.nextInt(maxActions.size())).a;
    }

    /**
     * First, roll psi (% of taking an option) Second, roll epsilon (% of taking
     * a random action) Third, pick the best action
     */
    public AbstractGroundedAction getAction(State s) {
	if (!psiOn) {
	    return getEpsilonAction(s);
	}

	List<QValue> qValues = this.qplanner.getQs(s);
	List<QValue> optionValues = new ArrayList<QValue>();
	List<QValue> primitiveValues = new ArrayList<QValue>();
	List<QValue> maxActions = new ArrayList<QValue>();

	for (int i = 0; i < qValues.size(); i++) {
	    if (((GroundedAction) qValues.get(i).a).action.isPrimitive()) {
		primitiveValues.add(qValues.get(i));
	    } else {
		optionValues.add(qValues.get(i));
	    }
	}

	// roll psi
	if (optionValues.size() > 0 && rand.nextDouble() <= psi) {
	    maxActions.add(optionValues.get(0));
	    for (int i = 1; i < optionValues.size(); i++) {
		if (optionValues.get(i).q == maxActions.get(0).q) {
		    maxActions.add(optionValues.get(i));
		} else if (optionValues.get(i).q > maxActions.get(0).q) {
		    maxActions.clear();
		    maxActions.add(optionValues.get(i));
		}
	    }
	} else {
	    // roll epsilon
	    if (rand.nextDouble() <= epsilon) {
		return primitiveValues
			.get(rand.nextInt(primitiveValues.size())).a;
	    } else {
		maxActions.add(primitiveValues.get(0));
		for (int i = 1; i < primitiveValues.size(); i++) {
		    if (primitiveValues.get(i).q == maxActions.get(0).q) {
			maxActions.add(primitiveValues.get(i));
		    } else if (primitiveValues.get(i).q > maxActions.get(0).q) {
			maxActions.clear();
			maxActions.add(primitiveValues.get(i));
		    }
		}
	    }
	}

	return maxActions.get(rand.nextInt(maxActions.size())).a;
    }

    public boolean isPsiOn() {
	return psiOn;
    }

    public void psiOff() {
	psiOn = false;
    }

    public void psiOn() {
	psiOn = true;
    }
}
