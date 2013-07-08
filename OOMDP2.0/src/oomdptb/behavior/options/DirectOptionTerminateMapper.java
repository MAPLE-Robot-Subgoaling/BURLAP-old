package oomdptb.behavior.options;

import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public interface DirectOptionTerminateMapper {

	public State generateOptionTerminalState(State s);
	public int getNumSteps(State s, State sp);
	public double getCumulativeReward(State s, State sp, RewardFunction rf, double discount);
	
}
