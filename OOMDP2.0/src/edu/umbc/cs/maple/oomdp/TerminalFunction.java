/* Author: James MacGlashan
 * Description:
 * Abstract class for determining if a state in an OO-MDP domain is a terminal state
 * This kind of information is important for episode and goal-oriented MDPs
 */


package edu.umbc.cs.maple.oomdp;

public interface TerminalFunction {
	
	public boolean isTerminal(State s);	
	
}
