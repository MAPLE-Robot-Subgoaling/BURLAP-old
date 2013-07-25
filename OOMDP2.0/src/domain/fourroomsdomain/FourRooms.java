package domain.fourroomsdomain;

import java.util.*;

import oomdptb.behavior.EpisodeAnalysis;
import oomdptb.behavior.EpisodeSequenceVisualizer;
import oomdptb.behavior.Policy;
import oomdptb.behavior.QValue;
import oomdptb.behavior.learning.tdmethods.QLearning;
import oomdptb.behavior.learning.tdmethods.SarsaLam;
import oomdptb.behavior.options.PrimitiveOption;
import oomdptb.behavior.options.SubgoalOption;
import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.behavior.statehashing.DiscreteStateHashFactory;
import oomdptb.behavior.statehashing.StateHashTuple;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.DomainGenerator;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.GroundedProp;
import oomdptb.oomdp.ObjectClass;
import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;
import oomdptb.oomdp.common.SingleGoalPFRF;
import oomdptb.oomdp.common.SinglePFTF;
import oomdptb.oomdp.explorer.TerminalExplorer;
import oomdptb.oomdp.explorer.VisualExplorer;
import oomdptb.oomdp.visualizer.Visualizer;

/**
 * A Domain implementing a Four Rooms, Grid-World style setup complete with four doorways for the agent
 * to traverse through. It holds two attributes [ATTX,ATTY] to declare the position of the respective 
 * Object Classes [CLASSAGENT, CLASSGOAL]. The agent is restricted to 4 possible motions [ACTIONNORTH, 
 * ACTIONSOUTH, ACTIONEAST, ACTIONWEST] and one goal oriented propositional function [PFATGOAL].
 * @author Tenji Tembo - MAPLE Lab
 */
public class FourRooms implements DomainGenerator {

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
	public static final double LAMBDA = 0.90;
	public static Map<StateHashTuple, List<QAction>> qVals = new HashMap<StateHashTuple, List<QAction>>();
	public static QLearning Q;
	public static SarsaLam S;
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
		setGoal(s, 11, 11);
		int expMode = 4;
		
