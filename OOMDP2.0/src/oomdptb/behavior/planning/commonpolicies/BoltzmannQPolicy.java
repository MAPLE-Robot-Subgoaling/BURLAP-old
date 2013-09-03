package oomdptb.behavior.planning.commonpolicies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

import oomdptb.behavior.Policy;
import oomdptb.behavior.QValue;
import oomdptb.behavior.planning.OOMDPPlanner;
import oomdptb.behavior.planning.PlannerDerivedPolicy;
import oomdptb.behavior.planning.QComputablePlanner;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class BoltzmannQPolicy extends Policy implements PlannerDerivedPolicy{

	protected QComputablePlanner		qplanner;
	double								temperature;
	Random 								rand;
	
	
	
	public BoltzmannQPolicy(double temperature){
		this.qplanner = null;
		this.temperature = temperature;
		this.rand = new Random();
	}
	
	public BoltzmannQPolicy(QComputablePlanner vf, double temperature){
		this.qplanner = vf;
		this.temperature = temperature;
		this.rand = new Random();
	}
	
	@Override
	public GroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		List<QValue> qValues = this.qplanner.getQs(s);
		return this.getActionDistributionForQValues(qValues);
	}

	
	
	private List<ActionProb> getActionDistributionForQValues(List <QValue> qValues){
		
		List <ActionProb> res = new ArrayList<Policy.ActionProb>();
		
		double [] normed = this.getTempNormalizedQs(qValues);
		double max = this.max(normed);
		double [] tnormed = this.getTranslatedQs(normed, max);
		double sumexp = 0.;
		for(int i = 0; i < tnormed.length; i++){
			sumexp += Math.exp(tnormed[i]);
		}
		double loggedSumExp = Math.log(sumexp);
		double shift = max + loggedSumExp;
		
		for(int i = 0; i < qValues.size(); i++){
			double p = Math.exp(normed[i] - shift);
			if(Double.isNaN(p)){
				throw new RuntimeErrorException(new Error("Probability in Boltzmann policy distribution is NaN"));
			}
			QValue q = qValues.get(i);
			ActionProb ap = new ActionProb(q.a, p);
			res.add(ap);
		}
		
		return res;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}

	@Override
	public void setPlanner(OOMDPPlanner planner) {
		if(!(planner instanceof QComputablePlanner)){
			throw new RuntimeErrorException(new Error("Planner is not a QComputablePlanner"));
		}
		
		this.qplanner = (QComputablePlanner)planner;
		
	}
	
	
	
	protected double [] getTempNormalizedQs(List <QValue> qValues){
		double [] normed = new double[qValues.size()];
		for(int i = 0 ;i < qValues.size(); i++){
			normed[i] = qValues.get(i).q / this.temperature;
		}
		return normed;
	}
	
	protected double [] getTranslatedQs(double [] qs, double c){
		double [] translated = new double[qs.length];
		for(int i = 0; i < qs.length; i++){
			translated[i] = qs[i] - c;
		}
		
		return translated;
	}
	
	protected double max(double [] darray){
		double max = Double.NEGATIVE_INFINITY;
		for(double d : darray){
			max = Math.max(max, d);
		}
		return max;
	}

}
