package oomdptb.oomdp;
/**
 * This is a class for representing grounded actions. Grounded actions are an individual action with a list of specific parameters passed into it.
 * @author James
 *
 */
public class GroundedAction {

	public Action action;
	public String [] params;
	/**
	 * 
	 * @param a the action that is being executed
	 * @param p the parameters that are passed into this action
	 */
	public GroundedAction(Action a, String [] p){
		this.init(a, p);
	}
		
	/**
	 * 
	 * @param a the action that is being executed
	 * @param p the parameters that are passed into this action (comma delineated)
	 */
	public GroundedAction(Action a, String p){
		
		String [] ps = null;
		if(p.equals("")){
			ps = new String[0];
		}
		else{
			ps = p.split(",");
		}
		this.init(a, ps);
	}
	
	/**
	 * 
	 * @param a the action that is being executed
	 * @param p the parameters that are passed into this action (comma delineated)
	 */
	private void init(Action a, String [] p){
		action = a;
		params = p;
	}
	
	/**
	 * Executes the grounded action on a given state
	 * @param s the state on which to execute the action
	 * @return The state after the action has been executed
	 */
	public State executeIn(State s){
		return action.performAction(s, params);
	}
	
	//toString returns the name of the action followed by each paramter
	@Override
	public String toString(){
		StringBuffer buf = new StringBuffer();
		buf.append(action.getName());
		for(int i = 0; i < params.length; i++){
			buf.append(" ").append(params[i]);
		}
		
		return buf.toString();
	}
	
	
	@Override
	public boolean equals(Object other){
		
		if(this == other){
			return true;
		}
		
		if(!(other instanceof GroundedAction)){
			return false;
		}
		
		GroundedAction go = (GroundedAction)other;
		if(this.action != go.action){
			return false;
		}
		
		String [] pog = this.action.getParameterOrderGroups();
		
		for(int i = 0; i < this.params.length; i++){
			String p = this.params[i];
			String orderGroup = pog[i];
			boolean foundMatch = false;
			for(int j = 0; j < go.params.length; j++){
				if(p.equals(go.params[j]) && orderGroup.equals(pog[j])){
					foundMatch = true;
					break;
				}
			}
			if(!foundMatch){
				return false;
			}		
		}
		
		return true;
	}
	
}
