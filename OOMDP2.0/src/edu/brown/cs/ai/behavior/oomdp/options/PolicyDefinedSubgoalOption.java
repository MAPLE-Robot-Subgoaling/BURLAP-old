package edu.brown.cs.ai.behavior.oomdp.options;

import edu.brown.cs.ai.behavior.oomdp.planning.StateConditionTest;
import edu.umbc.cs.maple.behavior.oomdp.Policy;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.State;


/*
 * This Subgoal option's initiation states are defined by the set
 * of states for which its policy is defined
 */


public class PolicyDefinedSubgoalOption extends Option {

	Policy						policy;
	StateConditionTest			subgoalTest;
	
	public PolicyDefinedSubgoalOption(String name, Policy p, StateConditionTest sg){
		this.policy = p;
		this.subgoalTest = sg;
		this.name_ = name;
		this.parameterClasses_ = new String[0];
		this.replacedClassNames_ = new String[0];
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
		if(subgoalTest.satisfies(this.map(s))){
			return 1.;
		}
		return 0.;
	}

	@Override
	public boolean applicableInState(State st, String [] params){
		return policy.getAction(this.map(st)) != null;
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
