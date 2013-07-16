package edu.umbc.cs.maple.oomdp.rodexperiment;

import edu.umbc.cs.maple.domain.oomdp.DomainGenerator;
import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectClass;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.State;

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
	public static final String				PFREACHGOAL = "reachedGoal";
	public static final String				PFTOUCHGOAL = "touchedGoal";
	public static final String				PFTOUCHSURFACE = "touchingSurface"; //touching an obstacle

	public static Domain					RODDOMAIN = null;	

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
		PropositionalFunction reachGoal = new ReachGoalPF(PFREACHGOAL, RODDOMAIN, new String[]{AGENTCLASS, GOALCLASS});
		PropositionalFunction touchGoal = new TouchGoalPF(PFTOUCHGOAL, RODDOMAIN, new String[]{AGENTCLASS, GOALCLASS});
		PropositionalFunction touchSurface = new TouchSurfacePF(PFTOUCHSURFACE, RODDOMAIN, new String[]{AGENTCLASS, OBSTACLECLASS});

		return RODDOMAIN;

	}

	public static void updateMotion(State st, double change) {
		// TODO Auto-generated method stub
		
	}

	public static void incAngle(State st, int dir) {
		// TODO Auto-generated method stub
		
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
	
	public class ReachGoalPF extends PropositionalFunction{

		public ReachGoalPF(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}

}
