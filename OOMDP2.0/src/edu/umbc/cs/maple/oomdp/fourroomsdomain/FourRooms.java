package edu.umbc.cs.maple.oomdp.fourroomsdomain;

import java.util.*;

import edu.umbc.cs.maple.domain.oomdp.DomainGenerator;
import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.ObjectClass;
import edu.umbc.cs.maple.oomdp.ObjectInstance;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.RewardFunction;
import edu.umbc.cs.maple.oomdp.State;
import edu.umbc.cs.maple.oomdp.explorer.TerminalExplorer;
import edu.umbc.cs.maple.oomdp.explorer.VisualExplorer;
import edu.umbc.cs.maple.oomdp.visualizer.Visualizer;
import edu.brown.cs.ai.behavior.oomdp.planning.deterministc.*;
import edu.umbc.cs.maple.behavior.oomdp.planning.*;

public class FourRooms implements DomainGenerator {
	
	/**
	 * PLEASE READ: Couple things to note:
	 * 1) The Q-Learning Code is still partially attached to the domain. I am working on that.
	 * 2) Grounded Actions support for the Q-Learning Code does not exist at the moment. 
	 * 		Working on that too.
	 * 3) Having some trouble grabbing the reward function implementation due to null objects
	 * 		State.getObject(String) gives me null, however State.getObjectsOfTrueClass(String).get(0) does not...
	 * 		and for some reason the RewardFunction doesn't like that.
	 * 4) All it prints for now is just regular steps. As soon as functionality for Grounded Actions increases
	 * I can go ahead and implement the episode analyzer.
	 */

	//Attributes, Actions, and PropFuncs
	public static final String ATTX = "x";
	public static final String ATTY = "y";
	public static final String CLASSAGENT = "agent";
	public static final String CLASSGOAL = "goal";
	public static final String ACTIONNORTH = "north";
	public static final String ACTIONSOUTH = "south";
	public static final String ACTIONEAST = "east";
	public static final String ACTIONWEST = "west";
	public static final String PFATGOAL = "atGoal";	
	public static final int	MAXX = 12;
	public static final int	MAXY = 12;	
	
	//For the domain
	public static int[][] MAP;
	public static Domain DOMAIN = null;	
	
	//Extra Stuff I added for the learning algorithm
	public static final double LEARNINGRATE = 0.99;
	public static final double DISCOUNTFACTOR = 0.95;
	public static Map<StateHashTuple, List<QAction>> qVals = new HashMap<StateHashTuple, List<QAction>>();
	
	/**
	 * main() - starts the program
	 * @param args - none for now...
	 */
	public static void main(String[] args) {
		FourRooms frd = new FourRooms();
		Domain d = frd.generateDomain();
		State s = FourRooms.getCleanState();
		
		setAgent(s, 1, 1);
		setGoal(s, 5, 5);
		
		int expMode = 3;
		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
		}
		
