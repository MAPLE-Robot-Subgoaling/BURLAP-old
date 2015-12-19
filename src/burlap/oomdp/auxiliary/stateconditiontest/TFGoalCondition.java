package burlap.oomdp.auxiliary.stateconditiontest;

import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;

/**
 * A simple StateConditionTest wrapper of TerminalFunciton. Deterministic
 * forward search planners search for goal states that are indicated by
 * StateConditionTest objects. If a TerminalFunction only terminates in goal
 * states, this class can be used to wrap the terminal function to indicate that
 * goal states are any previously defined terminal states.
 * 
 * @author James MacGlashan
 * 
 */
public class TFGoalCondition implements StateConditionTest {

	protected TerminalFunction tf;

	/**
	 * Sets this class to return true on any states that are terminal states as
	 * indicated by the TerminalFunction.
	 * 
	 * @param tf
	 *            the TerminalFunction that indicates goal states.
	 */
	public TFGoalCondition(TerminalFunction tf) {
		this.tf = tf;
	}

	/**
	 * Returns the {@link burlap.oomdp.core.TerminalFunction} used to specify
	 * the goal condition.
	 * 
	 * @return the {@link burlap.oomdp.core.TerminalFunction} used to specify
	 *         the goal condition.
	 */
	public TerminalFunction getTf() {
		return tf;
	}

	@Override
	public boolean satisfies(State s) {
		return tf.isTerminal(s);
	}

	/**
	 * Sets the {@link burlap.oomdp.core.TerminalFunction} used to specify the
	 * goal condition.
	 * 
	 * @param tf
	 *            the {@link burlap.oomdp.core.TerminalFunction} used to specify
	 *            the goal condition.
	 */
	public void setTf(TerminalFunction tf) {
		this.tf = tf;
	}

}
