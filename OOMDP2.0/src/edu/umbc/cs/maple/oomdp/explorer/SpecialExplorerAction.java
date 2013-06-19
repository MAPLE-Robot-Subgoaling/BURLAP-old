package edu.umbc.cs.maple.oomdp.explorer;

import edu.umbc.cs.maple.oomdp.State;

public interface SpecialExplorerAction {
	public State applySpecialAction(State curState);
}
