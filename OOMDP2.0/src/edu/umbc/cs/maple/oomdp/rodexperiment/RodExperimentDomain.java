package edu.umbc.cs.maple.oomdp.rodexperiment;

import edu.umbc.cs.maple.domain.oomdp.DomainGenerator;
import edu.umbc.cs.maple.oomdp.Domain;

public class RodExperimentDomain implements DomainGenerator {
	
	public static final String				XATTNAME = "xAtt"; //x attribute
	public static final String				YATTNAME = "yAtt"; //y attribute
	
	public static final String				AATTNAME = "angATT"; //Angle of the rod
	
	public static final String				LATTNAME = "lAtt"; //left boundary 
	public static final String				RATTNAME = "rAtt"; //right boundary
	public static final String				BATTNAME = "bAtt"; //bottom boundary
	public static final String				TATTNAME = "tAtt"; //top boundary
	
	//all the objects in the domain
	public static final String				AGENTCLASS = "agent";
	public static final String				OBSTACLECLASS = "obstacle";
	public static final String				GOALCLASS = "goal";
	
	//All the actions available to the agent
	public static final String				ACTIONMOVEUP = "moveUp"; //moves one unit up
	public static final String				ACTIONMOVEDOWN = "moveDown"; //moves one unit down
	public static final String				ACTIONUPTHRUST = "upThrust"; //rotates 10 degrees
	public static final String				ACTIONDOWNTHRUST = "downThrust"; //rotates -10 degrees
	
	//Propositional Fuctions
	public static final String				PFTOUCHGOAL = "reachedGoal";
	public static final String				PFTOUCHSURFACE = "touchingSurface"; //touching an obstacle
	
	public static Domain					DOMAIN = null;	
	
	public Domain generateDomain(){
		
		return DOMAIN;
		
	}

}
