package edu.umbc.cs.maple.oomdp;

public abstract class RewardFunction {
	
	protected Domain		domain_; //the domain on which this reward function operates
	
	public RewardFunction(){
		
	}
	
	public RewardFunction(Domain domain){
		domain_ = domain;
	}
	
	public void setDomain(Domain domain){
		domain_ = domain;
	}
	
	public Domain getDomain(){
		return domain_;
	}
	
	
	//note that params are the parameters for the action
	public abstract double reward(State s, GroundedAction a, State sprime);

}
