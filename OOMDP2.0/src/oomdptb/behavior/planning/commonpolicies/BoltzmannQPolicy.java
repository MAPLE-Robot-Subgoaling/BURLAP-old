package oomdptb.behavior.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import oomdptb.behavior.Policy;
import oomdptb.behavior.QValue;
import oomdptb.behavior.planning.QComputablePlanner;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class BoltzmannQPolicy extends Policy {

	protected QComputablePlanner		qplanner;
	double								temperature;
	Random 								rand;
	
	public BoltzmannQPolicy(QComputablePlanner vf, double temperature){
		this.qplanner = vf;
		this.temperature = temperature;
		this.rand = new Random();
	}
	
	@Override
	public GroundedAction getAction(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		List <ActionProb> dist = this.getActionDistributionForQValues(qValues);
		double tp = 0.;
		double roll = this.rand.nextDouble();
		for(ActionProb ap : dist){
			tp += ap.pSelection;
			if(roll < tp){
				return ap.ga;
			}
		}
		
		//something went wrong, should have added to one forcing a return so return null to break things
		return null;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		return this.getActionDistributionForQValues(qValues);
	}

	
	
	private List<ActionProb> getActionDistributionForQValues(List <QValue> qValues){
		
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		
		double [] aps = new double[qValues.size()];
		double sumAP = 0.;
		for(int i = 0; i < qValues.size(); i++){
			double v = Math.exp(qValues.get(i).q / this.temperature);
			if(v == 0.){
				System.out.println("Problem1");
			}
			aps[i] = v;
			sumAP += v;
		}
		
		for(int i = 0; i < qValues.size(); i++){
			QValue q = qValues.get(i);
			double p = aps[i]/sumAP;
			if(Double.isNaN(p)){
				System.out.println("Problem");
			}
			ActionProb ap = new ActionProb(q.a, p);
			res.add(ap);
		}
		
		return res;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

}
