package oomdptb.oomdp.stocashticgames.tournament;

import oomdptb.oomdp.stocashticgames.AgentType;

public class MatchEntry {

	public AgentType agentType;
	public int agentId;
	
	public MatchEntry(AgentType at, int ai){
		this.agentType = at;
		this.agentId = ai;
	}

}
