package oomdptb.behavior.planning.deterministic;

import oomdptb.behavior.planning.OOMDPPlanner;
import oomdptb.behavior.planning.StateConditionTestIterable;
import oomdptb.oomdp.State;

public class MultiStatePrePlanner {

	
	public static void runPlannerForAllInitStates(OOMDPPlanner planner, StateConditionTestIterable initialStates){
		for(State s : initialStates){
			planner.planFromState(s);
		}
	}
	
}
