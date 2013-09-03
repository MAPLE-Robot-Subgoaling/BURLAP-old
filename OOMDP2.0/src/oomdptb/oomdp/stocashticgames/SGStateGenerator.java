package oomdptb.oomdp.stocashticgames;

import java.util.List;

import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.State;

public abstract class SGStateGenerator {

	public abstract State generateState(List <Agent> agents);
	
	protected ObjectInstance getAgentObjectInstance(Agent a){
		return new ObjectInstance(a.agentType.oclass, a.worldAgentName);
	}

}
