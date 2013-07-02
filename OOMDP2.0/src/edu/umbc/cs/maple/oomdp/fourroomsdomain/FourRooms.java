package edu.umbc.cs.maple.oomdp.fourroomsdomain;

import java.util.*;

import edu.umbc.cs.maple.domain.oomdp.DomainGenerator;
import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.GroundedProp;
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
import edu.umbc.cs.maple.behavior.oomdp.EpisodeAnalysis;

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
	public static EpisodeAnalysis analyzer;
	public static FourRoomsStateParser parser;
	
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
		
		int expMode = 1;
		
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
			for(int i = 1; i <= 1; i++){
				analyzer = new EpisodeAnalysis();
				parser = new FourRoomsStateParser();
				System.out.println("\t\t~ Episode " + i + " ~\t\t");
				runSim(d, s);
				setAgent(s, 1, 1);
				setGoal(s, 5, 5);
				analyzer.writeToFile("Episode " + i + ".txt", parser);
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
		
		//Variable Declaration
		List<QAction> currStateActionList = null;
		int steps = 0;
		
		//Reward Function Implementation...
		RewardFunction rf = new SingleGoalPFRF(d.getPropFunction(PFATGOAL));
		rf.setDomain(d);
		//d.getPropFunction(PFATGOAL).isTrue(s, new String[]{FourRooms.CLASSAGENT, FourRooms.CLASSGOAL})
		
		
		//While the agent is not at the goal state
		while(!FourRooms.isTrue(s, d.getPropFunction(PFATGOAL))){
			
			/***************Part one****************/
			
			//Generate the StateHashTuple
			Map <String, List<Attribute>> attributesForHash = new HashMap<String, List<Attribute>>();
			attributesForHash.put(FourRooms.CLASSAGENT, DOMAIN.getObjectClass(FourRooms.CLASSAGENT).attributeList_);
			StateHashTuple currentStateTuple = new StateHashTuple(s, attributesForHash);
			
			//Search for a match
			currStateActionList = qVals.get(currentStateTuple);

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
			
			//System.out.println("Action Taken: " + groundAction.action.getName());
			
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
			newStateActionList = qVals.get(newStateTuple);
			
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
			double reward = rf.reward(s, groundAction, newState);
			double newQval = FourRooms.updateQValue(currentQvalue, highestQvalue, reward);
			for(QAction oldVal :currStateActionList){
				if(currentQvalue == oldVal.getQVal()){
					 oldVal.setQVal(newQval);
				}
			}
			//System.out.println("Reward Function reward: " + rf.reward(s, groundAction, newState));
			
			//Change the States
			//System.out.print(s.getStateDescription());
			//System.out.println("Step number: " + steps + "\n---------------\n");
			s = newState;
			steps++;
			analyzer.record(s, groundAction, reward);
		}
		
		//End of the Episode
		System.out.println("\tSteps: " + steps);
	}
	
	
	public static boolean isTrue(State s, PropositionalFunction pf){
		boolean isTrue = false;
		
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		for(GroundedProp gp: gps){
			if(gp.isTrue(s)){
				isTrue = true;
			}else{
				break;
			}
		}
		
		return isTrue;
	}
	
	/**
	 * getReward() - returns the reward of the state-action pair executed
	 * @param currentState - agent's current position
	 * @param newState - agent's new position once the action is committed
	 * @param d - domain
	 * @return - reward value
	 */
	public static double getReward(State currentState, State newState, Domain d){
		if(FourRooms.isTrue(currentState, d.getPropFunction(PFATGOAL))) //the agent has reached the goal
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
	public Domain generateDomain(){		
		if(DOMAIN != null){
			return DOMAIN;
		}
		
		//otherwise create the domain data
		DOMAIN = new Domain();
		generateMap();
		
		Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, MAXX, 1);
		
		Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, MAXY, 1);
		
		DOMAIN.addAttribute(xatt);
		DOMAIN.addAttribute(yatt);
		
		ObjectClass agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);
		
		ObjectClass goalClass = new ObjectClass(DOMAIN, CLASSGOAL);
		goalClass.addAttribute(xatt);
		goalClass.addAttribute(yatt);
		
		DOMAIN.addObjectClass(goalClass);
		DOMAIN.addObjectClass(agentClass);
	
		Action north = new NorthAction(ACTIONNORTH, DOMAIN, "");
		Action south = new SouthAction(ACTIONSOUTH, DOMAIN, "");
		Action east = new EastAction(ACTIONEAST, DOMAIN, "");
		Action west = new WestAction(ACTIONWEST, DOMAIN, "");
		
		DOMAIN.addAction(north);
		DOMAIN.addAction(south);
		DOMAIN.addAction(east);
		DOMAIN.addAction(west);
		
		PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, new String[]{CLASSAGENT, CLASSGOAL});
		DOMAIN.addPropositionalFunction(atGoal);
		
		RewardFunction rf = new SingleGoalPFRF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));
		rf.setDomain(DOMAIN);
		
		
		return DOMAIN;
	}

	public static State getCleanState(){	
		FourRooms frd = new FourRooms();
		frd.generateDomain();
		State s = new State();
		
		//the order of the objects determines the order in which they are drawn -Richard
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSGOAL), CLASSGOAL+0));
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSAGENT), CLASSAGENT+0));
		
		return s;
	}
	
	public static void setAgent(State s, int x, int y){
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		agent.setValue(ATTX, x);
		agent.setValue(ATTY, y);
	}
	
	public static void setGoal(State s, int x, int y){
		ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(0);
		goal.setValue(ATTX, x);
		goal.setValue(ATTY, y);
	}
	
	public static void generateMap(){
		MAP = new int[MAXX+1][MAXY+1]; //+1 to handle zero base
		frameMap();
		setStandardWalls();
	}
	
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
	
	public static void setStandardWalls(){
		horizontalWall(1, 1, 6);
		horizontalWall(3, 5, 6);
		horizontalWall(7, 8, 5);
		horizontalWall(10, 11, 5);
		
		verticalWall(1, 1, 6);
		verticalWall(3, 8, 6);
		verticalWall(10, 11, 6);
	}
	
	protected static void horizontalWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++)
			MAP[x][y] = 1;
	}
	
	protected static void verticalWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++)
			MAP[x][y] = 1;
	}
	
	public static void move(State s, int xd, int yd){
		
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		int ax = agent.getDiscValForAttribute(ATTX);
		int ay = agent.getDiscValForAttribute(ATTY);
		int nx = ax+xd;
		int ny = ay+yd;
		
		if(MAP[nx][ny] == 1){
			nx = ax;
			ny = ay;
		}
		
		agent.setValue(ATTX, nx);
		agent.setValue(ATTY, ny);
	
	}
	
	public static class NorthAction extends Action{
		public NorthAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, 1);
			return st;
		}
	}
	
	public static class SouthAction extends Action{
		public SouthAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			move(st, 0, -1);
			return st;
		}
	}
	
	public static class EastAction extends Action{
		public EastAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			move(st, 1, 0);
			return st;
		}
	}
	
	public static class WestAction extends Action{
		public WestAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			move(st, -1, 0);
			return st;
		}
	}
	
	public static class AtGoalPF extends PropositionalFunction{
		public AtGoalPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params){
			ObjectInstance agent = st.getObject(params[0]);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			
			ObjectInstance goal = st.getObject(params[1]);
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			
			if(ax == gx && ay == gy)
				return true;
			return false;
		}	
	}
	
	public static double updateQValue(double oldQVal, double highestQVal, double reward){
		return oldQVal + FourRooms.LEARNINGRATE * ((reward + FourRooms.DISCOUNTFACTOR * highestQVal) - oldQVal);
	}
}