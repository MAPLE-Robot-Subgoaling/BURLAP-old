package oomdptb.behavior.planning.deterministic;

import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public class UniformPlusGoalRF implements RewardFunction {

	protected StateConditionTest gc;
	
	public UniformPlusGoalRF(StateConditionTest gc){
		this.gc = gc;
	}
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		if(!gc.satisfies(sprime)){
			return -1;
		}
		return 0;
	}

}