		if(expMode == 0){	
			TerminalExplorer exp = new TerminalExplorer(d);
			exp.addActionShortHand("n", ACTIONNORTH);
			exp.addActionShortHand("e", ACTIONEAST);
			exp.addActionShortHand("w", ACTIONWEST);
			exp.addActionShortHand("s", ACTIONSOUTH);
		}else if(expMode == 1){
			Visualizer v = FourRoomsVisual.getVisualizer();
			VisualExplorer exp = new VisualExplorer(d, v, s);	
			//use w-s-a-d-x
			exp.addKeyAction("w", ACTIONNORTH);
			exp.addKeyAction("s", ACTIONSOUTH);
			exp.addKeyAction("a", ACTIONWEST);
			exp.addKeyAction("d", ACTIONEAST);
			exp.initGUI();
		}else if(expMode == 3){
			
			//Runs the simulator via text output
			for(int i = 1; i <= 50; i++){
				System.out.println("\t\t~ Episode " + i + " ~\t\t");
				runSim(d, s);
				setAgent(s, 1, 1);
				setGoal(s, 5, 5);
			}
		}
	}
	
	/**
	 * runSim() - runs one episode of the simulation
	 * @param d - domain 
	 * @param s - starting state of the simulation
	 * @return - none
	 */
	public static void runSim(Domain d, State s){
		System.out.println("Running One Episode.");
		
		//Variable Declaration
		List<QAction> currStateActionList = null;
		int steps = 0;
		
		//While the agent is not at the goal state
		while(!d.getPropFunction(PFATGOAL).isTrue(s, new String[]{FourRooms.CLASSAGENT, FourRooms.CLASSGOAL})){
			
			/***************Part one****************/
			
			//Generate the StateHashTuple
			Map <String, List<Attribute>> attributesForHash = new HashMap<String, List<Attribute>>();
			attributesForHash.put(FourRooms.CLASSAGENT, DOMAIN.getObjectClass(FourRooms.CLASSAGENT).attributeList_);
			StateHashTuple currentStateTuple = new StateHashTuple(s, attributesForHash);
			
			//Search for a match
			for(Map.Entry<StateHashTuple, List<QAction>> entry: qVals.entrySet()){
				if(currentStateTuple.equals(entry.getKey())){
					currStateActionList = entry.getValue();
					break;
				}
			}
			
			//no match found
			if(currStateActionList == null){
				currStateActionList = new ArrayList<QAction>();
				currStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONNORTH)).get(0), Math.random() * 12));
				currStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONSOUTH)).get(0), Math.random() * 12));
				currStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONEAST)).get(0), Math.random() * 12));
				currStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONWEST)).get(0), Math.random() * 12));
				qVals.put(currentStateTuple, currStateActionList);
			}
			
			//Looking for the best Q-Value in the current state
			Double currentQvalue = -100.0;
			GroundedAction groundAction = null;
			for(QAction oldVal :currStateActionList){
				if(currentQvalue <= oldVal.getQVal()){
					currentQvalue = oldVal.getQVal();
					groundAction = oldVal.action;
				}
			}
			
			//error checking - prevents a null pointer exception call
			//note - unless the currentQvalue equals -100.00, it shouldn't execute
			if(groundAction == null && currentQvalue == -100.0){
				System.out.println("Fatal Error - Current State");
				System.exit(0);
			}
			
			/**************Part Two****************/
			
			//Perform the Action
			Action doAction = d.getAction(groundAction.action.getName());
			State newState = doAction.performAction(s, "");
			
			/***************Part Three***************/
			
			//Generate the StateHashTuple for the new state
			Map <String, List<Attribute>> attributesForHashNew = new HashMap<String, List<Attribute>>();
			attributesForHashNew.put(FourRooms.CLASSAGENT, DOMAIN.getObjectClass(FourRooms.CLASSAGENT).attributeList_);
			StateHashTuple newStateTuple = new StateHashTuple(newState, attributesForHashNew);
			List<QAction> newStateActionList = null;
			
			//Search for a match
			for(Map.Entry<StateHashTuple, List<QAction>> entry: qVals.entrySet()){
				if(newStateTuple.equals(entry.getKey())){
					newStateActionList = entry.getValue();
					break;
				}
			}
			
			//no match found
			if(newStateActionList == null){
				newStateActionList = new ArrayList<QAction>();
				newStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONNORTH)).get(0), Math.random() * 12));
				newStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONSOUTH)).get(0), Math.random() * 12));
				newStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONEAST)).get(0), Math.random() * 12));
				newStateActionList.add(new QAction(s.getAllGroundedActionsFor(d.getAction(ACTIONWEST)).get(0), Math.random() * 12));
				qVals.put(newStateTuple, newStateActionList);
			}
			
			//Looking for the best Q-Value in the current state
			Double highestQvalue = -100.0;
			GroundedAction newGroundAction = null;
			for(QAction oldVal :newStateActionList){
				if(highestQvalue <= oldVal.getQVal()){
					highestQvalue = oldVal.getQVal();
					newGroundAction = oldVal.action;
				}
			}
			
			//error checking - prevents a null pointer exception call
			//note - unless the highestQvalue equals -100.00, it shouldn't execute
			if(newGroundAction == null && highestQvalue == -100.0){
				System.out.println("Fatal Error - Second Part");
				System.exit(0);
			}
			
			/******************Part Four**************/
			
			//QValue update
			double reward = FourRooms.getReward(s, newState, d);
			double newQval = FourRooms.updateQValue(currentQvalue, highestQvalue, reward);
			for(QAction oldVal :currStateActionList){
				if(currentQvalue == oldVal.getQVal()){
					 oldVal.setQVal(newQval);
				}
			}
			
			s = newState;
			steps++;
		}
		
		//End of the Episode
		System.out.println("\tSteps: " + steps);
	}
	
	/**
	 * getReward() - returns the reward of the state-action pair executed
	 * @param currentState - agent's current position
	 * @param newState - agent's new position once the action is committed
	 * @param d - domain
	 * @return - reward value
	 */
	public static double getReward(State currentState, State newState, Domain d){
		if(d.getPropFunction(PFATGOAL).isTrue(currentState, new String[]{CLASSAGENT, CLASSGOAL})) //the agent has reached the goal
			return 10;
		else{
			int cX = currentState.getObjectsOfTrueClass(CLASSAGENT).get(0).getDiscValForAttribute(ATTX);
			int cY = currentState.getObjectsOfTrueClass(CLASSAGENT).get(0).getDiscValForAttribute(ATTY);
			//System.out.println("Current State::\t\t" + cX + ":" + cY);
			
			int nX = newState.getObjectsOfTrueClass(CLASSAGENT).get(0).getDiscValForAttribute(ATTX);
			int nY = newState.getObjectsOfTrueClass(CLASSAGENT).get(0).getDiscValForAttribute(ATTY);
			//System.out.println("New State::\t\t" + nX + ":" + nY);

			if(cX == nX && cY == nY) 
				return -5; //the agent has not moved - hit a wall
			else
				return -1; //the agent has moved - still on the grid
		}
	}

	@Override
	/**
	 * generateDomain() - generates the domain for the program to use
	 * defines all the classes and objects available in the domain, as well
	 * as the attributes, actions, and conditions.
	 * @return - none
	 */
	public Domain generateDomain(){		
		if(DOMAIN != null){
			return DOMAIN;
		}
		
		//otherwise create the domain data
		DOMAIN = new Domain();
		generateMap();
		
		//creates the attributes, define the parameters of the domain
		Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, MAXX, 1);
		
		Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, MAXY, 1);
		
		DOMAIN.addAttribute(xatt);
		DOMAIN.addAttribute(yatt);
		
		//adding the objects associated with the domain - agent and goal
		ObjectClass agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);
		
		ObjectClass goalClass = new ObjectClass(DOMAIN, CLASSGOAL);
		goalClass.addAttribute(xatt);
		goalClass.addAttribute(yatt);
		
		DOMAIN.addObjectClass(goalClass);
		DOMAIN.addObjectClass(agentClass);
	
		//adding the actions
		Action north = new NorthAction(ACTIONNORTH, DOMAIN, "");
		Action south = new SouthAction(ACTIONSOUTH, DOMAIN, "");
		Action east = new EastAction(ACTIONEAST, DOMAIN, "");
		Action west = new WestAction(ACTIONWEST, DOMAIN, "");
		
		DOMAIN.addAction(north);
		DOMAIN.addAction(south);
		DOMAIN.addAction(east);
		DOMAIN.addAction(west);
		
		//adding the conditions 
		PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, new String[]{CLASSAGENT, CLASSGOAL});
		DOMAIN.addPropositionalFunction(atGoal);
		
		//reward function - currently unoperational
		RewardFunction rf = new SingleGoalPFRF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));
		rf.setDomain(DOMAIN);
		
		return DOMAIN;
	}

	/**
	 * getCleanState() - creates a fresh new state to use in the state transition.
	 * @return - a state object
	 */
	public static State getCleanState(){	
		FourRooms frd = new FourRooms();
		frd.generateDomain();
		State s = new State();
		
		//adds the object and goal to the current state
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSAGENT), CLASSAGENT+0));
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSGOAL), CLASSGOAL+0));
		return s;
	}
	
	/**
	 * setAgent() - sets the agent's location inside the initial state
	 * @param s - state
	 * @param x - x attribute
	 * @param y - y attribute
	 */
	public static void setAgent(State s, int x, int y){
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		agent.setValue(ATTX, x);
		agent.setValue(ATTY, y);
	}
	
	/**
	 * setGoal() - sets the goal's location inside the initial state 
	 * @param s - state
	 * @param x - x attribute
	 * @param y - y attribute
	 */
	public static void setGoal(State s, int x, int y){
		ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(0);
		goal.setValue(ATTX, x);
		goal.setValue(ATTY, y);
	}
	
	/**
	 * generateMap() - generates a 2D array map to represent the boundary's of
	 * the domain.
	 */
	public static void generateMap(){
		MAP = new int[MAXX+1][MAXY+1]; //+1 to handle zero base
		frameMap();
		setStandardWalls();
	}
	
	/**
	 * frameMap() - sets up the wall of the domain
	 */
	public static void frameMap(){
		for(int x = 0; x <= MAXX; x++){
			for(int y = 0; y <= MAXY; y++){
				if(x == 0 || x == MAXX || y == 0 || y == MAXY)
					MAP[x][y] = 1;
				else
					MAP[x][y] = 0;
			}
		}
	}
	
	/**
	 * setStandardWalls() - sets up the doorways of the domain
	 */
	public static void setStandardWalls(){
		horizontalWall(1, 1, 6);
		horizontalWall(3, 5, 6);
		horizontalWall(7, 8, 5);
		horizontalWall(10, 11, 5);
		
		verticalWall(1, 1, 6);
		verticalWall(3, 8, 6);
		verticalWall(10, 11, 6);
	}
	
	/**
	 * horizontalWall() - creates a horizontal barrier for the agent
	 * @param xi - starting x position
	 * @param xf - final x position
	 * @param y - y position
	 */
	protected static void horizontalWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++)
			MAP[x][y] = 1;
	}
	
	/**
	 * verticalWall() - creates a vertical barrier for the agent 
	 * @param yi - starting y position
	 * @param yf - ending y position
	 * @param x - x position
	 */
	protected static void verticalWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++)
			MAP[x][y] = 1;
	}
	
	/**
	 * move() - moves the agent to a new location, thus creating a
	 * state transition from s to s-prime. 
	 * @param s - State
	 * @param xd - x distance
	 * @param yd - y distance
	 */
	public static void move(State s, int xd, int yd){
		
		//Collects the position of the agent, based off true class
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int nx = ax+xd;
		int ny = ay+yd;
		
		//checks to see if it hit a wall
		if(MAP[nx][ny] == 1){
			nx = ax;
			ny = ay;
		}
		
		//updates the agent's position
		agent.setValue(ATTX, nx);
		agent.setValue(ATTY, ny);
	
	}
	
	/**
	 * NorthAction - extended class to replicate north action
	 */
	public static class NorthAction extends Action{
		public NorthAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		/**
		 * performActionHelper() - helper function that executes the action
		 * @param st - state
		 * @param params - string parameters
		 * @return - returns the state that has been updated by move.
		 */
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, 1);
			return st;
		}
	}
	
	/**
	 * SouthAction - extended class to replicate south action
	 */
	public static class SouthAction extends Action{
		public SouthAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		/**
		 * performActionHelper() - helper function that executes the action
		 * @param st - state
		 * @param params - string parameters
		 * @return - returns the state that has been updated by move.
		 */
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, -1);
			return st;
		}
	}
	
	/**
	 * EastAction - extended class to replicate east action
	 */
	public static class EastAction extends Action{
		public EastAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		/**
		 * performActionHelper() - helper function that executes the action
		 * @param st - state
		 * @param params - string parameters
		 * @return - returns the state that has been updated by move.
		 */
		protected State performActionHelper(State st, String[] params) {
			move(st, 1, 0);
			return st;
		}
	}
	
	/**
	 * WestAction - extended class to replicate west action
	 */
	public static class WestAction extends Action{
		public WestAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		/**
		 * performActionHelper() - helper function that executes the action
		 * @param st - state
		 * @param params - string parameters
		 * @return - returns the state that has been updated by move.
		 */
		protected State performActionHelper(State st, String[] params) {
			move(st, -1, 0);
			return st;
		}
	}
	
	/**
	 * AtGoalPF - propositional function to determine if at goal state
	 */
	public static class AtGoalPF extends PropositionalFunction{
		public AtGoalPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		/**
		 * isTrue() - determines if the agent has reached the goal state
		 * @param st - state
		 * @param params - string parameters
		 */
		public boolean isTrue(State st, String[] params){
			ObjectInstance agent = st.getObjectsOfTrueClass(params[0]).get(0);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			
			ObjectInstance goal = st.getObjectsOfTrueClass(params[1]).get(0);
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			
			if(ax == gx && ay == gy)
				return true;
			return false;
		}	
	}
	
	/**
	 * updateQValue() - runs the update function on the Q-Value
	 * @param oldQVal - original q value
	 * @param highestQVal - highest q value from the set from s-prime
	 * @param reward - reward function
	 * @return - updated q-value
	 */
	public static double updateQValue(double oldQVal, double highestQVal, double reward){
		return oldQVal + (FourRooms.LEARNINGRATE * reward) + ((FourRooms.DISCOUNTFACTOR * highestQVal) - oldQVal);
	}
}
