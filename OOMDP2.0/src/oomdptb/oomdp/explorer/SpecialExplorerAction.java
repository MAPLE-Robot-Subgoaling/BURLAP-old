package oomdptb.oomdp.explorer;

import oomdptb.oomdp.State;

public interface SpecialExplorerAction {
	public State applySpecialAction(State curState);
}
