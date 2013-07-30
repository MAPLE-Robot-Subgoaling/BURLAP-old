package oomdptb.behavior.options;

import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class LocalSubgoalTF implements TerminalFunction {

	
	protected StateConditionTest		applicableStateTest;
	protected StateConditionTest		subgoalStateTest;
	
	
	public LocalSubgoalTF(StateConditionTest subgoalStateTest) {
		this.applicableStateTest = null;
		this.subgoalStateTest = subgoalStateTest;
	}
	
	public LocalSubgoalTF(StateConditionTest applicableStateTest, StateConditionTest subgoalStateTest) {
		this.applicableStateTest = applicableStateTest;
		this.subgoalStateTest = subgoalStateTest;
	}

	@Override
	public boolean isTerminal(State s) {
		
		if(this.applicableStateTest != null){
			if(!this.applicableStateTest.satisfies(s)){
				return true; //terminate when reaching a state that is not an initiation state
			}
		}
		
		return this.subgoalStateTest.satisfies(s);

	}

}
