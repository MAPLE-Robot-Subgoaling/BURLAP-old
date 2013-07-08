package oomdptb.oomdp.common;

import java.util.List;

import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.GroundedProp;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public class SingleGoalPFRF implements RewardFunction {

	PropositionalFunction			pf;
	double							goalReward;
	double							nonGoalReward;
	
	
	
	public SingleGoalPFRF(PropositionalFunction pf){
		this.pf = pf;
		this.goalReward = 1.;
		this.nonGoalReward = 0.;
	}
	
	public SingleGoalPFRF(PropositionalFunction pf, double goalReward, double nonGoalReward){
		this.pf = pf;
		this.goalReward = goalReward;
		this.nonGoalReward = nonGoalReward;
	}
	
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		List<GroundedProp> gps = sprime.getAllGroundedPropsFor(pf);
		
		for(GroundedProp gp : gps){
			if(gp.isTrue(sprime)){
				return goalReward;
			}
		}
		
		return nonGoalReward;
	}

}
