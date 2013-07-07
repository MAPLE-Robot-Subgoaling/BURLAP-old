package oomdptb.oomdp;

public class GroundedAction {

	public Action action;
	public String [] params;
	
	public GroundedAction(Action a, String [] p){
		this.init(a, p);
	}
	
	
	//may also take parameters as a single string that is comma delineated
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
	
	
	private void init(Action a, String [] p){
		action = a;
		params = p;
	}
	
	
	public State executeIn(State s){
		return action.performAction(s, params);
	}
	
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
