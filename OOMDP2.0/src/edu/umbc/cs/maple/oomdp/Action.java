package edu.umbc.cs.maple.oomdp;

import java.util.ArrayList;
import java.util.List;



public abstract class Action {

	protected String					name_;					//name of the action
	protected Domain					domain_;				//domain that hosts the action
	protected String []					parameterClasses_;		//list of class names for each parameter of the action
	protected String []					replacedClassNames_;	//list of class names to which each parameter object should be paramaterized
	
	
	public Action(){
		//should not be called directly, but may be useful for subclasses of Action
	}
	
	
	//parameterClasses is expected to be comma delimited with no unnecessary spaces
	public Action(String name, Domain domain, String parameterClasses){
		
		String [] pClassArray;
		if(parameterClasses.equals("")){
			pClassArray = new String[0];
		}
		else{
			pClassArray = parameterClasses.split(",");
		}
		
		String [] rcn = new String[pClassArray.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		
		this.init(name, domain, pClassArray, rcn);
		
	}
	
	public Action(String name, Domain domain, String [] parameterClasses){
		
		String [] rcn = new String[parameterClasses.length];
		for(int i = 0; i < rcn.length; i++){
			rcn[i] = name + ".P" + i;
		}
		this.init(name, domain, parameterClasses, rcn);
		
	}
	
	public Action(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		this.init(name, domain, parameterClasses, replacedClassNames);
	}
	
	
	public void init(String name, Domain domain, String [] parameterClasses, String [] replacedClassNames){
		
		name_ = name;
		domain_ = domain;
		domain_.addAction(this);
		parameterClasses_ = parameterClasses;
		replacedClassNames_ = replacedClassNames;
		
	}
	
	public final String getName(){
		return name_;
	}
	
	
	public final String[] getParameterClasses(){
		return parameterClasses_;
	}
	
	public final String[] getReplacedClasses(){
		return replacedClassNames_;
	}
	
	public final Domain getDomain(){
		return domain_;
	}
	
	
	public final boolean applicableInState(State st, String params){
		return applicableInState(st, params.split(","));
	}
	
	
	public boolean applicableInState(State st, String [] params){
		//default behavior is that an action can be applied in any state
		//but this might need be overridden if that is not the case
		return true; 
	}
	
	
	//parameterClasses is expected to be comma delimited with no unnecessary spaces
	public final State performAction(State st, String params){
		
		return performAction(st, params.split(","));
		
	}
	
	public final State performAction(State st, String [] params){
		
		State resultState = st.copy();
		if(!this.applicableInState(st, params)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		
		return performActionHelper(resultState, params);
		
	}
	
	
	//naturally, this should be overridden if it's not a primitive.
	//primitive here means that execution is longer than one time step
	//and a result of executing other actions
	public boolean isPrimitive(){
		return true;
	}
	
	
	public List<TransitionProbability> getTransitions(State st, String params){
		return this.getTransitions(st, params.split(","));
	}
	
	///this method should only be defined for finite MDPs
	//the default behavior assumes that the MDP is deterministic and will need to be
	//overridden for stochastic MDPs for each action
	public List<TransitionProbability> getTransitions(State st, String [] params){
		
		List <TransitionProbability> transition = new ArrayList<TransitionProbability>();
		State res = this.performAction(st, params);
		transition.add(new TransitionProbability(res, 1.0));
		
		return transition;
	}
	
	//parameterClasses is expected to be comma delimited with no unnecessary spaces
	//should return modified State st
	protected abstract State performActionHelper(State st, String [] params);
	
	
	
	
	public boolean equals(Object obj){
		Action op = (Action)obj;
		if(op.name_.equals(name_))
			return true;
		return false;
	}
	
	public int hashCode(){
		return name_.hashCode();
	}
	
	
}
