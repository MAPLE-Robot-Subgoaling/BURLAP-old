package domain.teleporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.behavior.PolicyBlock.PolicyBlockPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.options.PrimitiveOption;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
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
import burlap.oomdp.visualizer.Visualizer;

public class TeleporterDomain implements DomainGenerator {
    public static final String ATTX = "x";
    public static final String ATTY = "y";
    public static final String ATTGHOST = "ghostAtt";

    public static final String CLASSAGENT = "agent";
    public static final String CLASSTELE = "teleporter";
    public static final String CLASSGOAL = "goal";
    public static final String CLASSBLOCK = "ghost";

    public static final String ACTIONNORTH = "north";
    public static final String ACTIONSOUTH = "south";
    public static final String ACTIONEAST = "east";
    public static final String ACTIONWEST = "west";

    public static final String PFATGOAL = "atGoal";
    public static final String PFATTELE = "atTele";

    public static final int	MAXX = 5;
    public static final int	MAXY = 5;

    public static int[][] MAP;
    public static boolean MAPGENERATED = false;
    public static Domain DOMAIN = null;

    public static final double LEARNINGRATE = 0.99;
    public static final double DISCOUNTFACTOR = 0.95;
    public static final double LAMBDA = 1.0;
    public static Map<StateHashTuple, List<QAction>> qVals =
            new HashMap<StateHashTuple, List<QAction>>();
    public static LearningAgent Q, S;
    public static OOMDPPlanner planner;
    public static EpisodeAnalysis analyzer;
    public static TeleporterStateParser parser;
    public static RewardFunction rf;
    public static TerminalFunction tf;
    public static PolicyBlockPolicy pi;

    public static void main(String[] args) {
        TeleporterDomain tpd = new TeleporterDomain();
        Domain d = tpd.generateDomain();
        State s = getCleanState();
        parser = new TeleporterStateParser();
        
        for (int i = 1; i <= 10; i++) {
            setAgent(s, 4, 4);
            setGoal(s, 1, 3);
            setTele(s, 2, 3);
            setBlock(s, 2, 4);

            analyzer = new EpisodeAnalysis();
            System.out.print("Episode " + i + ": ");
            analyzer = Q.runLearningEpisodeFrom(s);
            System.out.println("\tSteps: " + analyzer.numTimeSteps());
            analyzer.writeToFile(String.format("output/e%03d", i), parser);
        }

        Visualizer v = TeleporterVisualizer.getVisualizer();
        new EpisodeSequenceVisualizer(v, d, parser, "output");
        int i = 1;
        System.out.println(pi.policy.size());
        //for (Entry<StateHashTuple, GroundedAction> e: pi.policy.entrySet()) {
        	//System.out.println(i + ":\n" + e.getKey().hashCode() + "\n: " + e.getValue());
        	//System.out.println(e.getKey().s.hashCode());
        	//i++;
        //}
    }
    
