package oomdptb.behavior;

import java.util.List;

import oomdptb.behavior.options.Option;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;
import oomdptb.oomdp.common.NullAction;


public abstract class Policy {

	protected boolean evaluateDecomposesOptions = true;
	protected boolean annotateOptionDecomposition = true;
	
	public abstract GroundedAction getAction(State s); //returns null when policy is undefined for s
	public abstract List<ActionProb> getActionDistributionForState(State s); //returns null when policy is undefined for s
	public abstract boolean isStochastic();
	
	
	public void evaluateMethodsShouldDecomposeOption(boolean toggle){
		this.evaluateDecomposesOptions = toggle;
	}
	
	public void evaluateMethodsShouldAnnotateOptionDecomposition(boolean toggle){
		this.annotateOptionDecomposition = toggle;
	}
	
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, TerminalFunction tf){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		while(!tf.isTerminal(cur)){
			cur = this.followAndRecordPolicy(res, cur, rf);
		}
		
		return res;
	}
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, TerminalFunction tf, int maxSteps){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s); //add initial state
		
		State cur = s;
		int nSteps = 0;
		while(!tf.isTerminal(cur) && nSteps < maxSteps){
			
			cur = this.followAndRecordPolicy(res, cur, rf);
			
			nSteps = res.numTimeSteps();
			
		}
		
		return res;
	}
	public EpisodeAnalysis evaluateBehavior(State s, RewardFunction rf, int numSteps){
		EpisodeAnalysis res = new EpisodeAnalysis();
		res.addState(s);
		
		State cur = s;
		int nSteps = 0;
		while(nSteps < numSteps){
			
			cur = this.followAndRecordPolicy(res, cur, rf);
			
			nSteps = res.numTimeSteps();
			
		}
		
		return res;
	}
	
	
	private State followAndRecordPolicy(EpisodeAnalysis ea, State cur, RewardFunction rf){
		
		State next = null;
		
		//follow policy
		GroundedAction ga = this.getAction(cur);
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions){
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(next, ga, r);
		}
		else{
			//then we need to decompose the option
			Option o = (Option)ga.action;
			o.initiateInState(cur, ga.params);
			int ns = 0;
			do{
				//do step of option
				GroundedAction cga = o.oneStepActionSelection(cur, ga.params);
				next = cga.executeIn(cur);
				double r = rf.reward(cur, cga, next);
				
				if(annotateOptionDecomposition){
					//setup a null action to record the option and primitive action taken
					NullAction annotatedPrimitive = new NullAction(o.getName() + "(" + ns + ")-" + cga.action.getName());
					GroundedAction annotatedPrimitiveGA = new GroundedAction(annotatedPrimitive, cga.params);
					
					//record it
					ea.recordTransitionTo(next, annotatedPrimitiveGA, r);
				}
				else{
					//otherwise just record the primitive that was taken
					ea.recordTransitionTo(next, cga, r);
				}
				
				cur = next;
				ns++;
				
				
			}while(o.continueFromState(cur, ga.params));
			
		}
		
		//return outcome state
		return next;
	}
	
	
	
	public class ActionProb{
		public GroundedAction ga;
		public double pSelection;
		
		public ActionProb(GroundedAction ga, double p){
			this.ga = ga;
			this.pSelection = p;
		}
		
	}
	
}
