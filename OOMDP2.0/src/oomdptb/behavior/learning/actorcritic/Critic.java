package oomdptb.behavior.learning.actorcritic;

import oomdptb.oomdp.Action;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public interface Critic {
	
	public void addNonDomainReferencedAction(Action a);
	
	public void initializeEpisode(State s);
	public void endEpisode();

	public CritiqueResult critiqueAndUpdate(State s, GroundedAction ga, State sprime);
	
}
