package edu.umbc.cs.maple.oomdp.robotarm;

import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectClass;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;

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
	public static final String 				A1ATTNAME = "ang1ATT";
	public static final String				A2ATTNAME = "ang2ATT";

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

	//The angles can only be incremented by this amount
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


	//Propositional functions
	//PFTOUCHGOAL = when the hand(the fingers attached part) touches the goal
	//			Doesn't count when the arm touches the goal
	//PFTOUCHOBSTACLE = when the agnts touch the obstacle
	//			Obstacle Avoidance
	public static final String				PFTOUCHGOAL = "touchedGoal";
	public static final String				PFTOUCHOBSTACLE = "touchedObstacle";

	//Initialized an arm domain
	public static Domain					ROBOTARMDOMAIN = null;

	/**This functions generates the attributes for all the objects in the domain
	 *Adds the objects, actions, and proposition functions to the domain
	 * @return ROBOTARMDOMAIN: the arm agent just generated
	 */
	public Domain generateDomain(){

		if(ROBOTARMDOMAIN != null){
			return ROBOTARMDOMAIN;
		}

		//Initialize the domain
		ROBOTARMDOMAIN = new Domain();

		//Initilaized the three points as attributes and set the limits on the attributes
		Attribute xatt = new Attribute(ROBOTARMDOMAIN, XATTNAME, Attribute.AttributeType.REAL);
		xatt.setLims(XMIN, XMAX);
		Attribute yatt = new Attribute(ROBOTARMDOMAIN, YATTNAME, Attribute.AttributeType.REAL);
		yatt.setLims(YMIN, YMAX);

		Attribute x1att = new Attribute(ROBOTARMDOMAIN, X1ATTNAME, Attribute.AttributeType.REAL);
		x1att.setLims(XMIN, XMAX);
		Attribute y1att = new Attribute(ROBOTARMDOMAIN, Y1ATTNAME, Attribute.AttributeType.REAL);
		y1att.setLims(YMIN, YMAX);

		Attribute x2att = new Attribute(ROBOTARMDOMAIN, X2ATTNAME, Attribute.AttributeType.REAL);
		x2att.setLims(XMIN, XMAX);
		Attribute y2att = new Attribute(ROBOTARMDOMAIN, Y2ATTNAME, Attribute.AttributeType.REAL);
		y2att.setLims(YMIN, YMAX);

		//Two angles for the hand
		Attribute a1att = new Attribute(ROBOTARMDOMAIN, A1ATTNAME, Attribute.AttributeType.REAL);
		a1att.setLims(-ANGLEMAX, ANGLEMAX);

		Attribute a2att = new Attribute(ROBOTARMDOMAIN, A2ATTNAME, Attribute.AttributeType.REAL);
		a2att.setLims(-ANGLEMAX, ANGLEMAX);

		//The boundaries of the obstacles and goal
		Attribute latt = new Attribute(ROBOTARMDOMAIN, LATTNAME, Attribute.AttributeType.REAL);
		latt.setLims(XMIN, XMAX);

		Attribute ratt = new Attribute(ROBOTARMDOMAIN, RATTNAME, Attribute.AttributeType.REAL);
		ratt.setLims(XMIN, XMAX);

		Attribute batt = new Attribute(ROBOTARMDOMAIN, BATTNAME, Attribute.AttributeType.REAL);
		batt.setLims(YMIN, YMAX);

		Attribute tatt = new Attribute(ROBOTARMDOMAIN, TATTNAME, Attribute.AttributeType.REAL);
		tatt.setLims(YMIN, YMAX);

		//create classes
		ObjectClass agentclass = new ObjectClass(ROBOTARMDOMAIN, AGENTCLASS);
		agentclass.addAttribute(xatt);
		agentclass.addAttribute(yatt);
		agentclass.addAttribute(x1att);
		agentclass.addAttribute(y1att);
		agentclass.addAttribute(x2att);
		agentclass.addAttribute(y2att);
		agentclass.addAttribute(a1att);
		agentclass.addAttribute(a2att);

		ObjectClass obstclss = new ObjectClass(ROBOTARMDOMAIN, OBSTACLECLASS);
		obstclss.addAttribute(latt);
		obstclss.addAttribute(ratt);
		obstclss.addAttribute(batt);
		obstclss.addAttribute(tatt);


		ObjectClass padclass = new ObjectClass(ROBOTARMDOMAIN, GOALCLASS);
		padclass.addAttribute(latt);
		padclass.addAttribute(ratt);
		padclass.addAttribute(batt);
		padclass.addAttribute(tatt);
		
		//add actions to the domain
		Action moveUp = new ActionMoveUp(ACTIONMOVEUP, ROBOTARMDOMAIN, "");
		Action moveDown = new ActionMoveDown(ACTIONMOVEDOWN, ROBOTARMDOMAIN, "");
		Action turnArmLeft = new ActionTurnArmLeft(ACTIONTURNARMLEFT, ROBOTARMDOMAIN, "");
		Action turnArmRight = new ActionTurnArmRight(ACTIONTURNARMRIGHT, ROBOTARMDOMAIN, "");
		Action turnHandLeft = new ActionTurnHandLeft(ACTIONTURNHANDLEFT, ROBOTARMDOMAIN, "");
		Action turnHandRight = new ActionTurnHandRight(ACTIONTURNHANDRIGHT, ROBOTARMDOMAIN, "");
		
		//add propositional functions to the domain
		PropositionalFunction touchGoal = new TouchGoalPF(PFTOUCHGOAL, ROBOTARMDOMAIN, new String[]{AGENTCLASS, GOALCLASS});
		PropositionalFunction touchObstacle = new TouchObstacleePF(PFTOUCHOBSTACLE, ROBOTARMDOMAIN, new String[]{AGENTCLASS, OBSTACLECLASS});
		
		return ROBOTARMDOMAIN;
	}

}
