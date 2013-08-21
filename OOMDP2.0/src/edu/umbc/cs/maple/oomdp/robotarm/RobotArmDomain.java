package edu.umbc.cs.maple.oomdp.robotarm;

import java.util.List;

import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectClass;
import edu.umbc.cs.maple.oomdp.ObjectInstance;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.State;
import edu.umbc.cs.maple.oomdp.explorer.TerminalExplorer;

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
	//PFTOUCHOBSTACLE = when the agents touch the obstacle
	//			Obstacle Avoidance
	public static final String				PFTOUCHGOAL = "touchedGoal";
	public static final String				PFTOUCHOBSTACLE = "touchedObstacle";

	//Initialized an arm domain
	public static Domain					ROBOTARMDOMAIN = null;

	public static void main(String[] args){
		RobotArmDomain rad = new RobotArmDomain();
		rad.generateDomain();

		State clean= rad.getCleanState();

		rad.setAgent(clean, 0., 3., 0., 5., 8., 0, 10., 11.);
		rad.setObstacle(clean, 20., 30., 40., 60.);
		rad.setGoal(clean, 40., 42., 38., 34.);

		TerminalExplorer te = new TerminalExplorer(ROBOTARMDOMAIN);

		te.addActionShortHand("w", ACTIONMOVEUP);
		te.addActionShortHand("s", ACTIONMOVEDOWN);
		te.addActionShortHand("a", ACTIONTURNARMLEFT);
		te.addActionShortHand("d", ACTIONTURNARMRIGHT);
		te.addActionShortHand("e", ACTIONTURNHANDRIGHT);
		te.addActionShortHand("q", ACTIONTURNHANDLEFT);

		te.exploreFromState(clean);

	}

	public State getCleanState(){

		this .generateDomain();

		State s = new State();

		ObjectInstance agent = new ObjectInstance(ROBOTARMDOMAIN.getObjectClass(AGENTCLASS), AGENTCLASS + "0");
		s.addObject(agent);

		ObjectInstance goal = new ObjectInstance(ROBOTARMDOMAIN.getObjectClass(GOALCLASS), GOALCLASS + "0");
		s.addObject(goal);


		ObjectInstance obst = new ObjectInstance(ROBOTARMDOMAIN.getObjectClass(OBSTACLECLASS), OBSTACLECLASS + "0");
		s.addObject(obst);


		return s;

	}

	public void setAgent(State s, double x, double y, double a1, double x1, double y1, double a2, double x2, double y2){

		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);

		agent.setValue(A1ATTNAME, a1);
		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(X1ATTNAME, x1);
		agent.setValue(Y1ATTNAME, y1);
		agent.setValue(A2ATTNAME, a2);
		agent.setValue(X2ATTNAME, x1);
		agent.setValue(Y2ATTNAME, y1);
	}

	public void setObstacle(State s, double l, double r, double b, double t){
		ObjectInstance obst = s.getObjectsOfTrueClass(OBSTACLECLASS).get(0);

		obst.setValue(LATTNAME, l);
		obst.setValue(RATTNAME, r);
		obst.setValue(BATTNAME, b);
		obst.setValue(TATTNAME, t);
	}

	public void setGoal(State s, double l, double r, double b, double t){
		ObjectInstance goal = s.getObjectsOfTrueClass(GOALCLASS).get(0);

		goal.setValue(LATTNAME, l);
		goal.setValue(RATTNAME, r);
		goal.setValue(BATTNAME, b);
		goal.setValue(TATTNAME, t);
	}

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
		PropositionalFunction touchObstacle = new TouchObstaclePF(PFTOUCHOBSTACLE, ROBOTARMDOMAIN, new String[]{AGENTCLASS, OBSTACLECLASS});

		return ROBOTARMDOMAIN;
	}

	/**
	 * Updates the agents position based on the action chosen
	 * @param st: the current state of the agent
	 * @param moveUpDown: determines whether the whole hand should move up or down
	 * 			+1.0 - the whole hand moves up; -1.0 - moved down 1; 0 - do not move the whole hand up or down 1
	 * @param turnArmLeftRight: determines whether the arm should turn right or left
	 * @param turnHandLeftRight: determines whether the hand should turn right or left at the wrist
	 */
	public static void updateMotion(State st, double moveUpDown, double turnArmLeftRight, double turnHandLeftRight){

		ObjectInstance agent = st.getObjectsOfTrueClass(AGENTCLASS).get(0);

		//All the attributes of the agent
		double ang1 = agent.getRealValForAttribute(A1ATTNAME);
		double ang2 = agent.getRealValForAttribute(A2ATTNAME);
		double x = agent.getRealValForAttribute(XATTNAME);
		double y = agent.getRealValForAttribute(YATTNAME);
		double x1 = agent.getRealValForAttribute(X1ATTNAME);
		double y1 = agent.getRealValForAttribute(Y1ATTNAME);
		double x2 = agent.getRealValForAttribute(X2ATTNAME);
		double y2 = agent.getRealValForAttribute(Y2ATTNAME);

		//get temporary values, so if any of the values do not work we can revert back
		double tempAng1 = agent.getRealValForAttribute(A1ATTNAME);
		double tempAng2 = agent.getRealValForAttribute(A2ATTNAME);
		double tempX = agent.getRealValForAttribute(XATTNAME);
		double tempY = agent.getRealValForAttribute(YATTNAME);
		double tempX1 = agent.getRealValForAttribute(X1ATTNAME);
		double tempY1 = agent.getRealValForAttribute(Y1ATTNAME);
		double tempX2 = agent.getRealValForAttribute(X2ATTNAME);
		double tempY2 = agent.getRealValForAttribute(Y2ATTNAME);


		//First, if the actions calls the whole hand to be moved up or down
		if(moveUpDown == 1.0 || moveUpDown == -1.0){		
			x = x + moveUpDown;
			y = y + moveUpDown;
			x1 = x1 + moveUpDown;
			y1 = y1 + moveUpDown;
			x2 = x2 + moveUpDown;
			y2 = y2 + moveUpDown;
		}

		//If the arm is intended to turn left or right
		if(turnArmLeftRight == 1.0 || turnArmLeftRight == -1.0){

			//First change the angle of the arm
			ang1 = ang1 + (turnArmLeftRight * ANGLEINC);
			//Then rotate the (x1,y1) point around the (x,y)
			//Then change the (x2, y2) points accordingly
			double cosTheta = Math.cos(ANGLEINC * turnArmLeftRight);
			double sinTheta = Math.cos(ANGLEINC * turnArmLeftRight);
			x1 = ((cosTheta * (x1 - x)) - (sinTheta * (y1 - y)) + x);
			y1 = ((cosTheta * (y1 - y)) + (sinTheta * (x1 - x)) + y);

			//Find the difference (x1, y1) was changed
			//Then use the difference to change the (x2, y2) positions
			double changeX = x1 - tempX1;
			double changeY = y1 - tempY1;
			x2 = changeX + x2;
			y2 = changeY + y2;

		}

		//If the hand is intended to tuwn left or right
		if(turnHandLeftRight == 1.0 || turnHandLeftRight == -1.0){

			//first change the angle of the wrist
			ang2 = ang2 + (turnHandLeftRight * ANGLEINC);

			//Then rotate the point (x2, y2) around (x1, y1)
			double cosTheta = Math.cos(ANGLEINC * turnHandLeftRight);
			double sinTheta = Math.cos(ANGLEINC * turnHandLeftRight);
			x1 = ((cosTheta * (x2 - x1)) - (sinTheta * (y2 - y1)) + x1);
			y1 = ((cosTheta * (y2 - y1)) + (sinTheta * (x2 - x1)) + y1);

		}

		//Now check for out of bounds
		if(x < XMIN || x > XMAX || x1 < XMIN || x1 > XMAX || x2 < XMIN || x2 > XMAX || 
				y < YMIN || y > YMAX ||y1 < YMIN || y1 > YMAX || y2 < YMIN || y2 > YMAX ||
				ang1 > ANGLEMAX || ang1 < -ANGLEMAX ||
				ang2 > (ang1 + ANGLEMAX) ||  ang2 < (-ang1 - ANGLEMAX)){

			ang1 = tempAng1;
			ang2 = tempAng2;
			x = tempX;
			y = tempY;
			x1 = tempX1;
			y1 = tempY1;
			x2 = tempX2;
			y2 = tempY2;

		}

		//Also check for object collisions
		List <ObjectInstance> obstacles = st.getObjectsOfTrueClass(OBSTACLECLASS);
		for(ObjectInstance o : obstacles){
			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);

			if((x >= l && x <= r && y >= b && y <= t) || 
					(x1 >= l && x1 <= r && y1 >= b && y1 <= t) ||
					(x2 >= l && x2 <= r && y2 >= b && y2 <= t)){

				ang1 = tempAng1;
				ang2 = tempAng2;
				x = tempX;
				y = tempY;
				x1 = tempX1;
				y1 = tempY1;
				x2 = tempX2;
				y2 = tempY2;

				//can only hit one obsbtacle so break out of search
				break;
			}
		}

		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(X1ATTNAME, x1);
		agent.setValue(Y1ATTNAME, y1);
		agent.setValue(A1ATTNAME, ang1);
		agent.setValue(X2ATTNAME, x2);
		agent.setValue(Y2ATTNAME, y2);
		agent.setValue(A2ATTNAME, ang2);

	}

	/**Defines the ActionMoveUp for the current domain
	 * 		Creates the Action by calling the parent class action
	 * 		the performActionHelper calls the updateMotion to actually carry out the intended action
	 * 		this function moves the whole hand one unit up  
	 */

	public class ActionMoveUp extends Action{

		public ActionMoveUp(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionMoveUp(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RobotArmDomain.updateMotion(st, 1.0, 0., 0.);
			return st;
		}
	}

	/**
	 * This action moves the whole hand down 1
	 */
	public class ActionMoveDown extends Action{

		public ActionMoveDown(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionMoveDown(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RobotArmDomain.updateMotion(st, -1.0, 0., 0.);
			return st;
		}
	}

	/**
	 * This action rotates the arm pi/18 R to the right
	 */
	public class ActionTurnArmRight extends Action{

		public ActionTurnArmRight(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionTurnArmRight(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RobotArmDomain.updateMotion(st, 0.0, -1., 0.);
			return st;
		}
	}

	/**
	 * This action rotates the arm pi/18 R to the left
	 */
	public class ActionTurnArmLeft extends Action{

		public ActionTurnArmLeft(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionTurnArmLeft(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RobotArmDomain.updateMotion(st, 0.0, 1., 0.);
			return st;
		}
	}

	/**
	 * This action rotates the hand pi/18 R to the right
	 */
	public class ActionTurnHandRight extends Action{

		public ActionTurnHandRight(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionTurnHandRight(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RobotArmDomain.updateMotion(st, 0.0, 0.0, -1.0);
			return st;
		}
	}

	/**
	 * This action rotates the hand pi/18 R to the left
	 */
	public class ActionTurnHandLeft extends Action{

		public ActionTurnHandLeft(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionTurnHandLeft(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RobotArmDomain.updateMotion(st, 0.0, 0.0, -1.0);
			return st;
		}
	}

	/**Propositional function to check whether the hand has touched the goal
	 * If the arm touches the goal, the function returns false
	 */
	public class TouchGoalPF extends PropositionalFunction{

		public TouchGoalPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}

		public TouchGoalPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance goal = st.getObject(params[1]);

			double l = goal.getRealValForAttribute(LATTNAME);
			double r = goal.getRealValForAttribute(RATTNAME);
			double b = goal.getRealValForAttribute(BATTNAME);
			double t = goal.getRealValForAttribute(TATTNAME);

			double x1 = agent.getRealValForAttribute(X1ATTNAME);
			double y1 = agent.getRealValForAttribute(Y1ATTNAME);
			double x2 = agent.getRealValForAttribute(X2ATTNAME);
			double y2 = agent.getRealValForAttribute(Y2ATTNAME);

			//If the (x1,y1) or (x2,y2); which basically represent the dimensions of the had are within the
			//dinemsions of the box, the propositional function will return true
			if((x1 >=l && x1 <= r && y1 <=t && y1>=b) || (x2 >=l && x2 <= r && y2 <=t && y2 >= b)){
				return true;
			}

			return false;
		}
	}

	/**Used for obstacle avoidance
	 * If any of the dimensions of the agents hand are within the dimesions of the obstacles return true
	 */
	public class TouchObstaclePF extends PropositionalFunction{

		public TouchObstaclePF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}

		public TouchObstaclePF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {


			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance o = st.getObject(params[1]);
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);
			double x1 = agent.getRealValForAttribute(XATTNAME);
			double y1 = agent.getRealValForAttribute(YATTNAME);
			double x2 = agent.getRealValForAttribute(XATTNAME);
			double y2 = agent.getRealValForAttribute(YATTNAME);

			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);

			if((x >= l && x <= r && y >= b && y <= t) || (x1 >= l && x1 <= r && y1 >= b && y1 <= t) || (x2 >= l && x2 <= r && y2 >= b && y2 <= t)){
				return true;
			}

			return false;
		}



	}


}
