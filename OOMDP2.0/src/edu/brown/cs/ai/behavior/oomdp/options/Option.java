package edu.brown.cs.ai.behavior.oomdp.options;

import java.util.Random;

import edu.brown.cs.ai.behavior.oomdp.planning.StateMapping;
import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.RewardFunction;
import edu.umbc.cs.maple.oomdp.State;

/*
 * Note: will the reward tracker work for hierarchical options with discounting?
 * I think not they will only work if the one step hierarchical action selection
 * only returns primitives, in which case I will have to implement
 * that for any hierarchical subclasses 
 * 
 */


public abstract class Option extends Action {

	protected Random 									rand;
	
	//variables for keeping track of reward from execution
	protected RewardFunction 							rf;
	protected boolean 									keepTrackOfReward;
	protected double 									discountFactor;
	protected double 									lastCumulativeReward;
	protected double									cumulativeDiscount;
	protected int										lastNumSteps;
	
	protected StateMapping								stateMapping;
	
	protected DirectOptionTerminateMapper				terminateMapper;
	
	
	public abstract boolean isMarkov();
	public abstract boolean usesDeterministicTermination();
	public abstract boolean usesDeterministicPolicy();
	public abstract double probabilityOfTermination(State s, String [] params);
	public abstract void initiateInStateHelper(State s, String [] params); //important if the option is not Markovian; called automatically by the perform action helper
	public abstract GroundedAction oneStepActionSelection(State s, String [] params);
	
	public Option(){
		this.init();
	}
	
	public Option(String name, Domain domain, String parameterClasses) {
		super(name, domain, parameterClasses);
		this.init();
	}

	
	public Option(String name, Domain domain, String [] parameterClasses){
		super(name, domain, parameterClasses);
		this.init();
	}
	
	public Option(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		super(name, domain, parameterClasses, replacedClassNames);
		this.init();
	}
	
	private void init(){
		rand = new Random();
		rf = null;
		keepTrackOfReward = false;
		discountFactor = 1.;
		lastCumulativeReward = 0.;
		cumulativeDiscount = 1.;
		lastNumSteps = 0;
		stateMapping = null;
		terminateMapper = null;
	}
	
	public void setStateMapping(StateMapping m){
		this.stateMapping = m;
	}
	
	public void setTerminateMapper(DirectOptionTerminateMapper tm){
		this.terminateMapper = tm;
	}
	
	protected State map(State s){
		if(stateMapping == null){
			return s;
		}
		return stateMapping.mapState(s);
	}
	
	public void keepTrackOfRewardWith(RewardFunction rf, double discount){
		this.keepTrackOfReward = true;
		this.rf = rf;
		this.discountFactor = discount;
	}
	
	//options should not be connected from the domain, because the domain describes base level behavior
	//this method overrides and removes the statement connecting the domain to it
	public void init(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		
		name_ = name;
		domain_ = domain;
		parameterClasses_ = parameterClasses;
		replacedClassNames_ = replacedClassNames;
		
	}
	
	
	public double getLastCumulativeReward(){
		return this.lastCumulativeReward;
	}
	
	public int getLastNumSteps(){
		return this.lastNumSteps;
	}
	
	//should be overridden by special primitive option subclass which simply encapsulates an action and provides option interfaces
	@Override
	public boolean isPrimitive(){
		return false;
	}
	
	public void initiateInState(State s, String [] params){
		lastCumulativeReward = 0.;
		cumulativeDiscount = 1.;
		lastNumSteps = 0;
		this.initiateInStateHelper(s, params);
	}
	
	protected State performActionHelper(State st, String[] params){
		
		if(terminateMapper != null){
			State ns = terminateMapper.generateOptionTerminalState(st);
			lastNumSteps = terminateMapper.getNumSteps(st, ns);
			lastCumulativeReward = terminateMapper.getCumulativeReward(st, ns, rf, discountFactor);
			return ns;
		}
		
		State curState = st;
		
		this.initiateInState(curState, params);
		
		do{
			curState = this.oneStep(curState, params);
		}while(this.continueFromState(curState, params));
		
		
		
		return curState;
	}
	
	
	public State oneStep(State s, String [] params){
		GroundedAction ga = this.oneStepActionSelection(s, params);
		State sprime = ga.executeIn(s);
		lastNumSteps++;
		if(keepTrackOfReward){
			lastCumulativeReward += cumulativeDiscount*rf.reward(s, ga, sprime);
			cumulativeDiscount *= discountFactor;
		}
		return sprime;
	}
	
	
	
	public boolean continueFromState(State s, String [] params){
		double pt = this.probabilityOfTermination(s, params);
		
		//deterministic case needs no random roll
		if(pt == 1.){
			return false;
		}
		else if(pt == 0.){
			return true;
		}
		
		//otherwise need to do a random roll to determine if we terminated here or not
		double roll = rand.nextDouble();
		if(roll < pt){
			return false; //terminate
		}
		
		return true;
		
	}
	
	
	
	
	
	

	
	
	

}
