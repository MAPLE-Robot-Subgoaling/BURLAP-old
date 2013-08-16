package oomdptb.behavior.learning.actorcritic.critics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import oomdptb.behavior.learning.actorcritic.Critic;
import oomdptb.behavior.learning.actorcritic.CritiqueResult;
import oomdptb.behavior.options.Option;
import oomdptb.behavior.options.OptionEvaluatingRF;
import oomdptb.behavior.statehashing.StateHashFactory;
import oomdptb.behavior.statehashing.StateHashTuple;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class TDLambda implements Critic {

	protected RewardFunction						rf;
	protected TerminalFunction						tf;
	protected double								gamma;
	protected StateHashFactory						hashingFactory;
	protected double								learningRate;
	protected double								vinit;
	protected double								lambda;
	
	
	protected Map<StateHashTuple, VValue>			vIndex;
	protected LinkedList<StateEligibilityTrace>		traces;
	
	
	public TDLambda(RewardFunction rf, TerminalFunction tf, double gamma, StateHashFactory hashingFactory, double learningRate, double vinit, double lambda) {
		this.rf = rf;
		this.tf = tf;
		this.gamma = gamma;
		this.hashingFactory = hashingFactory;
		
		this.learningRate = learningRate;
		this.vinit = vinit;
		this.lambda = lambda;
		
		
		vIndex = new HashMap<StateHashTuple, VValue>();
	}

	@Override
	public void addNonDomainReferencedAction(Action a) {
		if(a instanceof Option){
			if(!(this.rf instanceof OptionEvaluatingRF)){
				this.rf = new OptionEvaluatingRF(this.rf);
			}
		}

	}
	
	
	@Override
	public void initializeEpisode(State s) {
		this.traces = new LinkedList<TDLambda.StateEligibilityTrace>();
	}

	@Override
	public void endEpisode() {
		this.traces.clear();
	}
	
	@Override
	public CritiqueResult critiqueAndUpdate(State s, GroundedAction ga, State sprime) {
		
		StateHashTuple sh = hashingFactory.hashState(s);
		StateHashTuple shprime = hashingFactory.hashState(sprime);
		
		double r = this.rf.reward(s, ga, sprime);
		double discount = gamma;
		if(ga.action instanceof Option){
			Option o = (Option)ga.action;
			discount = Math.pow(gamma, o.getLastNumSteps());
		}
		
		VValue vs = this.getV(sh);
		double nextV = 0.;
		if(!this.tf.isTerminal(sprime)){
			nextV = this.getV(shprime).v;
		}
		
		double delta = r + discount*nextV - vs.v;
		
		//update all traces
		boolean foundTrace = false;
		for(StateEligibilityTrace t : traces){
			
			if(t.sh.equals(sh)){
				foundTrace = true;
				t.eligibility = 1.;
			}
			
			t.v.v = t.v.v + this.learningRate * delta * t.eligibility;
			t.eligibility = t.eligibility * lambda * discount;
		}
		
		if(!foundTrace){
			//then add it
			vs.v = vs.v + this.learningRate * delta;
			StateEligibilityTrace t = new StateEligibilityTrace(sh, discount*this.lambda, vs);
			
			traces.add(t);
		}
		
		
		CritiqueResult critique = new CritiqueResult(s, ga, sprime, delta);
		
		return critique;
	}

	
	protected VValue getV(StateHashTuple sh){
		VValue v = this.vIndex.get(sh);
		if(v == null){
			v = new VValue(vinit);
			this.vIndex.put(sh, v);
		}
		return v;
	}
	
	
	class VValue{
		
		public double v;
		
		public VValue(double v){
			this.v = v;
		}

	}
	
	
	public static class StateEligibilityTrace{
		
		public double			eligibility;
		public StateHashTuple	sh;
		public VValue			v;

		public StateEligibilityTrace(StateHashTuple sh, double eligibility, VValue v){
			this.sh = sh;
			this.eligibility = eligibility;
			this.v = v;
		}
		
		
		
	}


	

}
