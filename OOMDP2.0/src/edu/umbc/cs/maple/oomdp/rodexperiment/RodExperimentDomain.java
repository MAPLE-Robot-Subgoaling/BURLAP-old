package edu.umbc.cs.maple.oomdp.rodexperiment;

import edu.umbc.cs.maple.domain.oomdp.DomainGenerator;
import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectClass;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.State;
import edu.umbc.cs.maple.oomdp.ObjectInstance;
import edu.umbc.cs.maple.oomdp.explorer.TerminalExplorer;

public class RodExperimentDomain implements DomainGenerator {

	public static final String				XATTNAME = "xAtt"; //x attribute
	public static final String				YATTNAME = "yAtt"; //y attribute
	public static final String				X1ATTNAME = "x1Att";
	public static final String				Y1ATTNAME = "y1Att";

	public static final String				AATTNAME = "angATT"; //Angle of the rod

	public static final String				LATTNAME = "lAtt"; //left boundary 
	public static final String				RATTNAME = "rAtt"; //right boundary
	public static final String				BATTNAME = "bAtt"; //bottom boundary
	public static final String				TATTNAME = "tAtt"; //top boundary

	//all the objects in the domain
	public static final String				AGENTCLASS = "agent";
	public static final String				OBSTACLECLASS = "obstacle";
	public static final String				GOALCLASS = "goal";

	public static final double				XMIN = 0.;
	public static final double				XMAX = 100.;
	public static final double				YMIN = 0.;
	public static final double				YMAX = 50.;
	public static final double				ANGLEMAX = Math.PI/2.;
	public static final double 				ANGLEINC = Math.PI/18;


	//All the actions available to the agent
	public static final String				ACTIONMOVEUP = "moveUp"; //moves one unit up
	public static final String				ACTIONMOVEDOWN = "moveDown"; //moves one unit down
	public static final String				ACTIONTURNLEFT = "upThrust"; //rotates 10 degrees
	public static final String				ACTIONTURNRIGHT = "downThrust"; //rotates -10 degrees

	//Propositional Functions
	public static final String				PFTOUCHGOAL = "touchedGoal";
	public static final String				PFTOUCHSURFACE = "touchingSurface"; //touching an obstacle

	public static Domain					RODDOMAIN = null;	

	public static void main(String[] args){
		RodExperimentDomain red = new RodExperimentDomain();
		red.generateDomain();

		State clean= red.getCleanState();

		red.setAgent(clean, 0., 3., 0., 0., 0.);
		red.setObstacle(clean, 20., 30., 40., 60.);
		red.setGoal(clean, 40., 42., 38., 34.);
		
		TerminalExplorer te = new TerminalExplorer(RODDOMAIN);
		
		te.addActionShortHand("w", ACTIONMOVEUP);
		te.addActionShortHand("s", ACTIONMOVEDOWN);
		te.addActionShortHand("a", ACTIONTURNLEFT);
		te.addActionShortHand("d", ACTIONTURNRIGHT);
		
		te.exploreFromState(clean);
		
	}

