package oomdptb.behavior.planning.deterministic.informed.astar;

import java.util.List;
import java.util.Map;

import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.behavior.planning.deterministic.informed.Heuristic;
import oomdptb.behavior.planning.deterministic.informed.PrioritizedSearchNode;
import oomdptb.behavior.planning.deterministic.informed.astar.AStar;
import oomdptb.behavior.planning.StateHashTuple;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;

public class StaticWeightedAStar extends AStar {

	protected double			epsilonP1;
	
	public StaticWeightedAStar(Domain domain, RewardFunction rf, StateConditionTest gc, Map<String, List<Attribute>> attributesForHashCode, Heuristic heuristic, double epsilon) {
		super(domain, rf, gc, attributesForHashCode, heuristic);
		this.epsilonP1 = 1. + epsilon;
	}
	
	@Override
	public double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, StateHashTuple successorState) {
		double cumR = 0.;
		double r = 0.;
		if(parentNode != null){
			double pCumR = cumulatedRewardMap.get(parentNode.s);
			r = rf.reward(parentNode.s.s, generatingAction, successorState.s);
			cumR = pCumR + r;
		}
		
		double H  = heuristic.h(successorState.s);
		lastComputedCumR = cumR;
		double F = cumR + (this.epsilonP1*H);
		
		return F;
	}

}
