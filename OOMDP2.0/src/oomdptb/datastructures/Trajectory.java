package oomdptb.datastructures;

import java.util.*;

import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;


public class Trajectory {

	private List <State>				states_;
	private List <GroundedAction>		actions_;
	
	
	//assume this is well formed (states has one more element than actions)
	public Trajectory(List <State> states, List <GroundedAction> actions){
		states_ = states;
		actions_ = actions;
	}
	
	public int numStates(){
		return states_.size();
	}
	
	
	//returns the i'th state
	public State getState(int i){
		return states_.get(i);
	}
	
	
	//return the action performed in the i'th state
	public GroundedAction getAction(int i){
		if(i > actions_.size()){ //there are more states than actions since there is no action when the sequence stops
			return null;
		}
		return actions_.get(i);
	}
	
	
}
