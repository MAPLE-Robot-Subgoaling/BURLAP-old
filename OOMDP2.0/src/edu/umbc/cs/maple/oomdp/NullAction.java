package edu.umbc.cs.maple.oomdp;

/*
 * This action is an action that does nothing. 
 * It may be useful for making references to actions that do not have domain associations
 * or if a domain needs a no-op action
 * 
 */


public class NullAction extends Action {

	
	public NullAction(String name){
		this.name_ = name;
		this.parameterClasses_ = new String[0];
		this.replacedClassNames_ = new String[0];
		this.domain_ = null;
	}
	
	public NullAction(String name, Domain domain, String parameterClasses){
		super(name, domain, parameterClasses);
	}
	
	public NullAction(String name, Domain domain, String [] parameterClasses){
		super(name, domain, parameterClasses);
	}
	
	public NullAction(String name, Domain domain, String [] parameterClasses, String [] replacedClassName){
		super(name, domain, parameterClasses, replacedClassName);
	}
	
	@Override
	protected State performActionHelper(State st, String[] params) {
		return st;
	}

}
