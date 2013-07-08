package oomdptb.behavior.planning.deterministic;

import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class TFGoalCondition implements StateConditionTest {

	protected TerminalFunction tf;
	
	public TFGoalCondition(TerminalFunction tf){
		this.tf = tf;
	}
	
	@Override
	public boolean satisfies(State s) {
		return tf.isTerminal(s);
	}

}
