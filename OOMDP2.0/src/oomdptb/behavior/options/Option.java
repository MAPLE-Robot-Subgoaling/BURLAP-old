package oomdptb.behavior.options;

import java.util.Random;

import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.behavior.planning.StateMapping;
import oomdptb.oomdp.*;
import oomdptb.oomdp.common.*;



/*
 * Note: will the reward tracker work for 3-level hierarchical options with discounting?
 * I think not they will only work if the one step hierarchical action selection
 * only returns primitives, in which case I will have to implement
 * that for any hierarchical subclasses 
 * 
 */


public abstract class Option extends Action {

	protected Random 									rand;
	
	protected EpisodeAnalysis							lastOptionExecutionResults;
	protected boolean									shouldRecordResults;
	protected boolean									shouldAnnotateExecution;
	
	//variables for keeping track of reward from execution
	protected RewardFunction 							rf;
	protected boolean 									keepTrackOfReward;
	protected double 									discountFactor;
	protected double 									lastCumulativeReward;
	protected double									cumulativeDiscount;
	protected int										lastNumSteps;
	protected TerminalFunction							externalTerminalFunction;
	
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
		externalTerminalFunction = new NullTermination();
		shouldRecordResults = true;
		shouldAnnotateExecution = true;
	}
	
	
	public void toggleShouldRecordResults(boolean toggle){
		this.shouldRecordResults = toggle;
	}
	
	public void toggleShouldAnnotateResults(boolean toggle){
		this.shouldAnnotateExecution = toggle;
	}
	
	
	public boolean isRecordingExecutionResults(){
		return shouldRecordResults;
	}
	
	public boolean isAnnotatingExecutionResults(){
		return shouldAnnotateExecution;
	}
	
	public EpisodeAnalysis getLastExecutionResults(){
		return lastOptionExecutionResults;
	}
	
	public void setStateMapping(StateMapping m){
		this.stateMapping = m;
	}
	
	public void setTerminateMapper(DirectOptionTerminateMapper tm){
		this.terminateMapper = tm;
	}
	
	public void setExernalTermination(TerminalFunction tf){
		if(tf == null){
			this.externalTerminalFunction = new NullTermination();
		}
		else{
			this.externalTerminalFunction = tf;
		}
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
		
		this.name = name;
		this.domain = domain;
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = replacedClassNames;
		
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
		lastOptionExecutionResults = new EpisodeAnalysis(s);
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
		}while(this.continueFromState(curState, params) && !externalTerminalFunction.isTerminal(curState));
		
		
		
		return curState;
	}
	
	
	public State oneStep(State s, String [] params){
		GroundedAction ga = this.oneStepActionSelection(s, params);
		State sprime = ga.executeIn(s);
		lastNumSteps++;
		double r = 0.;
		if(keepTrackOfReward){
			r = rf.reward(s, ga, sprime);
			lastCumulativeReward += cumulativeDiscount*r;
			cumulativeDiscount *= discountFactor;
		}
		
		if(shouldRecordResults){
			GroundedAction recordAction = ga;
			if(shouldAnnotateExecution){
				NullAction annotatedPrimitive = new NullAction(this.name + "(" + (lastNumSteps-1) + ")-" + ga.action.getName());
				recordAction = new GroundedAction(annotatedPrimitive, ga.params);
			}
			lastOptionExecutionResults.recordTransitionTo(sprime, recordAction, r);
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
