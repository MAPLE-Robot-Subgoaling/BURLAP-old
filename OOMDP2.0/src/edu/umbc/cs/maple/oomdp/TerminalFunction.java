/* Author: James MacGlashan
 * Description:
 * Abstract class for determining if a state in an OO-MDP domain is a terminal state
 * This kind of information is important for episode and goal-oriented MDPs
 */


package edu.umbc.cs.maple.oomdp;

public abstract class TerminalFunction {

	protected Domain		domain_; //the domain on which this terminal function operates
	
	public TerminalFunction(){
		
	}
	
	public TerminalFunction(Domain domain){
		domain_ = domain;
	}
	
	public final void setDomain(Domain domain){
		domain_ = domain;
	}
	
	public final Domain getDomain(){
		return domain_;
	}
	
	public abstract boolean isTerminal(State s);
	
	
}
