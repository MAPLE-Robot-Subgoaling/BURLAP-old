package domain.taxiworld;

import java.util.*;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.PrimitiveOption;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.visualizer.Visualizer;

/**
 * @author Nicholas Haltmeyer
 * Class definition for the taxi world domain
 * Allows for generation of a taxi world domain with any number of passengers
 * Based off of the four rooms domain
 */
public class TaxiWorldDomain implements DomainGenerator {
    public static final String ATTX = "x";
    public static final String ATTY = "y";
    public static final String ATTCARRIED = "being carried";
    public static final String ATTCARRY = "carrying";
    public static final String ATTDROPPED = "dropped off";
    public static final String ATTLEFT = "passengers left";

    public static final String CLASSAGENT = "agent";
    public static final String CLASSGOAL = "goal";
    public static final String CLASSPASS = "passenger ";

    public static final String ACTIONNORTH = "north";
    public static final String ACTIONSOUTH = "south";
    public static final String ACTIONEAST = "east";
    public static final String ACTIONWEST = "west";
    public static final String ACTIONPICKUP = "pick up";
    public static final String ACTIONDROPOFF = "drop off";

    public static final String PFATGOAL = "atGoal";	
    public static final String PFATPASS = "atPassenger";
    public static final String PFATFINISH = "atFinish";

    public static final int	MAXX = 16;
    public static final int	MAXY = 11;
    // Using Integer instead of int because if not set, will throw a NullPointerException
    public static Integer MAXPASS;

    public static int[][] MAP;
    public static boolean MAPGENERATED = false;
    public static Domain DOMAIN = null;

    public static final double LEARNINGRATE = 0.99;
    public static final double DISCOUNTFACTOR = 0.95;
    public static final double LAMBDA = 1.0;
    public static final double GAMMA = 0.2;
    public static RewardFunction rf;
    public static TerminalFunction tf;

    /**
     * Drives the learning
     * @param args none
     */
    public static void main(String[] args) {
        // Set the total number of passengers to be used in the domain
        MAXPASS = 2;

        TaxiWorldDomain txd = new TaxiWorldDomain();
        Domain d = txd.generateDomain();
        State s = getCleanState();
        TaxiWorldStateParser parser = new TaxiWorldStateParser();
        DiscreteStateHashFactory hashFactory = new DiscreteStateHashFactory();
        hashFactory.setAttributesForClass(CLASSAGENT, DOMAIN.getObjectClass(CLASSAGENT).attributeList);
        QLearning Q = new QLearning(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, GAMMA, LEARNINGRATE, Integer.MAX_VALUE);

        int[][] passPos = getRandomSpots(MAXPASS);
        // int[][] passPos = {
        //     {1, 1}
        // };
        for (int i = 1; i <= 1000; i++) {
            setAgent(s, 4, 5);
            setGoal(s, 4, 5);
            
            for (int j = 1; j <= MAXPASS; j++) {
                setPassenger(s, j, passPos[j - 1][0], passPos[j - 1][1]);
            }

            EpisodeAnalysis analyzer = new EpisodeAnalysis();
            System.out.print("Episode " + i + ": ");
            analyzer = Q.runLearningEpisodeFrom(s);
            System.out.println("\tSteps: " + analyzer.numTimeSteps());
            analyzer.writeToFile(String.format("output/e%03d", i), parser);
        }

        Visualizer v = TaxiWorldVisualizer.getVisualizer();
        new EpisodeSequenceVisualizer(v, d, parser, "output");
    }

    /**
     * Returns random open spots on the map, bounded by max
     * @param max the maximum number of passengers to be generated
     * @return 2-d array of open spots on the map
     */
    public static int[][] getRandomSpots(int max) {
        Random rand = new Random();
        int[][] spots = new int[max][2];
        ArrayList<Integer[]> open = getOpenSpots();

        for (int i = 0; i < max; i++) {
            Integer[] temp = open.get(rand.nextInt(open.size()));
            spots[i] = new int[] {(int) temp[0], (int) temp[1]};
        }

        return spots;
    }

