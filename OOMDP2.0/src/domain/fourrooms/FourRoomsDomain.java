package domain.fourrooms;

import java.util.List;
import java.util.Map;

import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.DomainGenerator;
import oomdptb.oomdp.ObjectClass;
import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.SADomain;
import oomdptb.oomdp.State;
import oomdptb.oomdp.explorer.TerminalExplorer;
import oomdptb.oomdp.explorer.VisualExplorer;
import oomdptb.oomdp.visualizer.Visualizer;

/**
 * 
 * @author James
 * The four rooms domains is a basic block world domain in which the agent has to navigate to the goal square.
 * This class should be used as a basis for anyone interested in developing a more complicated grid world domain.
 */
public class FourRoomsDomain implements DomainGenerator {

	//Constants
	public static final String							ATTX = "x";
	public static final String							ATTY = "y";
	
	public static final String							CLASSAGENT = "agent";
	public static final String							CLASSGOAL = "goal";
	
	public static final String							ACTIONNORTH = "north";
	public static final String							ACTIONSOUTH = "south";
	public static final String							ACTIONEAST = "east";
	public static final String							ACTIONWEST = "west";
	
	public static final String							PFATGOAL = "atGoal";
	
	public static final int								MAXX = 12;
	public static final int								MAXY = 12;
	
	public static int[][]								MAP;
	
	/*A variable of type Domain
	 * The attributes of the Domain class are:
	 *private List <ObjectClass>						objectClasses;			//list of object classes
	 *private Map <String, ObjectClass>				objectClassMap;		//look up object classes by name
	 *
	 *private List <Attribute>						attributes;			//list of attributes
	 *private Map <String, Attribute>					attributeMap;			//lookup attributes by name
	 *
	 *private List <PropositionalFunction>			propFunctions;			//list of propositional functions
	 *private Map <String, PropositionalFunction> 	propFunctionMap;		//lookup propositional functions by name
	 *
	 *private List <Action>							actions;				//list of actions
	 *private Map <String, Action>					actionMap;				//lookup actions by name
	 */
	public static Domain								DOMAIN = null;	
	
