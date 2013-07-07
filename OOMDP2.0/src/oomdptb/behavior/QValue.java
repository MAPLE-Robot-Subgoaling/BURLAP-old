package oomdptb.behavior;

import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class QValue {
	public State 				s;
	public GroundedAction		a;
	public double				q;
	
	public QValue(State s, GroundedAction a, double q){
		this.s = s;
		this.a = a;
		this.q = q;
	}
	
}
