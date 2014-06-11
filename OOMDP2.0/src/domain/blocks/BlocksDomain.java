package domain.blocks;

import java.util.List;
import java.util.Map;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;


/**
 * 
 * @author Shawn
 * The blocks domain is a popular planning domain in artificial intelligence
 * There are a number of blocks on the table, which can be stacked on top each other.
 * You can only move a block that is clear (no blocks on top of it)
 */
public class BlocksDomain implements DomainGenerator {

	//Constants
	public static final String							ATTX = "xpos";
	public static final String							ATTY = "ypos";
	
	public static final String							CLASSCLAW = "claw";
	public static final String							CLASSBLOCK = "block";
	
	public static final String							ACTIONGRAB = "grab";
	public static final String							ACTIONDROP = "drop";
	public static final String							ACTIONLEFT = "left";
	public static final String							ACTIONRIGHT = "right";
	
	public static final String							PFON = "isOn";
	public static final String							PFCLEAR = "isClear";
	public static final String							PFHOLDING = "isHolding";
	
	public static final int								TABLESPOTS = 12;
	public static final int								NUMBLOCKS = 4;
	
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
		BlocksDomain frd = new BlocksDomain();
		
		/*Generates a domain by calling the function generateDomain()
		 * 		
		 * 
		 */
		Domain d = frd.generateDomain();
		
		State s = BlocksDomain.getCleanState();
		setBlock(s, 0, 1);
		setBlock(s, 1, 2);
		setBlock(s, 2, 2);
		setBlock(s, 3, 4);
		setClaw(s, 5);
		
		
		int expMode = 1;
		if(args.length > 0){
			if(args[0].equals("v")){
				expMode = 1;
			}
		}
		