	public static void main(String[] args) {
		
		/*Creating an object called fourRoomsDomain()
		 * 	This object has no constructor nor any attributes that would differ from each object		
		*/
		FourRoomsDomain frd = new FourRoomsDomain();
		
		/*Generates a domain by calling the function generateDomain()
		 * 		
		 * 
		 */
		Domain d = frd.generateDomain();
		
		State s = FourRoomsDomain.getCleanState();
		setAgent(s, 1, 1);
		setGoal(s, 5, 5);
		
		
		int expMode = 1;
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
			
		}
		else if(expMode == 1){
			
			Visualizer v = FourRoomsVisualizer.getVisualizer();
			VisualExplorer exp = new VisualExplorer(d, v, s);
			
			//use w-s-a-d-x
			exp.addKeyAction("w", ACTIONNORTH);
			exp.addKeyAction("s", ACTIONSOUTH);
			exp.addKeyAction("a", ACTIONWEST);
			exp.addKeyAction("d", ACTIONEAST);
			
			exp.initGUI();
		}
		
	}
	
	
	@Override
	@SuppressWarnings(value = { "unused" })
	public Domain generateDomain() {
		
		if(DOMAIN != null){
			return DOMAIN;
		}
		
		//otherwise create the domain data
		//Starts with all the attributes inititalized
		DOMAIN = new SADomain();
		
		//Calls the function generateMap which in itself calls the functions
		//			1)frameMap()
		//			2)setStandardWalls()
		generateMap();
		
		//Creates a new Attribute object
		Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, MAXX, 1);
		
		Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, MAXY, 1);
		
		
		ObjectClass agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
		agentClass.addAttribute(xatt);
		agentClass.addAttribute(yatt);
		
		ObjectClass goalClass = new ObjectClass(DOMAIN, CLASSGOAL);
		goalClass.addAttribute(xatt);
		goalClass.addAttribute(yatt);
		
		Action north = new NorthAction(ACTIONNORTH, DOMAIN, "");
		Action south = new SouthAction(ACTIONSOUTH, DOMAIN, "");
		Action east = new EastAction(ACTIONEAST, DOMAIN, "");
		Action west = new WestAction(ACTIONWEST, DOMAIN, "");
		
		PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, new String[]{CLASSAGENT, CLASSGOAL});
		
		return DOMAIN;
	}

	
	public static State getCleanState(){
		
		FourRoomsDomain frd = new FourRoomsDomain();
		frd.generateDomain();
		
		State s = new State();
		
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSAGENT), CLASSAGENT+0));
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSGOAL), CLASSGOAL+0));
		
		return s;
	}
	
	/**
	 * Sets the agent's position to the given coordinates in a states
	 * @param s the state to modify
	 * @param x the new x coordinate of the agent
	 * @param y the new y coordinate of the agent
	 */
	public static void setAgent(State s, int x, int y){
		ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
		agent.setValue(ATTX, x);
		agent.setValue(ATTY, y);
	}
	
	/**
	 * Sets the goal's position to the given coordinates in a states
	 * @param s the state to modify
	 * @param x the new x coordinate of the goal
	 * @param y the new y coordinate of the goal
	 */
	public static void setGoal(State s, int x, int y){
		ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(0);
		goal.setValue(ATTX, x);
		goal.setValue(ATTY, y);
	}
	
	
	public static void generateMap(){
		
		//Initializes the map two-dimensional array to be [13][13]
		MAP = new int[MAXX+1][MAXY+1]; //+1 to handle zero base
		
		frameMap();
		setStandardWalls();
		
	}
	
	/**Makes a big box with the walls being set to the value of 1
	 * So, (0,0) is one point in th Map and since it is the main frame, the value is set to 1
	 */
	public static void frameMap(){
		
		for(int x = 0; x <= MAXX; x++){
			for(int y = 0; y <= MAXY; y++){
				
				if(x == 0 || x == MAXX || y == 0 || y == MAXY){
					MAP[x][y] = 1;
				}
				else{
					MAP[x][y] = 0;
				}
			}
		}
	}
	
	
	/**
	 * Creates horizontal and vertical walls within the bigger box created in frameMap()
	 * see the documentation for horizontalWall() and verticalWall() for how to modify this
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
	 * Creates a horizontal wall
	 * @param xi The starting x coordinate of the wall
	 * @param xf The ending x coordinate of the wall
	 * @param y The y coordinate of the wall
	 */
	protected static void horizontalWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++){
			MAP[x][y] = 1;
		}
	}
	
	/**
	 * Creates a horizontal wall
	 * @param yi The stating y coordinate of the wall
	 * @param yf The ending y coordinate of the wall
	 * @param x	The x coordinate of the wall
	 */
	protected static void verticalWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++){
			MAP[x][y] = 1;
		}
	}
	
	/**
	 * Attempts to move the agent into the given position, taking into account walls and blocks
	 * This is a helper function for the North, South, East, and West actions.
	 * @param the current state
	 * @param the attempted new X position of the agent
	 * @param the attempted new Y position of the agent
	 */
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
			System.out.println("Action Performed: " + this.name);
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
			System.out.println("Action Performed: " + this.name);
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
			System.out.println("Action Performed: " + this.name);
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
			System.out.println("Action Performed: " + this.name);
			return st;
		}	
	}
	
	/**
	 * 
	 * @author James
	 * Propositional function for determining whether the agent is in the same position as the goal.
	 */
	public static class AtGoalPF extends PropositionalFunction{

		public AtGoalPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			
			//get the agent coordinates
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			
			ObjectInstance goal = st.getObject(params[1]);
			
			//get the goal coordinates
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			
			if(ax == gx && ay == gy){
				return true;
			}
			
			return false;
		}	
	}
}
