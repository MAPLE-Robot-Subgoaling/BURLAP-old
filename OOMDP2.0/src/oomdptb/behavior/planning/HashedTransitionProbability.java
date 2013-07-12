package oomdptb.behavior.planning;

import java.util.List;
import java.util.Map;

import oomdptb.behavior.planning.statehashing.StateHashFactory;
import oomdptb.behavior.planning.statehashing.StateHashTuple;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TransitionProbability;

public class HashedTransitionProbability {

	public StateHashTuple sh;
	public double p;
	
	public HashedTransitionProbability(StateHashTuple sh, double p){
		this.sh = sh;
		this.p = p;
	}
	
	public HashedTransitionProbability(State s, double p, StateHashFactory hashingFactory){
		this.sh = hashingFactory.hashState(s);
		this.p = p;
	}
	
	public HashedTransitionProbability(TransitionProbability tp, StateHashFactory hashingFactory){
		this.sh = hashingFactory.hashState(tp.s);
		this.p = tp.p;
	}
	
}
