package oomdptb.behavior.planning.deterministic;

import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import oomdptb.behavior.Policy;
import oomdptb.behavior.planning.OOMDPPlanner;
import oomdptb.behavior.planning.PlannerDerivedPolicy;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;


/**
 * This is a dynamic deterministic planner policy, which means
 * if the source deterministic planner has not already computed
 * and cached the plan for a query state, then this policy
 * will first compute a plan with the planner and then return the
 * answer
 * @author James MacGlashan
 */

public class DDPlannerPolicy extends Policy implements PlannerDerivedPolicy{

	protected DeterministicPlanner dp;
	
	
	public DDPlannerPolicy(){
		this.dp = null;
	}
	
	public DDPlannerPolicy(DeterministicPlanner dp){
		this.dp = dp;
	}
	
	
	@Override
	public void setPlanner(OOMDPPlanner planner) {
		
		if(!(planner instanceof DeterministicPlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a Deterministic Planner"));
		}
		
		this.dp = (DeterministicPlanner)planner;
		
	}
	
	
	@Override
	public GroundedAction getAction(State s) {
		return dp.querySelectedActionForState(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		GroundedAction selectedAction = this.getAction(s);
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
