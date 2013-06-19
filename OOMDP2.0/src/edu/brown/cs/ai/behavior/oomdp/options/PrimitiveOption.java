package edu.brown.cs.ai.behavior.oomdp.options;

import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.State;

/*
 * This class is just an option wrapper for a primitive action
 * This may be useful if a caller wants to universally support the option interface
 * and wants to include primitive options
 */


public class PrimitiveOption extends Option {

	protected Action srcAction;
	
	
	public PrimitiveOption(Action srcAction){
		this.srcAction = srcAction;
		this.init(srcAction.getName(), srcAction.getDomain(), srcAction.getParameterClasses(), srcAction.getReplacedClasses());
	}

	
	@Override
	public boolean isPrimitive(){
		return true;
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
		return true;
	}

	@Override
	public double probabilityOfTermination(State s, String[] params) {
		return 1.0;
	}

	@Override
	public void initiateInStateHelper(State s, String[] params) {
		//no bookkeeping necessary
	}
	
	@Override
	public boolean applicableInState(State st, String [] params){
		return this.srcAction.applicableInState(st, params);
	}

	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		return new GroundedAction(this.srcAction, params);
	}

}
