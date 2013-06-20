package edu.umbc.cs.maple.oomdp.data;

public class TrainingElement {
	public String 			command;
	public Trajectory		trajectory;
	
	public TrainingElement(String c, Trajectory t){
		command = c;
		trajectory = t;
	}
}