    /**
     * Returns all empty spots on the map
     * @return an ArrayList of all empty spots in the form int[] {x, y}
     */
    public static ArrayList<Integer[]> getOpenSpots() {
        if (!MAPGENERATED) {
            generateMap();
        }

        ArrayList<Integer[]> openSpots = new ArrayList<Integer[]>();
        for(int x = 0; x <= MAXX; x++) {
            for(int y = 0; y <= MAXY; y++) {
                if (MAP[x][y] == 0) {
                    openSpots.add(new Integer[] {x, y});
                }
            }
        }

        return openSpots;
    }

    /**
     * Concatenates an arbitrary number of arrays
     * @param first the first array in the ordering
     * @param rest all of the other arrays in the ordering
     * @return a concatenation of all arrays passed
     */
    @SafeVarargs
	public static <T> T[] arrConcat(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array: rest) {
            totalLength += array.length;
        }

        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;

        for (T[] array: rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }

    @Override
    public Domain generateDomain() {        
        if (MAXPASS == null) {
        	throw new RuntimeException("Maximum number of passengers not set; set MAXPASS.");
        }

        DOMAIN = new SADomain();
        generateMap();

        Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
        xatt.setDiscValuesForRange(0, MAXX, 1);
        Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
        yatt.setDiscValuesForRange(0, MAXY, 1);
        Attribute cyatt = new Attribute(DOMAIN, ATTCARRY, Attribute.AttributeType.DISC);
        cyatt.setDiscValuesForRange(0, 1, MAXPASS);
        Attribute cdatt = new Attribute(DOMAIN, ATTCARRIED, Attribute.AttributeType.DISC);
        cdatt.setDiscValuesForRange(0, 1, 1);
        Attribute datt = new Attribute(DOMAIN, ATTDROPPED, Attribute.AttributeType.DISC);
        datt.setDiscValuesForRange(0, 1, 1);
        Attribute latt = new Attribute(DOMAIN, ATTLEFT, Attribute.AttributeType.DISC);
        latt.setDiscValuesForRange(0, MAXPASS, 1);

        DOMAIN.addAttribute(xatt);
        DOMAIN.addAttribute(yatt);
        DOMAIN.addAttribute(cyatt);
        DOMAIN.addAttribute(cdatt);
        DOMAIN.addAttribute(datt);
        DOMAIN.addAttribute(latt);

        ObjectClass agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
        agentClass.addAttribute(xatt);
        agentClass.addAttribute(yatt);
        agentClass.addAttribute(cyatt);

        ObjectClass goalClass = new ObjectClass(DOMAIN, CLASSGOAL);
        goalClass.addAttribute(xatt);
        goalClass.addAttribute(yatt);
        goalClass.addAttribute(latt);

        String[] classPassArr = new String[MAXPASS];
        for (int i = 1; i <= MAXPASS; i++) {
            ObjectClass passClass = new ObjectClass(DOMAIN, CLASSPASS + i);
            passClass.addAttribute(xatt);
            passClass.addAttribute(yatt);
            passClass.addAttribute(cdatt);
            passClass.addAttribute(datt);
            DOMAIN.addObjectClass(passClass);
            classPassArr[i - 1] = CLASSPASS + i;
        }

        DOMAIN.addObjectClass(goalClass);
        DOMAIN.addObjectClass(agentClass);

        Action north = new PrimitiveOption(new NorthAction(ACTIONNORTH, DOMAIN, ""));
        Action south = new PrimitiveOption(new SouthAction(ACTIONSOUTH, DOMAIN, ""));
        Action east = new PrimitiveOption(new EastAction(ACTIONEAST, DOMAIN, ""));
        Action west = new PrimitiveOption(new WestAction(ACTIONWEST, DOMAIN, ""));
        Action pickUp = new PrimitiveOption(new PickUpAction(ACTIONPICKUP, DOMAIN, ""));
        Action dropOff = new PrimitiveOption(new DropOffAction(ACTIONDROPOFF, DOMAIN, ""));

        DOMAIN.addAction(north);
        DOMAIN.addAction(south);
        DOMAIN.addAction(east);
        DOMAIN.addAction(west);
        DOMAIN.addAction(pickUp);
        DOMAIN.addAction(dropOff);

        PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, arrConcat(new String[] {
            CLASSAGENT,
            CLASSGOAL
        }, classPassArr));
        PropositionalFunction atPassenger = new AtPassPF(PFATPASS, DOMAIN, arrConcat(new String[] {
            CLASSAGENT,
            CLASSGOAL
        }, classPassArr));
        PropositionalFunction atFinish = new AtFinishPF(PFATFINISH, DOMAIN, arrConcat(new String[] {
            CLASSAGENT,
            CLASSGOAL
        }, classPassArr));

