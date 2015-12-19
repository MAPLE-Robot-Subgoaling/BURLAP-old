package burlap.behavior.singleagent.learnfromdemo.mlirl.support;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;

/**
 * A tuple (triple) for storing the Q-gradient associated with a state and
 * action. The gradient is stored in a double array.
 * 
 * @author James MacGlashan.
 */
public class QGradientTuple {

	/**
	 * The state
	 */
	public State s;

	/**
	 * The action
	 */
	public AbstractGroundedAction a;

	/**
	 * The gradient for the state and action.
	 */
	public double[] gradient;

	/**
	 * Initializes.
	 * 
	 * @param s
	 *            the state
	 * @param a
	 *            the action
	 * @param gradient
	 *            the gradient for the state an action
	 */
	public QGradientTuple(State s, AbstractGroundedAction a, double[] gradient) {
		this.s = s;
		this.a = a;
		this.gradient = gradient;
	}

}
