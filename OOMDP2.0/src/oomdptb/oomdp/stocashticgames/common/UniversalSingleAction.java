package oomdptb.oomdp.stocashticgames.common;

import oomdptb.oomdp.State;
import oomdptb.oomdp.stocashticgames.SGDomain;
import oomdptb.oomdp.stocashticgames.SingleAction;

public class UniversalSingleAction extends SingleAction {

	public UniversalSingleAction(SGDomain d, String name) {
		super(d, name);
	}
	
	public UniversalSingleAction(SGDomain d, String name, String [] types){
		super(d, name, types);
	}
	
	public UniversalSingleAction(SGDomain d, String name, String [] types, String [] renames){
		super(d, name, types, renames);
	}

	@Override
	public boolean isApplicableInState(State s, String actingAgent, String [] params) {
		return true;
	}

}
