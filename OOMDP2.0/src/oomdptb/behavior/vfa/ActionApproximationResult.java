package oomdptb.behavior.vfa;

import java.util.List;

import oomdptb.oomdp.GroundedAction;

public class ActionApproximationResult {

	public GroundedAction			ga;
	public ApproximationResult		approximationResult;
	
	public ActionApproximationResult(GroundedAction ga, ApproximationResult approximationResult) {
		this.ga = ga;
		this.approximationResult = approximationResult;
	}
	
	
	public static ActionApproximationResult extractApproximationForAction(List<ActionApproximationResult> approximations, GroundedAction ga){
		for(ActionApproximationResult aar : approximations){
			if(aar.ga.equals(ga)){
				return aar;
			}
		}
		
		return null;
	}

}
