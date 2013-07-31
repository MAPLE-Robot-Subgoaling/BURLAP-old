package edu.umbc.cs.maple.oomdp.robotarm;

/**
 * Class details:
 * 	This class models a simple arm with two joints (the elbow and the wrist)
 *  The agent can basically move the arm or the hand in order to touch the goal object
 *  The whole hand can move up one unit up or down in the continuous domain or 10 degrees left or right
 *  The arm can also move 10 degrees left or right relative to the arm
 *  Class invariants: The angle of the whole hand has to be between -90 degrees <= angle <= 90 degrees
 *  					The x and y attributes cannot got out of bounds
 * @author Bhuvana Bellala
 *
 */
public class RobotArmDomain {

	//The agent is defined by three points
	//(x,y) defines the elbow
	//(x1,y1) defines the wrist
	//(x2,y2) defines the end of the hand
	public static final String				XATTNAME = "xAtt"; 
	public static final String				YATTNAME = "yAtt"; 
	public static final String				X1ATTNAME = "x1Att";
	public static final String				Y1ATTNAME = "y1Att";
	public static final String				X2ATTNAME = "x2Att";
	public static final String				Y2ATTNAME = "y2Att";

	//The agent is also defined by two angles
	//AAT1NAME = angle of the whole hand relative to the origin
	//AAT2NAME = angle of the wrist relative to the arm
	public static final String 				AAT1NAME = "ang1ATT";
	public static final String				AAT2NAME = "ang2ATT";

	//The obstacles and the goal are basically thought of as a rectangle
	//The "rectangles" are defined by the left, right, bottom, and top boundaries
	public static final String				LATTNAME = "lAtt"; //left boundary 
	public static final String				RATTNAME = "rAtt"; //right boundary
	public static final String				BATTNAME = "bAtt"; //bottom boundary
	public static final String				TATTNAME = "tAtt"; //top boundary

	//all the objects in the domain
	public static final String				AGENTCLASS = "agent";
	public static final String				OBSTACLECLASS = "obstacle";
	public static final String				GOALCLASS = "goal";

	//The boundaries of the whole domain; the objects in the domain must be contained within
	public static final double				XMIN = 0.;
	public static final double				XMAX = 100.;
	public static final double				YMIN = 0.;
	public static final double				YMAX = 50.;
	public static final double				ANGLEMAX = Math.PI/2.;

	//The angles can onle be incremented by this amount
	public static final double 				ANGLEINC = Math.PI/18;

	//All the actions available to the agent
	//ACTIONMOVEUP and ACTIONMOVEDOWN move the whole hand one unit up or down preserving the angles
	//ACTIONTURNARMLEFT/RIGHT move the arm pi/18R(10 degrees left or right) but preserve the angle of the wrist
	//ACTIONTURNHANDLEFT/RIGHT move the wrist pi/18R(10 degrees left or right) relative to the arm
	public static final String				ACTIONMOVEUP = "moveUp"; 
	public static final String				ACTIONMOVEDOWN = "moveDown"; 
	public static final String				ACTIONTURNARMLEFT = "armLeft"; 
	public static final String				ACTIONTURNARMRIGHT = "armRight";
	public static final String				ACTIONTURNHANDLEFT = "armLeft"; 
	public static final String				ACTIONTURNHANDRIGHT = "armRight"; 
	
	


}
