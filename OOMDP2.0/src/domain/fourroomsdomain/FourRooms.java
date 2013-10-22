package domain.fourroomsdomain;

import java.util.*;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.options.PrimitiveOption;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

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
	public static final double LAMBDA = 1.0;
	public static Map<StateHashTuple, List<QAction>> qVals = new HashMap<StateHashTuple, List<QAction>>();
	public static LearningAgent Q, S;
	public static OOMDPPlanner planner;
	public static EpisodeAnalysis analyzer;
	public static FourRoomsStateParser parser;
	public static RewardFunction rf;
	public static TerminalFunction tf;

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
		int expMode = 2;

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

		}else if(expMode == 2){
			//Runs the simulator via text output
			parser = new FourRoomsStateParser();

			for(int i = 1; i <= 100; i++){
				analyzer = new EpisodeAnalysis();

				System.out.print("Episode " + i + ": ");
				analyzer = Q.runLearningEpisodeFrom(s);
				System.out.println("\tSteps: " + analyzer.numTimeSteps());
				analyzer.writeToFile(String.format("output/e%03d", i), parser);

				setAgent(s, 1, 1);
				setGoal(s, 11, 11);
			}

			//Visualize the Steps
			Visualizer v = FourRoomsVisual.getVisualizer();
			EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, d, parser, "output");

		}else if(expMode == 3){
			parser = new FourRoomsStateParser();
			
			for(int i=1; i <=100; i++){
				System.out.print("Episode " + i + ": ");
				analyzer = S.runLearningEpisodeFrom(s);
				System.out.println("\tSteps: " + analyzer.numTimeSteps());
				analyzer.writeToFile(String.format("output/e%03d", i), parser);

				setAgent(s, 1, 1);
				setGoal(s, 11, 11);
			}
			
			//Visualize the Steps
			Visualizer v = FourRoomsVisual.getVisualizer();
			EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, d, parser, "output");
			
		}else if(expMode == 4){
			parser = new FourRoomsStateParser();
			
			//Running the Value Iteration Planner
			planner.planFromState(s);
			Policy p = new GreedyQPolicy((QComputablePlanner)planner);
			p.evaluateBehavior(s, rf, tf).writeToFile("output/OOMDP_Planner", parser);
			
			s = FourRooms.getCleanState();
			
			//Run Q-Learning and Generate Episode
			for(int i = 0; i < 100; i++){
				analyzer = Q.runLearningEpisodeFrom(s);
				setAgent(s,1,1);
				setGoal(s,11,11);
			}
			
			analyzer.writeToFile("output/Q-Learning", parser);
			s = FourRooms.getCleanState();
			System.out.println("Finished Q-Learning Analysis");
			
			//Run SARSA-LAM and Generate Episode
			for(int i = 0; i < 100; i++){
				analyzer = S.runLearningEpisodeFrom(s);
				setAgent(s,1,1);
				setGoal(s,11,11);
			}
			
			analyzer.writeToFile("output/Sarsa-Lambda", parser);
			s = FourRooms.getCleanState();
			System.out.println("Finished Sarsa-Lambda Anaylsis");
			
			//Visualize the Steps
			Visualizer v = FourRoomsVisual.getVisualizer();
			EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, d, parser, "output");
		}else if(expMode == 5){
			parser = new FourRoomsStateParser();
			
			//Bring up the visualizer
			Visualizer v = FourRoomsVisual.getVisualizer();
			new EpisodeSequenceVisualizer(v, d, parser, "output");
		}
	}

	

	@Override
	public Domain generateDomain(){		
		if(DOMAIN != null)
			return DOMAIN;

		//otherwise create the domain data
		DOMAIN = new SADomain();
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


		PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, new String[]{CLASSAGENT, CLASSGOAL});
		DOMAIN.addPropositionalFunction(atGoal);

		rf = new SingleGoalPFRF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));
		tf = new SinglePFTF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));

		DiscreteStateHashFactory hashFactory = new DiscreteStateHashFactory();
		hashFactory.setAttributesForClass(CLASSAGENT, DOMAIN.getObjectClass(CLASSAGENT).attributeList);
		Q = new QLearning(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.2, FourRooms.LEARNINGRATE, Integer.MAX_VALUE);
		S = new SarsaLam(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.2, FourRooms.LEARNINGRATE, FourRooms.LAMBDA);
		planner = new ValueIteration(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.001, 100);
		
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

	

}
