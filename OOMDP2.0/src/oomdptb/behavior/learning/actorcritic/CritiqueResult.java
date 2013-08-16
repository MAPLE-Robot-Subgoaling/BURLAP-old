package oomdptb.behavior.learning.actorcritic;

import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class CritiqueResult {

	protected State					s;
	protected GroundedAction		a;
	protected State					sprime;
	protected double				critique;
	
	public CritiqueResult(State s, GroundedAction a, State sprime, double critique) {
		this.s = s;
		this.a = a;
		this.sprime = sprime;
		this.critique = critique;
	}

	public State getS() {
		return s;
	}

	public GroundedAction getA() {
		return a;
	}

	public State getSprime() {
		return sprime;
	}

	public double getCritique() {
		return critique;
	}

	
	
}
