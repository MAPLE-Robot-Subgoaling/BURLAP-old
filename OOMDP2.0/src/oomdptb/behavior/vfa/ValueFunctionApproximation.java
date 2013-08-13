package oomdptb.behavior.vfa;

import java.util.List;
import java.util.Map;

import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public interface ValueFunctionApproximation {

	public ApproximationResult getStateValue(State s);
	public List<ActionApproximationResult> getStateActionValues(State s, List <GroundedAction> gas);
	
	public WeightGradient getWeightGradient(ApproximationResult approximationResult);
	
	
	
}