        DOMAIN.addPropositionalFunction(atGoal);
        DOMAIN.addPropositionalFunction(atPassenger);
        DOMAIN.addPropositionalFunction(atFinish);

        rf = new SingleGoalPFRF(DOMAIN.getPropFunction(PFATFINISH));
        tf = new SinglePFTF(DOMAIN.getPropFunction(PFATFINISH));

        /*DiscreteStateHashFactory hashFactory = new DiscreteStateHashFactory();
        hashFactory.setAttributesForClass(CLASSAGENT, DOMAIN.getObjectClass(CLASSAGENT).attributeList);
        Q = new QLearning(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, 0.2, LEARNINGRATE, Integer.MAX_VALUE);
        S = new SarsaLam(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, 0.2, LEARNINGRATE, LAMBDA);
        planner = new ValueIteration(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, 0.001, 100);*/

        return DOMAIN;
    }

    /**
     * Get a clean state
     * @return a clean state s for the domain
     */
    public static State getCleanState() {	
        State s = new State();

        s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSGOAL), CLASSGOAL));
        s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSAGENT), CLASSAGENT));

        for (int i = 1; i <= MAXPASS; i++) {
            s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSPASS + i), CLASSPASS + i));
        }

        return s;
    }

    /**
     * Sets the agent to position (x, y)
     * @param s the current state
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void setAgent(State s, int x, int y) {
        ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
        agent.setValue(ATTX, x);
        agent.setValue(ATTY, y);
        agent.setValue(ATTCARRY, 0);
    }

    /**
     * Sets the goal to position (x, y)
     * @param s the current state
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void setGoal(State s, int x, int y) {
        ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(0);
        goal.setValue(ATTX, x);
        goal.setValue(ATTY, y);
        goal.setValue(ATTLEFT, MAXPASS);
    }

    /**
     * Sets the passenger num to the coordinate (x, y)
     * @param s the current state
     * @param num the passenger number
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void setPassenger(State s, int num, int x, int y) {
        if (num > MAXPASS || num < 1) {
            throw new IllegalArgumentException("Invalid passenger number");
        }

        ObjectInstance pass = s.getObjectsOfTrueClass(CLASSPASS + num).get(0);

        pass.setValue(ATTX, x);
        pass.setValue(ATTY, y);
        pass.setValue(ATTCARRIED, 0);
        pass.setValue(ATTDROPPED, 0);
    }

    /**
     * Generates the map
     */
    public static void generateMap() {
        MAP = new int[MAXX + 1][MAXY + 1];
        frameMap();
        setStandardWalls();
        MAPGENERATED = true;
    }

    /**
     * Sets the outside frame of the map
     */
    public static void frameMap() {
        for(int x = 0; x <= MAXX; x++) {
            for(int y = 0; y <= MAXY; y++) {
                if(x == 0 || x == MAXX || y == 0 || y == MAXY) {
                    MAP[x][y] = 1;
                } else {
                    MAP[x][y] = 0;
                }
            }
        }
    }

    /**
     * Sets the standard wall positions
     */
    public static void setStandardWalls() {
        verticalWall(1, 4, 2);
        verticalWall(1, 4, 6);
        verticalWall(1, 4, 12);
        verticalWall(7, 10, 4);
        verticalWall(5, 8, 9);
        verticalWall(7, 10, 12);
    }

    /**
     * Creates a horizontal wall for given values
     * @param xi initial x coordinate
     * @param xf final x coordinate
     * @param y y coordinate
     */
    protected static void horizontalWall(int xi, int xf, int y) {
        for(int x = xi; x <= xf; x++) {
            MAP[x][y] = 1;
        }
    }

    /**
     * Creates a vertical wall for given values
     * @param yi initial y coordinate
     * @param yf final y coordinate
     * @param x x coordinate
     */
    protected static void verticalWall(int yi, int yf, int x) {
        for(int y = yi; y <= yf; y++) {
            MAP[x][y] = 1;
        }
    }

    /**
     * Attempts to move the agent
     * @param s the current state
     * @param xd the x coordinate of the destination
     * @param yd the y coordinate of the destination
     */
    public static void move(State s, int xd, int yd) {
        ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
        int ax = agent.getDiscValForAttribute(ATTX);
        int ay = agent.getDiscValForAttribute(ATTY);
        int nx = ax+xd;
        int ny = ay+yd;

        if(MAP[nx][ny] == 1) {
            nx = ax;
            ny = ay;
        }

        int passNum = agent.getDiscValForAttribute(ATTCARRY);

        if (passNum != 0) {
            ObjectInstance pass = s.getObjectsOfTrueClass(CLASSPASS + passNum).get(0);
            pass.setValue(ATTX, nx);
            pass.setValue(ATTY, ny);
        }

        agent.setValue(ATTX, nx);
        agent.setValue(ATTY, ny);
    }
    
    /**
     * Attempts to pick up a passenger
     * @param s the current state
     */
    public static void pickUp(State s) {
        ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
        int ax = agent.getDiscValForAttribute(ATTX);
        int ay = agent.getDiscValForAttribute(ATTY);

        for (int i = 1; i <= MAXPASS; i++) {
            ObjectInstance pass = s.getObjectsOfTrueClass(CLASSPASS + i).get(0);
            int px = pass.getDiscValForAttribute(ATTX);
            int py = pass.getDiscValForAttribute(ATTY);

            if (agent.getDiscValForAttribute(ATTCARRY) == 0 &&
                pass.getDiscValForAttribute(ATTDROPPED) == 0 &&
                ax == px && ay == py) {
                agent.setValue(ATTCARRY, i);
                pass.setValue(ATTCARRIED, 1);
                break;
            }
        }
    }

    /**
     * Attempts to drop off a passenger
     * @param s the current state
     */
    public static void dropOff(State s) {
        ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
        int ax = agent.getDiscValForAttribute(ATTX);
        int ay = agent.getDiscValForAttribute(ATTY);

        ObjectInstance goal = s.getObjectsOfTrueClass(CLASSGOAL).get(0);
        int gx = goal.getDiscValForAttribute(ATTX);
        int gy = goal.getDiscValForAttribute(ATTY);
        int passNum = agent.getDiscValForAttribute(ATTCARRY);

        if (passNum == 0) {
            return;
        }
        ObjectInstance pass = s.getObjectsOfTrueClass(CLASSPASS + passNum).get(0);

        if (pass.getDiscValForAttribute(ATTCARRIED) == 1 &&
            agent.getDiscValForAttribute(ATTCARRY) != 0 &&
            ax == gx && ay == gy) {
            agent.setValue(ATTCARRY, 0);
            pass.setValue(ATTDROPPED, 1);
            pass.setValue(ATTCARRIED, 0);
            goal.setValue(ATTLEFT, goal.getDiscValForAttribute(ATTLEFT) - 1);
        }
    }

    /**
     * Defines the action of moving north
     */
    public static class NorthAction extends Action {
        /**
         * Constructs the action
         * @param name name of the action
         * @param domain domain tied to the action
         * @param parameterClasses string of parameters
         */
        public NorthAction(String name, Domain domain, String parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        protected State performActionHelper(State st, String[] params) {
            move(st, 0, 1);
            return st;
        }
    }

    /**
     * Defines the action of moving south
     */
    public static class SouthAction extends Action {
        /**
         * Constructs the action
         * @param name name of the action
         * @param domain domain tied to the action
         * @param parameterClasses string of parameters
         */
        public SouthAction(String name, Domain domain, String parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        protected State performActionHelper(State st, String[] params) {
            move(st, 0, -1);
            return st;
        }
    }

    /**
     * Defines the action of moving east
     */
    public static class EastAction extends Action{
        /**
         * Constructs the action
         * @param name name of the action
         * @param domain domain tied to the action
         * @param parameterClasses string of parameters
         */
        public EastAction(String name, Domain domain, String parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        protected State performActionHelper(State st, String[] params) {
            move(st, 1, 0);
            return st;
        }
    }

    /**
     * Defines the action of moving west
     */
    public static class WestAction extends Action {
        /**
         * Constructs the action
         * @param name name of the action
         * @param domain domain tied to the action
         * @param parameterClasses string of parameters
         */
        public WestAction(String name, Domain domain, String parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        protected State performActionHelper(State st, String[] params) {
            move(st, -1, 0);
            return st;
        }
    }

    /**
     * Defines the action of picking up a passenger
     */
    public static class PickUpAction extends Action {
        /**
         * Constructs the action
         * @param name name of the action
         * @param domain domain tied to the action
         * @param parameterClasses string of parameters
         */
        public PickUpAction(String name, Domain domain, String parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        protected State performActionHelper(State st, String[] params) {
            pickUp(st);
            return st;
        }
    }

    /**
     * Defines the action of dropping off a passenger
     */
    public static class DropOffAction extends Action {
        /**
         * Constructs the action
         * @param name name of the action
         * @param domain domain tied to the action
         * @param parameterClasses string of parameters
         */
        public DropOffAction(String name, Domain domain, String parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        protected State performActionHelper(State st, String[] params) {
            dropOff(st);
            return st;
        }
    }

    /**
     * Defines the propositional function of being at a goal
     */
    public static class AtGoalPF extends PropositionalFunction {
        /**
         * Constructs the PF
         * @param name name of the PF
         * @param domain domain tied to the PF
         * @param parameterClasses string of parameters
         */
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

            if(ax == gx && ay == gy) {
                return true;
            }
            
            return false;
        }	
    }

    /**
     * Defines the propositional function of being at the finish point
     */
    public static class AtFinishPF extends PropositionalFunction {
        /**
         * Constructs the PF
         * @param name name of the PF
         * @param domain domain tied to the PF
         * @param parameterClasses string of parameters
         */
        public AtFinishPF(String name, Domain domain, String[] parameterClasses) {
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

            if(ax == gx && ay == gy &&
               goal.getDiscValForAttribute(ATTLEFT) == 0) {
                return true;
            }
            return false;
        }	
    }

    /**
     * Defines the propositional function of being on a passenger
     */
    public static class AtPassPF extends PropositionalFunction {
        /**
         * Constructs the PF
         * @param name name of the PF
         * @param domain domain tied to the PF
         * @param parameterClasses string of parameters
         */
        public AtPassPF(String name, Domain domain, String[] parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        public boolean isTrue(State st, String[] params) {
            ObjectInstance agent = st.getObject(params[0]);
            int ax = agent.getDiscValForAttribute(ATTX);
            int ay = agent.getDiscValForAttribute(ATTY);

            for (int i = 1; i <= MAXPASS; i++) {
                ObjectInstance pass = st.getObject(params[1 + i]);
                int px = pass.getDiscValForAttribute(ATTX);
                int py = pass.getDiscValForAttribute(ATTY);
                
                if (agent.getDiscValForAttribute(ATTCARRY) == 0 &&
                    pass.getDiscValForAttribute(ATTDROPPED) == 0 &&
                    pass.getDiscValForAttribute(ATTCARRIED) == 0 &&
                    ax == px && ay == py) {
                    return true;
                }
            }

            return false;
        }
    }
}
