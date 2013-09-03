package oomdptb.oomdp.stocashticgames;

import java.util.Map;

import oomdptb.oomdp.State;

public interface JointReward {
	public Map<String, Double> reward(State s, JointAction ja, State sp);
}