		if(expMode == 0){		//Terminal Explorer
			TerminalExplorer exp = new TerminalExplorer(d);
			exp.addActionShortHand("n", ACTIONNORTH);
			exp.addActionShortHand("e", ACTIONEAST);
			exp.addActionShortHand("w", ACTIONWEST);
			exp.addActionShortHand("s", ACTIONSOUTH);
			
		}else if(expMode == 1){		//Visual Explorer 
			Visualizer v = FourRoomsVisual.getVisualizer();
			VisualExplorer exp = new VisualExplorer(d, v, s);	
			
			//use w-s-a-d-x
			exp.addKeyAction("w", ACTIONNORTH);
			exp.addKeyAction("s", ACTIONSOUTH);
			exp.addKeyAction("a", ACTIONWEST);
			exp.addKeyAction("d", ACTIONEAST);
			exp.initGUI();
			
		}else if(expMode == 2){		//Q-Learning - OOMDPTB Version
			//Runs the simulator via text output
			parser = new FourRoomsStateParser();
			
			System.out.println("\tRunning Q-Learning\n");
			
			for(int i = 1; i <= 100; i++){
				analyzer = new EpisodeAnalysis();
				 
				System.out.print("Episode " + i + ": ");
				analyzer = Q.runLearningEpisodeFrom(s);
				System.out.println("\tTime Steps: " + analyzer.numTimeSteps() + "\tSteps: " + Q.getLastNumSteps());
				analyzer.writeToFile(String.format("output/e%03d", i), parser);
				
				setAgent(s, 1, 1);
				setGoal(s, 11, 11);
			}
			
			//Visualize the Steps
			Visualizer v = FourRoomsVisual.getVisualizer();
			new EpisodeSequenceVisualizer(v, d, parser, "output");
			
			
		}else if(expMode == 3){		//Sarsa(Lambda) - OOMDPTB
			parser = new FourRoomsStateParser();
			
			System.out.println("\tRunning SARSA-LAMBDA\n");
			
			for(int i = 1; i <= 100; i++){
				analyzer = new EpisodeAnalysis();
				System.out.print("Episode " + i + ": ");
				analyzer = S.runLearningEpisodeFrom(s);
				System.out.println("\tTime Steps: " + analyzer.numTimeSteps() + "\tSteps: " + S.getLastNumSteps());
				analyzer.writeToFile(String.format("output/e%03d", i), parser);
				
				setAgent(s, 1, 1);
				setGoal(s, 11, 11);			
				
			}

			//Visualize the Steps
			Visualizer v = FourRoomsVisual.getVisualizer();
			new EpisodeSequenceVisualizer(v, d, parser, "output");
		}else if(expMode == 4){
			parser = new FourRoomsStateParser();
			
			//Opens the visualizer - Make sure there are episode files in the output folder
			Visualizer v = FourRoomsVisual.getVisualizer();
			new EpisodeSequenceVisualizer(v, d, parser, "output");
		}
	}
	
	/**
	 * run() - runs one episode of the Simulation.
	 * **Note - This code uses the framework provided by the OOMDPTB Package**
	 * @param d - Domain
	 * @param s - State
	 */
	public static void run(Domain d, State s){
		
		//Variable Declaration
		RewardFunction rf = new SingleGoalPFRF(d.getPropFunction(PFATGOAL), 10, -1.0);
		int steps = 0;

		//While the agent has not reached the goal state
		while(!FourRooms.isTrue(s, d.getPropFunction(PFATGOAL))){
			
			/******Part one********/
			//get the List of Actions and QValues
			List<QValue> currentStateActionList = Q.getQs(s);
			
			//search for a match
			currentStateActionList = Q.getQs(s);
			
			//Looking for the best QValue
			Double currentQValue = -100.00;
			GroundedAction groundAction = null;
			for(QValue oldVal: currentStateActionList){
				if(currentQValue <= oldVal.q){
					currentQValue = oldVal.q;
					groundAction = oldVal.a;
				}
			}
			
			//error checking - preventing a null groundAction from triggering
			if(groundAction == null && currentQValue == -100.00){
				System.out.println("Fatal Error");
				System.exit(0);
			}
			
			/******Part two********/
			Action doAction = d.getAction(groundAction.action.getName());
			State newState = doAction.performAction(s, "");
			
			/*******Part Three********/
			//get the new list of actions and q-values
			List<QValue> newStateActionList = Q.getQs(newState);
			
			//finds the highest q-value
			Double highestQvalue = -100.00;
			GroundedAction newGroundAction = null;
			for(QValue newVal: newStateActionList){
				if(highestQvalue <= newVal.q){
					highestQvalue = newVal.q;
					newGroundAction = newVal.a;
				}
			}
			
			//error checking - preventing a null ground action from triggering
			if(newGroundAction == null && highestQvalue == -100.00){
				System.out.println("Fatal Error - Part two");
				System.exit(0);
			}
			
			/**Part Four**/
			//Update QValue
			Double qVal = FourRooms.updateQValue(currentQValue, highestQvalue, rf.reward(s, groundAction, newState));
			for(QValue QVal: currentStateActionList){
				if(currentQValue == QVal.q && groundAction == QVal.a){
					QVal.q = qVal;
					break;
				}
			}
			
			//writes the items to the recorder
			analyzer.recordTransitionTo(newState, groundAction, qVal);
			s = newState;
			steps++;
		}
		System.out.println("\tSteps: " + steps);
		
	}
	
	public static boolean isTrue(State s, PropositionalFunction pf){
		boolean isTrue = false;
		
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		for(GroundedProp gp: gps){
			if(gp.isTrue(s))
				isTrue = true;
			else
				break;	
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
			
			int nX = newState.getObjectsOfTrueClass(CLASSAGENT).get(0).getDiscValForAttribute(ATTX);
			int nY = newState.getObjectsOfTrueClass(CLASSAGENT).get(0).getDiscValForAttribute(ATTY);

			if(cX == nX && cY == nY) 
				return -5; //the agent has not moved - hit a wall
			else
				return -1; //the agent has moved - still on the grid
		}
	}

	@Override
	public Domain generateDomain(){		
		if(DOMAIN != null)
			return DOMAIN;
		
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
	
		Action north = new PrimitiveOption(new NorthAction(ACTIONNORTH, DOMAIN, ""));
		Action south = new PrimitiveOption(new SouthAction(ACTIONSOUTH, DOMAIN, ""));
		Action east = new PrimitiveOption(new EastAction(ACTIONEAST, DOMAIN, ""));
		Action west = new PrimitiveOption(new WestAction(ACTIONWEST, DOMAIN, ""));
		
		DOMAIN.addAction(north);
		DOMAIN.addAction(south);
		DOMAIN.addAction(east);
		DOMAIN.addAction(west);
		
		//Toggle the commenting here when running with options or without them
		Action Door26 = new SubgoalOption("Start (1,1) to Door (2,6)", new StartToDoorNorthPolicy(), new StateCheck(1,1,2,6), new StateCheck(2,6,2,6));
		Action Door62 = new SubgoalOption("Start (1,1) to Door (6,2)", new StartToDoorEastPolicy(), new StateCheck(1,1,6,2), new StateCheck(6,2,6,2));
		Action Door69 = new SubgoalOption("Door (2,6) to Door (6,9)", new DoorNorthtoGoalEast(), new StateCheck(2,6,6,9), new StateCheck(6,9,6,9));
		Action Door95 = new SubgoalOption("Door (6,2) to Door (9,5)", new DoorEasttoGoalNorth(), new StateCheck(6,2,9,5), new StateCheck(9,5,9,5));
		Action Goal69 = new SubgoalOption("Door (6,9) to Goal (11,11)", new GoalEasttoGoal(), new StateCheck(6,9,11,11), new StateCheck(11,11,11,11));
		Action Goal95 = new SubgoalOption("Door (9,5) to Goal (11,11)", new GoalNorthtoGoal(), new StateCheck(9,5,11,11), new StateCheck(11,11,11,11));
		
		DOMAIN.addAction(Door26);
		DOMAIN.addAction(Door62);
		DOMAIN.addAction(Door69);
		DOMAIN.addAction(Door95);
		DOMAIN.addAction(Goal69);
		DOMAIN.addAction(Goal95);
		
		PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, new String[]{CLASSAGENT, CLASSGOAL});
		DOMAIN.addPropositionalFunction(atGoal);
		
		RewardFunction rf = new SingleGoalPFRF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));
		TerminalFunction tf = new SinglePFTF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));

		DiscreteStateHashFactory hashFactory = new DiscreteStateHashFactory();
		hashFactory.setAttributesForClass(CLASSAGENT, DOMAIN.getObjectClass(CLASSAGENT).attributeList);
		Q = new QLearning(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.2, FourRooms.LEARNINGRATE, Integer.MAX_VALUE);
		S = new SarsaLam(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.2, FourRooms.LEARNINGRATE, Integer.MAX_VALUE, FourRooms.LAMBDA);
		
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
	
	public static State generateState(int ax, int ay, int gx, int gy){
		State s = FourRooms.getCleanState();
		setAgent(s, ax, ay);
		setGoal(s, gx, gy);
		return s;
	}
	
	public static class StateCheck implements StateConditionTest{
			
		protected State condition;
		/**
		 * Creates the state Condition
		 * @param ax - agent x position
		 * @param ay - agent y position
		 * @param gx - goal x position
		 * @param gy - goal y position
		 */
		public StateCheck(int ax, int ay, int gx, int gy){
			condition = generateState(ax, ay, gx, gy);
		}
		
		/**
		 * runs the check via stateTuple()
		 * @param State s
		 * @return boolean true or false
		 */
		@Override
		public boolean satisfies(State s) {
			ObjectInstance agentState = s.getObject(CLASSAGENT+0);
			ObjectInstance agentCondition = condition.getObject(CLASSAGENT+0);
			
			int aSx = agentState.getDiscValForAttribute(ATTX); //X&Y for state passed in
			int aSy = agentState.getDiscValForAttribute(ATTY);
			
			int aCx = agentCondition.getDiscValForAttribute(ATTX); //X&Y for the Condition
			int aCy = agentCondition.getDiscValForAttribute(ATTY);
			
			if((aSx == aCx) && (aSy == aCy)) //Only Checks the Agent's relative position
				return true;
			return false;
		}
	}
	
	/**
	 * Simple Policy from the Start to the Door(2,6)
	 * @author Tenji Tembo
	 *
	 */
	public static class StartToDoorNorthPolicy extends Policy{
			
		//Policy Mapping
		public Map<StateHashTuple, GroundedAction> map = new HashMap<StateHashTuple, GroundedAction>();
		
		/**
		 * Generates the Policy Mapping
		 */
		public StartToDoorNorthPolicy(){
			map.put(new StateHashTuple(generateState(1, 1, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N
			map.put(new StateHashTuple(generateState(1, 2, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N
			map.put(new StateHashTuple(generateState(1, 3, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N
			map.put(new StateHashTuple(generateState(1, 4, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N		
			map.put(new StateHashTuple(generateState(1, 5, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E
			map.put(new StateHashTuple(generateState(2, 5, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N
			map.put(new StateHashTuple(generateState(2, 6, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N
		}
		
		
		@Override
		/**
		 * Enters the map and returns the corresponding GroundedAction for the State
		 */
		public GroundedAction getAction(State s) {
			return map.get(new StateHashTuple(s));
		}

		@Override
		/**
		 * Basic action distribution. Since it's one action per state, it's 100% all the time.
		 * @param State s
		 * @return ActionProb List
		 */
		public List<ActionProb> getActionDistributionForState(State s) {
			GroundedAction selectedAction = this.getAction(s);
			List <ActionProb> res = new ArrayList<Policy.ActionProb>();
			ActionProb ap = new ActionProb(selectedAction, 1.0);
			res.add(ap);
			return res;
		}

		@Override
		public boolean isStochastic() {
			return false;
		}
		
	}
	
	public static class StartToDoorEastPolicy extends Policy{
		
		//Policy Mapping
		public Map<StateHashTuple, GroundedAction> map = new HashMap<StateHashTuple, GroundedAction>();
		
		/**
		 * Generates the Policy Mapping
		 */
		public StartToDoorEastPolicy(){
			map.put(new StateHashTuple(generateState(1, 1, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E
			map.put(new StateHashTuple(generateState(2, 1, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E
			map.put(new StateHashTuple(generateState(3, 1, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E
			map.put(new StateHashTuple(generateState(4, 1, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E		
			map.put(new StateHashTuple(generateState(5, 1, 11, 11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), ""));	//N
			map.put(new StateHashTuple(generateState(5, 2, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E
			map.put(new StateHashTuple(generateState(6, 2, 11, 11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), ""));	//E
		}
		
		
		@Override
		/**
		 * Enters the map and returns the corresponding GroundedAction for the State
		 */
		public GroundedAction getAction(State s) {
			return map.get(new StateHashTuple(s));
		}

		@Override
		/**
		 * Basic action distribution. Since it's one action per state, it's 100% all the time.
		 * @param State s
		 * @return ActionProb List
		 */
		public List<ActionProb> getActionDistributionForState(State s) {
			GroundedAction selectedAction = this.getAction(s);
			List <ActionProb> res = new ArrayList<Policy.ActionProb>();
			ActionProb ap = new ActionProb(selectedAction, 1.0);
			res.add(ap);
			return res;
		}

		@Override
		public boolean isStochastic() {
			return false;
		}
		
	}
	
	public static class DoorNorthtoGoalEast extends Policy{
		//Policy Mapping
		public Map<StateHashTuple, GroundedAction> map = new HashMap<StateHashTuple, GroundedAction>();

		/**
		 *Generates the Policy
		 */

		public DoorNorthtoGoalEast(){
			map.put(new StateHashTuple(generateState(2,6,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(2,7,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(2,8,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(2,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(3,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(4,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(5,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(6,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
		}

		@Override
		/*
		 * Enters the map and returns the corresponding GroundedAction for the State
		 */
		public GroundedAction getAction(State s){
			return map.get(new StateHashTuple(s));
		}

		@Override
		/*
		 *Basic Action distribution. Since it's one action per state, it's 100% all of the time.
		 * @param State s
		 * @return ActionProb list
		 */
		public List<ActionProb> getActionDistributionForState(State s){
			GroundedAction selectedAction = this.getAction(s);
			List<ActionProb> res = new ArrayList<Policy.ActionProb>();
			ActionProb ap = new ActionProb(selectedAction, 1.0);
			res.add(ap);
			return res;
		}	

		@Override
		public boolean isStochastic(){
			return false;
		}
	}
	
	public static class DoorEasttoGoalNorth extends Policy{
		//Policy Mapping
		public Map<StateHashTuple, GroundedAction> map = new HashMap<StateHashTuple, GroundedAction>();

		/**
		 *Generates the Policy
		 */

		public DoorEasttoGoalNorth(){
			map.put(new StateHashTuple(generateState(6,2,11,11)), new GroundedAction(new EastAction(ACTIONNORTH, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(7,2,11,11)), new GroundedAction(new EastAction(ACTIONNORTH, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(8,2,11,11)), new GroundedAction(new EastAction(ACTIONNORTH, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(9,2,11,11)), new GroundedAction(new NorthAction(ACTIONEAST, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(9,3,11,11)), new GroundedAction(new NorthAction(ACTIONEAST, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(9,4,11,11)), new GroundedAction(new NorthAction(ACTIONEAST, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(9,5,11,11)), new GroundedAction(new NorthAction(ACTIONEAST, DOMAIN, ""), "")); //N
		}

		@Override
		/*
		 * Enters the map and returns the corresponding GroundedAction for the State
		 */
		public GroundedAction getAction(State s){
			return map.get(new StateHashTuple(s));
		}

		@Override
		/*
		 *Basic Action distribution. Since it's one action per state, it's 100% all of the time.
		 * @param State s
		 * @return ActionProb list
		 */
		public List<ActionProb> getActionDistributionForState(State s){
			GroundedAction selectedAction = this.getAction(s);
			List<ActionProb> res = new ArrayList<Policy.ActionProb>();
			ActionProb ap = new ActionProb(selectedAction, 1.0);
			res.add(ap);
			return res;
		}	

		@Override
		public boolean isStochastic(){
			return false;
		}
	}
	
	public static class GoalEasttoGoal extends Policy{
		//Policy Mapping
		public Map<StateHashTuple, GroundedAction> map = new HashMap<StateHashTuple, GroundedAction>();

		/**
		 *Generates the Policy
		 */

		public GoalEasttoGoal(){
			map.put(new StateHashTuple(generateState(6,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(7,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(8,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(9,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(10,9,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //E
			map.put(new StateHashTuple(generateState(11,9,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //N
			map.put(new StateHashTuple(generateState(11,10,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //N
		}

		@Override
		/*
		 * Enters the map and returns the corresponding GroundedAction for the State
		 */
		public GroundedAction getAction(State s){
			return map.get(new StateHashTuple(s));
		}

		@Override
		/*
		 *Basic Action distribution. Since it's one action per state, it's 100% all of the time.
		 * @param State s
		 * @return ActionProb list
		 */
		public List<ActionProb> getActionDistributionForState(State s){
			GroundedAction selectedAction = this.getAction(s);
			List<ActionProb> res = new ArrayList<Policy.ActionProb>();
			ActionProb ap = new ActionProb(selectedAction, 1.0);
			res.add(ap);
			return res;
		}	

		@Override
		public boolean isStochastic(){
			return false;
		}
	}
	
	public static class GoalNorthtoGoal extends Policy{
		//Policy Mapping
		public Map<StateHashTuple, GroundedAction> map = new HashMap<StateHashTuple, GroundedAction>();

		/**
		 *Generates the Policy
		 */

		public GoalNorthtoGoal(){
			map.put(new StateHashTuple(generateState(9,5,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //n
			map.put(new StateHashTuple(generateState(9,6,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //n
			map.put(new StateHashTuple(generateState(9,7,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //n
			map.put(new StateHashTuple(generateState(9,8,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //n
			map.put(new StateHashTuple(generateState(9,9,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //n
			map.put(new StateHashTuple(generateState(9,10,11,11)), new GroundedAction(new NorthAction(ACTIONNORTH, DOMAIN, ""), "")); //n
			map.put(new StateHashTuple(generateState(9,11,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //e
			map.put(new StateHashTuple(generateState(10,11,11,11)), new GroundedAction(new EastAction(ACTIONEAST, DOMAIN, ""), "")); //e
		}

		@Override
		/*
		 * Enters the map and returns the corresponding GroundedAction for the State
		 */
		public GroundedAction getAction(State s){
			return map.get(new StateHashTuple(s));
		}

		@Override
		/*
		 *Basic Action distribution. Since it's one action per state, it's 100% all of the time.
		 * @param State s
		 * @return ActionProb list
		 */
		public List<ActionProb> getActionDistributionForState(State s){
			GroundedAction selectedAction = this.getAction(s);
			List<ActionProb> res = new ArrayList<Policy.ActionProb>();
			ActionProb ap = new ActionProb(selectedAction, 1.0);
			res.add(ap);
			return res;
		}	

		@Override
		public boolean isStochastic(){
			return false;
		}
	}
}
