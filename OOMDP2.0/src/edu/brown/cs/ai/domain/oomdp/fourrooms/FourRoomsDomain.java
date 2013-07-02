package edu.brown.cs.ai.domain.oomdp.fourrooms;

import java.util.List;
import java.util.Map;

import edu.umbc.cs.maple.domain.oomdp.DomainGenerator;
import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Attribute;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectClass;
import edu.umbc.cs.maple.oomdp.ObjectInstance;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.State;
import edu.umbc.cs.maple.oomdp.explorer.TerminalExplorer;
import edu.umbc.cs.maple.oomdp.explorer.VisualExplorer;
import edu.umbc.cs.maple.oomdp.visualizer.Visualizer;

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
	 * 			private List <ObjectClass> objectClasses_; //list of object classes
				private Map <String, ObjectClass> objectClassMap_; //look up object classes by name
				private List <Attribute> attributes_; //list of attributes
				private Map <String, Attribute>	attributeMap_; //lookup attributes by name
				private List <PropositionalFunction> propFunctions_; //list of propositional functions
				private Map <String, PropositionalFunction> propFunctionMap_; //lookup propositional functions by name
				private List <Action> actions_; //list of actions
				private Map <String, Action> actionMap_; //lookup actions by name
	 */
	public static Domain								DOMAIN = null;	
	
	public static void main(String[] args) {
		
		/*Creating an object called fourRoomsDomain()
		 * 	THis object has no constructor nor any attributes that would differ from each object		
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
		DOMAIN = new Domain();
		
		//Calls the function generateMap which in itself calls the functions
		//			1)frameMap()
		//			2)setStandardWalls()
		generateMap();
		
		/*Creates a new Attribute object
		 * 
		 */
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
	 */
	public static void setStandardWalls(){
		
		/*
		 * The horizontal wall is set to be at Map[1][6]
		 */
		horizontalWall(1, 1, 6);
		//Horizontal walls set at Map[3][6], Map[4][6], Map[5][6]
		horizontalWall(3, 5, 6);
		//Horizontal walls set at Map[7][5], Map[8][5]
		horizontalWall(7, 8, 5);
		//Horizontal walls set at Map[10][5], Map[11][5]
		horizontalWall(10, 11, 5);
		
		//Vertical walls set at Map[1][6]
		verticalWall(1, 1, 6);
		//Vertical walls set at Map[3][6], Map[4][6],Map[5][6],Map[6][6],Map[7][6],Map[8][6],
		verticalWall(3, 8, 6);
		verticalWall(10, 11, 6);
		
		
	}
	
	
	protected static void horizontalWall(int xi, int xf, int y){
		for(int x = xi; x <= xf; x++){
			MAP[x][y] = 1;
		}
	}
	
	protected static void verticalWall(int yi, int yf, int x){
		for(int y = yi; y <= yf; y++){
			MAP[x][y] = 1;
		}
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
			System.out.println("Action Performed: " + this.name_);
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
			System.out.println("Action Performed: " + this.name_);
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
			System.out.println("Action Performed: " + this.name_);
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
			System.out.println("Action Performed: " + this.name_);
			return st;
		}
		
		
	}
	
	
	
	
	public static class AtGoalPF extends PropositionalFunction{

		public AtGoalPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			
			ObjectInstance agent = st.getObject(params[0]);
			int ax = agent.getDiscValForAttribute(ATTX);
			int ay = agent.getDiscValForAttribute(ATTY);
			
			ObjectInstance goal = st.getObject(params[1]);
			int gx = goal.getDiscValForAttribute(ATTX);
			int gy = goal.getDiscValForAttribute(ATTY);
			
			if(ax == gx && ay == gy){
				return true;
			}
			
			return false;
		}
		
		
		
	}
	

}