		if(expMode == 0){
			TerminalExplorer exp = new TerminalExplorer(d);
			exp.addActionShortHand("l", ACTIONLEFT);
			exp.addActionShortHand("r", ACTIONRIGHT);
			exp.addActionShortHand("g", ACTIONGRAB);
			exp.addActionShortHand("d", ACTIONDROP);
			
			exp.exploreFromState(s);
		}
		else if(expMode == 1){
			
			Visualizer v = BlocksVisualizer.getVisualizer();
			VisualExplorer exp = new VisualExplorer(d, v, s);
			
			exp.addKeyAction("w", ACTIONGRAB);
			exp.addKeyAction("s", ACTIONDROP);
			exp.addKeyAction("a", ACTIONLEFT);
			exp.addKeyAction("d", ACTIONRIGHT);
			
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
		
		generateMap();
		
		//Creates a new Attribute object
		Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
		xatt.setDiscValuesForRange(0, TABLESPOTS, 1);
		
		Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
		yatt.setDiscValuesForRange(0, NUMBLOCKS + 1, 1);
		
		ObjectClass blockClass = new ObjectClass(DOMAIN, CLASSBLOCK);
		blockClass.addAttribute(xatt);
		blockClass.addAttribute(yatt);

		ObjectClass clawClass = new ObjectClass(DOMAIN, CLASSCLAW);
		clawClass.addAttribute(xatt);
		clawClass.addAttribute(yatt);

		Action left = new LeftAction(ACTIONLEFT, DOMAIN, "");
		Action right = new RightAction(ACTIONRIGHT, DOMAIN, "");
		Action grab = new GrabAction(ACTIONGRAB, DOMAIN, "");
		Action drop = new DropAction(ACTIONDROP, DOMAIN, "");
		
		PropositionalFunction on = new OnPF(PFON, DOMAIN, new String[]{CLASSBLOCK, CLASSBLOCK});
		PropositionalFunction clear = new ClearPF(PFCLEAR, DOMAIN, new String[]{CLASSBLOCK});
		PropositionalFunction holding = new HoldingPF(PFHOLDING, DOMAIN, new String[]{CLASSCLAW, CLASSBLOCK});
		
		return DOMAIN;
	}

	
	public static State getCleanState(){
		
		BlocksDomain frd = new BlocksDomain();
		frd.generateDomain();
		
		State s = new State();
		
		s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSCLAW), CLASSCLAW+0));
		for(int i = 0; i < NUMBLOCKS; i++) {
			s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSBLOCK), CLASSBLOCK+i));
		}
		
		return s;
	}
	
	/**
	 * Sets the agent's position to the given coordinates in a states
	 * @param s the state to modify
	 * @param x the new x coordinate of the agent
	 * @param y the new y coordinate of the agent
	 */
	public static void setBlock(State s, int block_num, int x){
		ObjectInstance block = s.getObjectsOfTrueClass(CLASSBLOCK).get(block_num);
		int y = 0;
		for(ObjectInstance b : s.getObjectsOfTrueClass(CLASSBLOCK)) {
			try {
				if(b.getDiscValForAttribute(ATTX) == x) {
					y++;
				}
			} catch(burlap.oomdp.core.values.UnsetValueException uve) { }
		}
		block.setValue(ATTX, x);
		block.setValue(ATTY, y);
	}

	public static void setClaw(State s, int x) {
		ObjectInstance claw = s.getObjectsOfTrueClass(CLASSCLAW).get(0);
		claw.setValue(ATTX, x);
		claw.setValue(ATTY, NUMBLOCKS+1);
	}
	
	public static void generateMap(){
		
		//Initializes the map two-dimensional array to be [13][13]
		MAP = new int[TABLESPOTS][NUMBLOCKS]; //+1 to handle zero base
		
	}
	
	/**
	 * Attempts to move the block to a new slot, assuming it is open
	 * This is the helper function for MoveAction
	 * @param s the current state
	 * @param b the  block number to move
	 * @param slot the attempted new slot of the block
	 */
	public static void moveClaw(State s, int direction){
		ObjectInstance claw = s.getObjectsOfTrueClass(CLASSCLAW).get(0);
		ObjectInstance clawBlock = null;
		int x = claw.getDiscValForAttribute(ATTX);

		for(ObjectInstance b : s.getObjectsOfTrueClass(CLASSBLOCK)) {
			if(b.getDiscValForAttribute(ATTX) == x && b.getDiscValForAttribute(ATTY) == claw.getDiscValForAttribute(ATTY)) {
				clawBlock = b;
			}
		}

		int newPos = Math.max(Math.min(x + direction, TABLESPOTS - 1), 0);
		if(clawBlock != null) {
			clawBlock.setValue(ATTX, newPos);
		}
		claw.setValue(ATTX, newPos);
	}

	public static void grab(State s) {
		ObjectInstance claw = s.getObjectsOfTrueClass(CLASSCLAW).get(0);
		int x = claw.getDiscValForAttribute(ATTX);
		ObjectInstance topBlock = null;
		for(ObjectInstance b : s.getObjectsOfTrueClass(CLASSBLOCK)) {
			if(b.getDiscValForAttribute(ATTX) == x) {
				if(topBlock == null || b.getDiscValForAttribute(ATTY) > topBlock.getDiscValForAttribute(ATTY)) {
					topBlock = b;
				}
			}
		}

		if(topBlock != null) {
			topBlock.setValue(ATTY, claw.getDiscValForAttribute(ATTY));
		}
	}
	

	public static void drop(State s) {
		ObjectInstance claw = s.getObjectsOfTrueClass(CLASSCLAW).get(0);
		ObjectInstance clawBlock = null;
		int x = claw.getDiscValForAttribute(ATTX);
		int y = 0;
		for(ObjectInstance b : s.getObjectsOfTrueClass(CLASSBLOCK)) {
			if(b.getDiscValForAttribute(ATTX) == x && b.getDiscValForAttribute(ATTY) == claw.getDiscValForAttribute(ATTY)) {
				clawBlock = b;
			} else if(b.getDiscValForAttribute(ATTX) == x) {
				y++;
			}
		}

		if(clawBlock != null) {
			clawBlock.setValue(ATTY, y);
		}
	}
	
	public static class RightAction extends Action{

		public RightAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			moveClaw(st, 1);
			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	public static class LeftAction extends Action{

		public LeftAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			moveClaw(st, -1);
			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}

	public static class GrabAction extends Action {

		public GrabAction(String name, Domain domain, String parameterClasses){
			super(name, domain, parameterClasses);
		}
		
		@Override
		protected State performActionHelper(State st, String[] params) {
			grab(st);
			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}

	public static class DropAction extends Action {
		public DropAction(String name, Domain domain, String parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		protected State performActionHelper(State st, String[] params) {
			drop(st);
			System.out.println("Action Performed: " + this.name);
			return st;
		}
	}
	
	
	public static class OnPF extends PropositionalFunction{

		public OnPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance b1 = st.getObject(params[0]);
			ObjectInstance b2 = st.getObject(params[1]);

			return (b1.getDiscValForAttribute(ATTX) == b2.getDiscValForAttribute(ATTX) &&
					b1.getDiscValForAttribute(ATTY) == b2.getDiscValForAttribute(ATTY) + 1);
		}
	}
	
	public static class HoldingPF extends PropositionalFunction{

		public HoldingPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance claw = st.getObject(params[0]);
			ObjectInstance block = st.getObject(params[1]);

			return (claw.getDiscValForAttribute(ATTX) == block.getDiscValForAttribute(ATTX) &&
					claw.getDiscValForAttribute(ATTY) == block.getDiscValForAttribute(ATTY));
		}
	}

	public static class ClearPF extends PropositionalFunction{

		public ClearPF(String name, Domain domain, String[] parameterClasses) {
			super(name, domain, parameterClasses);
		}

		@Override
		public boolean isTrue(State st, String[] params) {
			ObjectInstance block = st.getObject(params[0]);

			for(ObjectInstance b2 : st.getObjectsOfTrueClass(CLASSBLOCK)) {
				if(block.getDiscValForAttribute(ATTX) == b2.getDiscValForAttribute(ATTX) &&
						block.getDiscValForAttribute(ATTY) + 1 == b2.getDiscValForAttribute(ATTY)) {
					return  false;
				}
			}
			return true;
		}
	}
}
