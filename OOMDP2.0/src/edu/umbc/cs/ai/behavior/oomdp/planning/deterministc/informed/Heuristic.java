package oomdptb.behavior.planning.deterministic.informed;

import oomdptb.oomdp.State;

public interface Heuristic {

	public double h(State s);
	
}
