package oomdptb.behavior.statehashing;

import oomdptb.oomdp.State;

public class StateHashFactory {

	public StateHashTuple hashState(State s){
		return new StateHashTuple(s);
	}
	
}