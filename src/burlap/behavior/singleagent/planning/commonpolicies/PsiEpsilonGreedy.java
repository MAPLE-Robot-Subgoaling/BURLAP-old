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

    public AbstractGroundedAction getAction(State s) {
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
	if (psiOn && rand.nextDouble() <= psi && optionValues.size() > 0) {
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

    public void psiOff() {
	psiOn = false;
    }

    public void psiOn() {
	psiOn = true;
    }
}
