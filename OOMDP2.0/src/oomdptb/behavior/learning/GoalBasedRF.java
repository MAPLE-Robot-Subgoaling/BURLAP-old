package oomdptb.behavior.learning;

import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public class GoalBasedRF implements RewardFunction {

	protected Domain domain;
	protected StateConditionTest gc;
	protected double goalReward;
	
	
	public GoalBasedRF(StateConditionTest gc) {
		this.gc = gc;
		this.goalReward = 1.;
	}

	public GoalBasedRF(Domain domain, StateConditionTest gc) {
		this.domain = domain;
		this.gc = gc;
		this.goalReward = 1.;
	}
	
	public GoalBasedRF(Domain domain, StateConditionTest gc, double goalReward) {
		this.domain = domain;
		this.gc = gc;
		this.goalReward = goalReward;
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		if(gc.satisfies(sprime)){
			return goalReward;
		}
		
		return 0.;
	}

}
