package edu.umbc.cs.maple.oomdp;

public interface StateParser {

	public String stateToString(State s);
	public State stringToState(String str);
	
	
}
