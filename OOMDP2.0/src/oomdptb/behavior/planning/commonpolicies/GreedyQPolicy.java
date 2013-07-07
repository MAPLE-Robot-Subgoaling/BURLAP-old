package oomdptb.behavior.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import oomdptb.behavior.Policy;
import oomdptb.behavior.QValue;
import oomdptb.behavior.planning.QComputablePlanner;
import oomdptb.debugtools.RandomFactory;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class GreedyQPolicy extends Policy {

	protected QComputablePlanner		qplanner;
	protected Random 					rand;
	
	public GreedyQPolicy(QComputablePlanner planner){
		qplanner = planner;
		rand = RandomFactory.getMapped(0);
	}
	
	public void setPlanner(QComputablePlanner qplanner){
		this.qplanner = qplanner;
	}
	
	@Override
	public GroundedAction getAction(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		List <QValue> maxActions = new ArrayList<QValue>();
		maxActions.add(qValues.get(0));
		double maxQ = qValues.get(0).q;
		for(int i = 1; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			if(q.q == maxQ){
				maxActions.add(q);
			}
			else if(q.q > maxQ){
				maxActions.clear();
				maxActions.add(q);
				maxQ = q.q;
			}
		}
		return maxActions.get(rand.nextInt(maxActions.size())).a;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		int numMax = 1;
		double maxQ = qValues.get(0).q;
		for(int i = 1; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			if(q.q == maxQ){
				numMax++;
			}
			else if(q.q > maxQ){
				numMax = 1;
				maxQ = q.q;
			}
		}
		
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		double uniformMax = 1./(double)numMax;
		for(int i = 0; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			double p = 0.;
			if(q.q == maxQ){
				p = uniformMax;
			}
			ActionProb ap = new ActionProb(q.a, p);
			res.add(ap);
		}
		
		
		return res;
	}

	@Override
	public boolean isStochastic() {
		return true; //although the policy is greedy, it randomly selects between tied actions
	}



}
