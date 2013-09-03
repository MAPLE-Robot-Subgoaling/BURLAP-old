package oomdptb.oomdp.common;

import oomdptb.oomdp.State;
import oomdptb.oomdp.StateAbstraction;

public class NullAbstraction implements StateAbstraction {

	@Override
	public State abstraction(State s) {
		return s.copy();
	}

}
