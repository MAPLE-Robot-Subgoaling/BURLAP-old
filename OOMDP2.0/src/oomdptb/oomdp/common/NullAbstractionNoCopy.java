package oomdptb.oomdp.common;

import oomdptb.oomdp.State;
import oomdptb.oomdp.StateAbstraction;

public class NullAbstractionNoCopy implements StateAbstraction{

	@Override
	public State abstraction(State s) {
		return s;
	}

	

}
