package oomdptb.behavior.planning.deterministic;

import java.util.ArrayList;
import java.util.List;

import oomdptb.behavior.Policy;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

/*
 * This is a static deterministic planner policy, which means
 * if the source deterministic planner has not already computed
 * and cached the plan for a query state, then this policy
 * is undefined for that state and will not try to compute it
 */


public class SDPlannerPolicy extends Policy {

	protected DeterministicPlanner dp;
	
	public SDPlannerPolicy(DeterministicPlanner dp){
		this.dp = dp;
	}
	
	
	@Override
	public GroundedAction getAction(State s) {
		if(this.dp.cachedPlanForState(s)){
			return this.dp.querySelectedActionForState(s);
		}
		return null; //then the policy is undefined
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		GroundedAction selectedAction = this.getAction(s);
		if(selectedAction == null){
			return null; //policy is undefined for this state
		}
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		ActionProb ap = new ActionProb(selectedAction, 1.);
		res.add(ap);
		return res;
	}


	@Override
	public boolean isStochastic() {
		return false;
	}

}
