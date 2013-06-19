package edu.brown.cs.ai.behavior.oomdp.options;

import edu.umbc.cs.maple.oomdp.RewardFunction;
import edu.umbc.cs.maple.oomdp.State;

public interface DirectOptionTerminateMapper {

	public State generateOptionTerminalState(State s);
	public int getNumSteps(State s, State sp);
	public double getCumulativeReward(State s, State sp, RewardFunction rf, double discount);
	
}
