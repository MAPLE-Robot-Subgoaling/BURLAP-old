package oomdptb.behavior.learning.tdmethods;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oomdptb.behavior.options.Option;
import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.behavior.Policy;
import oomdptb.behavior.planning.StateHashTuple;
import oomdptb.behavior.QValue;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class SarsaLam extends QLearning {


	protected double				lambda;
	
	
	public SarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Map<String, List<Attribute>> attributesForHashCode, 
			double qInit, double learningRate, int maxEpisodeSize, double lamda) {
		
		super(domain, rf, tf, gamma, attributesForHashCode, qInit, learningRate, maxEpisodeSize);
		this.sarsalamInit(lamda);
		
	}
	
	public SarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Map<String, List<Attribute>> attributesForHashCode, 
			double qInit, double learningRate, Policy learningPolicy, int maxEpisodeSize, double lamda) {
		
		super(domain, rf, tf, gamma, attributesForHashCode, qInit, learningRate, learningPolicy, maxEpisodeSize);
		this.sarsalamInit(lamda);
		
	}
	
	
	protected void sarsalamInit(double lamda){
		this.lambda = lamda;
	}
	
		
	
	@Override
	public EpisodeAnalysis runLearningEpisodeFrom(State initialState) {
		
		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		maxQChangeInLastEpisode = 0.;
		
		StateHashTuple curState = this.stateHash(initialState);
		eStepCounter = 0;
		LinkedList<EligibilityTrace> traces = new LinkedList<SarsaLam.EligibilityTrace>();
		
		GroundedAction action = learningPolicy.getAction(curState.s);
		QValue curQ = this.getQ(curState, action);
		
		
		
		while(!tf.isTerminal(curState.s) && eStepCounter < maxEpisodeSize){
			
			StateHashTuple nextState = this.stateHash(action.executeIn(curState.s));
			GroundedAction nextAction = learningPolicy.getAction(nextState.s);
			QValue nextQ = this.getQ(nextState, nextAction);
			double nextQV = nextQ.q;
			
			if(tf.isTerminal(nextState.s)){
				nextQV = 0.;
			}
			
			
			//manage option specifics
			double r = 0.;
			double discount = this.gamma;
			if(action.action.isPrimitive()){
				r = rf.reward(curState.s, action, nextState.s);
				eStepCounter++;
			}
			else{
				Option o = (Option)action.action;
				r = o.getLastCumulativeReward();
				int n = o.getLastNumSteps();
				discount = Math.pow(this.gamma, n);
				eStepCounter += n;
			}
			
			ea.recordTransitionTo(nextState.s, action, r);
			
			//delta
			double delta = r + (discount * nextQV) - curQ.q;
			
			//update all
			boolean foundCurrentQTrace = false;
			for(EligibilityTrace et : traces){
				
				if(et.sh.equals(curState) && et.q.a.equals(action)){
					foundCurrentQTrace = true;
					et.eligibility = 1.; //replacing traces
				}
				
				et.q.q = et.q.q + (learningRate * et.eligibility * delta);
				et.eligibility = et.eligibility * lambda * discount;
				
				double deltaQ = Math.abs(et.initialQ - et.q.q);
				if(deltaQ > maxQChangeInLastEpisode){
					maxQChangeInLastEpisode = deltaQ;
				}
				
			}
			
			if(!foundCurrentQTrace){
				//then update and add it
				curQ.q = curQ.q + (learningRate * delta);
				EligibilityTrace et = new EligibilityTrace(curState, curQ, lambda*discount);
				
				traces.add(et);

				double deltaQ = Math.abs(et.initialQ - et.q.q);
				if(deltaQ > maxQChangeInLastEpisode){
					maxQChangeInLastEpisode = deltaQ;
				}
				
			}
			
			
			//move on
			curState = nextState;
			action = nextAction;
			curQ = nextQ;
			
			
		}
		
		
		
		if(episodeHistory.size() >= numEpisodesToStore){
			episodeHistory.poll();
			episodeHistory.offer(ea);
		}
		
		return ea;
	}
	
	
	
	
	
	
	public static class EligibilityTrace{
		
		public double					eligibility;
		public StateHashTuple			sh;
		public QValue					q;
		public double					initialQ;
		
		public EligibilityTrace(StateHashTuple sh, QValue q, double elgigbility){
			this.sh = sh;
			this.q = q;
			this.eligibility = elgigbility;
			this.initialQ = q.q;
		}
		
		
	}
	
	

}