	public void setAgent(State s, double a, double x, double y, double x1, double y1){

		ObjectInstance agent = s.getObjectsOfTrueClass(AGENTCLASS).get(0);

		agent.setValue(AATTNAME, a);
		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(X1ATTNAME, x1);
		agent.setValue(Y1ATTNAME, y1);
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
	

	public State getCleanState(){

		this .generateDomain();

		State s = new State();

		ObjectInstance agent = new ObjectInstance(RODDOMAIN.getObjectClass(AGENTCLASS), AGENTCLASS + "0");
		s.addObject(agent);

		ObjectInstance goal = new ObjectInstance(RODDOMAIN.getObjectClass(GOALCLASS), GOALCLASS + "0");
		s.addObject(goal);


		ObjectInstance obst = new ObjectInstance(RODDOMAIN.getObjectClass(OBSTACLECLASS), OBSTACLECLASS + "0");
		s.addObject(obst);


		return s;

	}
	public Domain generateDomain(){

		if( RODDOMAIN!= null ){
			return RODDOMAIN;
		}

		RODDOMAIN = new Domain();

		//create attributes
		Attribute xatt = new Attribute(RODDOMAIN, XATTNAME, Attribute.AttributeType.REAL);
		xatt.setLims(XMIN, XMAX);

		Attribute yatt = new Attribute(RODDOMAIN, YATTNAME, Attribute.AttributeType.REAL);
		yatt.setLims(YMIN, YMAX);

		Attribute x1att = new Attribute(RODDOMAIN, X1ATTNAME, Attribute.AttributeType.REAL);
		xatt.setLims(XMIN, XMAX);

		Attribute y1att = new Attribute(RODDOMAIN, Y1ATTNAME, Attribute.AttributeType.REAL);
		yatt.setLims(YMIN, YMAX);

		Attribute aatt = new Attribute(RODDOMAIN, AATTNAME, Attribute.AttributeType.REAL);
		aatt.setLims(-ANGLEMAX, ANGLEMAX);

		Attribute latt = new Attribute(RODDOMAIN, LATTNAME, Attribute.AttributeType.REAL);
		latt.setLims(XMIN, XMAX);

		Attribute ratt = new Attribute(RODDOMAIN, RATTNAME, Attribute.AttributeType.REAL);
		ratt.setLims(XMIN, XMAX);

		Attribute batt = new Attribute(RODDOMAIN, BATTNAME, Attribute.AttributeType.REAL);
		batt.setLims(YMIN, YMAX);

		Attribute tatt = new Attribute(RODDOMAIN, TATTNAME, Attribute.AttributeType.REAL);
		tatt.setLims(YMIN, YMAX);

		//create classes
		ObjectClass agentclass = new ObjectClass(RODDOMAIN, AGENTCLASS);
		agentclass.addAttribute(xatt);
		agentclass.addAttribute(yatt);
		agentclass.addAttribute(x1att);
		agentclass.addAttribute(y1att);
		agentclass.addAttribute(aatt);

		ObjectClass obstclss = new ObjectClass(RODDOMAIN, OBSTACLECLASS);
		obstclss.addAttribute(latt);
		obstclss.addAttribute(ratt);
		obstclss.addAttribute(batt);
		obstclss.addAttribute(tatt);


		ObjectClass goalclass = new ObjectClass(RODDOMAIN, GOALCLASS);
		goalclass.addAttribute(latt);
		goalclass.addAttribute(ratt);
		goalclass.addAttribute(batt);
		goalclass.addAttribute(tatt);

		//Initialize actions
		Action moveUp = new ActionMoveUp(ACTIONMOVEUP, RODDOMAIN, "");
		Action moveDown = new ActionMoveDown(ACTIONMOVEDOWN, RODDOMAIN, "");
		Action turnLeft = new ActionTurnLeft(ACTIONTURNLEFT, RODDOMAIN, "");
		Action turnRight = new ActionTurnRight(ACTIONTURNRIGHT, RODDOMAIN, "");

		//add pfs
		PropositionalFunction touchGoal = new TouchGoalPF(PFTOUCHGOAL, RODDOMAIN, new String[]{AGENTCLASS, GOALCLASS});
		PropositionalFunction touchSurface = new TouchSurfacePF(PFTOUCHSURFACE, RODDOMAIN, new String[]{AGENTCLASS, OBSTACLECLASS});

		return RODDOMAIN;

	}

	public static void updateMotion(State st, double change) {

		ObjectInstance agent = st.getObjectsOfTrueClass(AGENTCLASS).get(0);
		double ang = agent.getRealValForAttribute(AATTNAME);
		double x = agent.getRealValForAttribute(XATTNAME);
		double y = agent.getRealValForAttribute(YATTNAME);

		if(change == 1.0 || change == -1.0 ){
			x = x + change;
			y = y + change;
		}else{
			x = (Math.cos(ang)*x) + (Math.sin(ang) * y * -1);
		}

		if (x > XMAX || x < XMIN || y > YMAX || y<YMIN){
			x = x - change;
			y = y - change;
		}

		//hits obstacles
		ObjectInstance obstacle = st.getObjectsOfTrueClass(OBSTACLECLASS).get(0);
		double l = obstacle.getRealValForAttribute(LATTNAME);
		double r = obstacle.getRealValForAttribute(RATTNAME);
		double b = obstacle.getRealValForAttribute(BATTNAME);
		double t = obstacle.getRealValForAttribute(TATTNAME);

		if (y >= b && y <= t && x >=l && x <= r){
			x = x - change;
			y = y - change;
		}

		agent.setValue(XATTNAME, x);
		agent.setValue(YATTNAME, y);
		agent.setValue(AATTNAME, ang);


	}

	public static void incAngle(State st, int dir) {
		ObjectInstance agent = st.getObjectsOfTrueClass(AGENTCLASS).get(0);
		double curA = agent.getRealValForAttribute(AATTNAME);

		double newa = curA + (dir * ANGLEINC);
		if(newa > ANGLEMAX){
			newa = ANGLEMAX;
		}
		else if(newa < -ANGLEMAX){
			newa = -ANGLEMAX;
		}

		agent.setValue(AATTNAME, newa);

	}

	public class ActionMoveUp extends Action{

		public ActionMoveUp(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionMoveUp(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RodExperimentDomain.updateMotion(st, 1.0);
			return st;
		}
	}

	public class ActionMoveDown extends Action{

		public ActionMoveDown(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionMoveDown(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RodExperimentDomain.updateMotion(st, -1.0);
			return st;
		}
	}

	public class ActionTurnRight extends Action{

		public ActionTurnRight(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionTurnRight(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RodExperimentDomain.incAngle(st, 1);
			RodExperimentDomain.updateMotion(st, 0.0);
			return st;
		}
	}

	public class ActionTurnLeft extends Action{

		public ActionTurnLeft(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}

		public ActionTurnLeft(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			RodExperimentDomain.incAngle(st, -1);
			RodExperimentDomain.updateMotion(st, 0.0);
			return st;
		}
	}

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

			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);

			if(x>=l && x<r && y==t){
				return true;
			}

			return false;
		}

	}

	public class TouchSurfacePF extends PropositionalFunction{

		public TouchSurfacePF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}

		public TouchSurfacePF(String name, Domain domain, String [] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {


			ObjectInstance agent = st.getObject(params[0]);
			ObjectInstance o = st.getObject(params[1]);
			double x = agent.getRealValForAttribute(XATTNAME);
			double y = agent.getRealValForAttribute(YATTNAME);

			double l = o.getRealValForAttribute(LATTNAME);
			double r = o.getRealValForAttribute(RATTNAME);
			double b = o.getRealValForAttribute(BATTNAME);
			double t = o.getRealValForAttribute(TATTNAME);

			if(x >= l && x <= r && y >= b && y <= t){
				return true;
			}

			return false;
		}



	}

}