    @Override
    public Domain generateDomain() {
        if (DOMAIN != null) {
            return DOMAIN;
        }

        DOMAIN = new SADomain();
        generateMap();

        Attribute xatt = new Attribute(DOMAIN, ATTX, Attribute.AttributeType.DISC);
        xatt.setDiscValuesForRange(0, MAXX, 1);
        Attribute yatt = new Attribute(DOMAIN, ATTY, Attribute.AttributeType.DISC);
        yatt.setDiscValuesForRange(0, MAXY, 1);
        Attribute ghostatt = new Attribute(DOMAIN, ATTGHOST, Attribute.AttributeType.DISC);
        ghostatt.setDiscValuesForRange(0, 2, 1);
        DOMAIN.addAttribute(xatt);
        DOMAIN.addAttribute(yatt);
        DOMAIN.addAttribute(ghostatt);

        ObjectClass agentClass = new ObjectClass(DOMAIN, CLASSAGENT);
        agentClass.addAttribute(xatt);
        agentClass.addAttribute(yatt);

        ObjectClass teleClass = new ObjectClass(DOMAIN, CLASSTELE);
        teleClass.addAttribute(xatt);
        teleClass.addAttribute(yatt);

        ObjectClass goalClass = new ObjectClass(DOMAIN, CLASSGOAL);
        goalClass.addAttribute(xatt);
        goalClass.addAttribute(yatt);
        
        ObjectClass blockClass = new ObjectClass(DOMAIN, CLASSBLOCK);
        blockClass.addAttribute(xatt);
        blockClass.addAttribute(yatt);
        blockClass.addAttribute(ghostatt);

        DOMAIN.addObjectClass(goalClass);
        DOMAIN.addObjectClass(agentClass);
        DOMAIN.addObjectClass(teleClass);
        DOMAIN.addObjectClass(blockClass);

        Action north = new PrimitiveOption(new NorthAction(ACTIONNORTH, DOMAIN, ""));
        Action south = new PrimitiveOption(new SouthAction(ACTIONSOUTH, DOMAIN, ""));
        Action east = new PrimitiveOption(new EastAction(ACTIONEAST, DOMAIN, ""));
        Action west = new PrimitiveOption(new WestAction(ACTIONWEST, DOMAIN, ""));

        DOMAIN.addAction(north);
        DOMAIN.addAction(south);
        DOMAIN.addAction(east);
        DOMAIN.addAction(west);

        PropositionalFunction atGoal = new AtGoalPF(PFATGOAL, DOMAIN, new String[] {
            CLASSAGENT,
            CLASSGOAL,
            CLASSTELE
        });
        PropositionalFunction atTele = new AtTelePF(PFATTELE, DOMAIN, new String[] {
                CLASSAGENT,
                CLASSGOAL,
                CLASSTELE
        });

        DOMAIN.addPropositionalFunction(atGoal);
        DOMAIN.addPropositionalFunction(atTele);

        rf = new SingleGoalPFRF(DOMAIN.getPropFunction(PFATGOAL));
        tf = new SinglePFTF(DOMAIN.getPropFunction(PFATGOAL));

        DiscreteStateHashFactory hashFactory = new DiscreteStateHashFactory();
        hashFactory.setAttributesForClass(CLASSAGENT, DOMAIN.getObjectClass(CLASSAGENT).attributeList);
        
        pi = new PolicyBlockPolicy(0.1);
        Q = new QLearning(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, 0.2, LEARNINGRATE, pi, Integer.MAX_VALUE);
        pi.setPlanner((OOMDPPlanner) Q);
        S = new SarsaLam(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, 0.2, LEARNINGRATE, LAMBDA);
        planner = new ValueIteration(DOMAIN, rf, tf, DISCOUNTFACTOR, hashFactory, 0.001, 100);

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
        s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSTELE), CLASSTELE));
        s.addObject(new ObjectInstance(DOMAIN.getObjectClass(CLASSBLOCK), CLASSBLOCK));
        
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
    }
    
    /**
     * Sets the block to position (x, y)
     * @param s the current state
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void setBlock(State s, int x, int y) {
        ObjectInstance block = s.getObjectsOfTrueClass(CLASSBLOCK).get(0);
        block.setValue(ATTX, x);
        block.setValue(ATTY, y);
        block.setValue(ATTGHOST, 1);
    }
    
    /**
     * Sets the teleporter to position (x, y)
     * @param s the current state
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public static void setTele(State s, int x, int y) {
        ObjectInstance tele = s.getObjectsOfTrueClass(CLASSTELE).get(0);
        tele.setValue(ATTX, x);
        tele.setValue(ATTY, y);
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
    	// No walls for now
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
        ObjectInstance tele = s.getObjectsOfTrueClass(CLASSTELE).get(0);
        //ObjectInstance ghost = s.getObjectsOfTrueClass(CLASSBLOCK).get(0);
        //ghost.setValue(ATTGHOST, (ghost.getDiscValForAttribute(ATTGHOST) % 2) + 1);
        int ax = agent.getDiscValForAttribute(ATTX);
        int ay = agent.getDiscValForAttribute(ATTY);
        int tx = tele.getDiscValForAttribute(ATTX);
        int ty = tele.getDiscValForAttribute(ATTY);
        int nx = ax+xd;
        int ny = ay+yd;

        if(MAP[nx][ny] == 1) {
            nx = ax;
            ny = ay;
        }
        // Teleport the to top right corner
        if (ax == tx && ay == ty) {
        	nx = MAXX - 1;
        	ny = MAXY - 1;
        }

        agent.setValue(ATTX, nx);
        agent.setValue(ATTY, ny);
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
    public static class EastAction extends Action {
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
        
        /*@Override
        public List<TransitionProbability> getTransitions(Action a, String[] params) {
        }*/
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
     * Defines the propositional function of being at a goal
     */
    public static class AtTelePF extends PropositionalFunction {
        /**
         * Constructs the PF
         * @param name name of the PF
         * @param domain domain tied to the PF
         * @param parameterClasses string of parameters
         */
        public AtTelePF(String name, Domain domain, String[] parameterClasses) {
            super(name, domain, parameterClasses);
        }

        @Override
        public boolean isTrue(State st, String[] params) {
            ObjectInstance agent = st.getObject(params[0]);
            int ax = agent.getDiscValForAttribute(ATTX);
            int ay = agent.getDiscValForAttribute(ATTY);

            ObjectInstance tele = st.getObject(params[2]);
            int tx = tele.getDiscValForAttribute(ATTX);
            int ty = tele.getDiscValForAttribute(ATTY);

            if(ax == tx && ay == ty) {
                return true;
            }
            
            return false;
        }	
    }
}
