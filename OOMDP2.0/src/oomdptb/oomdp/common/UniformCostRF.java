package oomdptb.oomdp.common;

import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public class UniformCostRF implements RewardFunction {

	
	public UniformCostRF(){
		
	}
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		return -1;
	}

}
