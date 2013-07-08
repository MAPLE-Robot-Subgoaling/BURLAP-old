package oomdptb.behavior.planning;

import java.util.List;
import java.util.Map;

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
	
	public HashedTransitionProbability(State s, double p, Map <String, List<Attribute>> attsForHash){
		this.sh = new StateHashTuple(s, attsForHash);
		this.p = p;
	}
	
	public HashedTransitionProbability(TransitionProbability tp, Map <String, List<Attribute>> attsForHash){
		this.sh = new StateHashTuple(tp.s, attsForHash);
		this.p = tp.p;
	}
	
}
