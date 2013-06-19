package edu.brown.cs.ai.behavior.oomdp.options;

import edu.brown.cs.ai.behavior.oomdp.planning.StateConditionTest;
import edu.brown.cs.ai.behavior.oomdp.planning.StateConditionTestIterable;
import edu.umbc.cs.maple.behavior.oomdp.Policy;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.State;

public class SubgoalOption extends Option {

	Policy						policy;
	StateConditionTest			initiationTest;
	StateConditionTest			subgoalTest;
	
	
	public SubgoalOption(String name, Policy p, StateConditionTest init, StateConditionTest sg){
		this.name_ = name;
		this.policy = p;
		this.initiationTest = init;
		this.subgoalTest = sg;
		
		this.parameterClasses_ = new String[0];
		this.replacedClassNames_ = new String[0];
		
	}
	
	
	public StateConditionTest getInitiationTest(){
		return this.initiationTest;
	}
	
	public StateConditionTest getSubgoalTest(){
		return this.subgoalTest;
	}
	
	public boolean enumerable(){
		return (initiationTest instanceof StateConditionTestIterable) && (subgoalTest instanceof StateConditionTestIterable);
	}
	
	@Override
	public boolean isMarkov() {
		return true;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return !policy.isStochastic();
	}

	@Override
	public double probabilityOfTermination(State s, String[] params) {
		State ms = this.map(s);
		if(subgoalTest.satisfies(ms) || policy.getAction(ms) == null){
			return 1.;
		}
		return 0.;
	}

	
	
	@Override
	public boolean applicableInState(State st, String [] params){
		if(initiationTest.satisfies(this.map(st))){
			return true;
		}
		return false;
	}
	
	
	@Override
	public void initiateInStateHelper(State s, String[] params) {
		//no bookkeeping
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		return policy.getAction(this.map(s));
	}

	
	
	
}
