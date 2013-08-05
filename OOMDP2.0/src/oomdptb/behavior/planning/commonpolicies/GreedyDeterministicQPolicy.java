package oomdptb.behavior.planning.commonpolicies;

import java.util.List;

import javax.management.RuntimeErrorException;

import oomdptb.behavior.Policy;
import oomdptb.behavior.QValue;
import oomdptb.behavior.planning.OOMDPPlanner;
import oomdptb.behavior.planning.PlannerDerivedPolicy;
import oomdptb.behavior.planning.QComputablePlanner;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class GreedyDeterministicQPolicy extends Policy implements PlannerDerivedPolicy{

	protected QComputablePlanner		qplanner;
	
	public GreedyDeterministicQPolicy() {
		qplanner = null;
	}
	
	public GreedyDeterministicQPolicy(QComputablePlanner qplanner){
		this.qplanner = qplanner;
	}
	
	public void setPlanner(OOMDPPlanner planner){
		
		if(!(planner instanceof QComputablePlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QComputablePlanner)planner;
	}
	

	@Override
	public GroundedAction getAction(State s) {
		
		List<QValue> qValues = this.qplanner.getQs(s);
		double maxQV = Double.NEGATIVE_INFINITY;
		QValue maxQ = null;
		for(QValue q : qValues){
			if(q.q > maxQV){
				maxQV = q.q;
				maxQ = q;
			}
		}
		
		return maxQ.a;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		return this.getDeterministicPolicy(s);
	}

	@Override
	public boolean isStochastic() {
		return false;
	}

}
