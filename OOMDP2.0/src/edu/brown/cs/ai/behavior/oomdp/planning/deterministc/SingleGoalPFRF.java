package edu.brown.cs.ai.behavior.oomdp.planning.deterministc;

import java.util.List;

import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.GroundedProp;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.RewardFunction;
import edu.umbc.cs.maple.oomdp.State;

public class SingleGoalPFRF extends RewardFunction {

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
