package oomdptb.oomdp;

public interface RewardFunction {
	
	//note that params are the parameters for the action
	/**
	 * 
	 * @param s the original state
	 * @param a the grounded action being performed
	 * @param sprime the state after the action has being performed
	 * @return the reward for the action
	 */
	public abstract double reward(State s, GroundedAction a, State sprime);

}
