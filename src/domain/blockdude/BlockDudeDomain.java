package domain.blockdude;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import burlap.behavior.policyblocks.PolicyBlocksPolicy;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.tdmethods.IOQLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.GoalConditionTF;
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
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.singleagent.explorer.TerminalExplorer;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.Visualizer;

public class BlockDudeDomain implements DomainGenerator {

    public static final String ATTX = "x";
    public static final String ATTY = "y";
    public static final String ATTDIR = "dir";
    public static final String ATTHOLD = "holding";
    public static final String ATTHEIGHT = "height";

    public static final String CLASSAGENT = "agent";
    public static final String CLASSBLOCK = "block";
    public static final String CLASSPLATFORM = "platform";
    public static final String CLASSEXIT = "exit";

    public static final String ACTIONUP = "up";
    public static final String ACTIONEAST = "east";
    public static final String ACTIONWEST = "west";
    public static final String ACTIONPICKUP = "pickup";
    public static final String ACTIONPUTDOWN = "putdown";

    public static final String PFHOLDINGBLOCK = "holdingBlock";
    public static final String PFATEXIT = "atExit";

    public int minx = 0;
    public int maxx = 25;
    public int miny = 0;
    public int maxy = 25;

    public BlockDudeDomain() {
	// do nothing
    }

    public BlockDudeDomain(int maxx, int maxy) {
	this.maxx = maxx;
	this.maxy = maxy;
    }

    public BlockDudeDomain(int minx, int maxx, int miny, int maxy) {

	this.minx = minx;
	this.miny = miny;

	this.maxx = maxx;
	this.maxy = maxy;
    }

    @SuppressWarnings("unused")
    @Override
    public Domain generateDomain() {

	Domain domain = new SADomain();

	// setup attributes
	Attribute xAtt = new Attribute(domain, ATTX,
		Attribute.AttributeType.DISC);
	xAtt.setDiscValuesForRange(minx, maxx, 1);

	Attribute yAtt = new Attribute(domain, ATTY,
		Attribute.AttributeType.DISC);
	yAtt.setDiscValuesForRange(miny, maxy, 1);

	Attribute dirAtt = new Attribute(domain, ATTDIR,
		Attribute.AttributeType.DISC);
	dirAtt.setDiscValuesForRange(0, 1, 1);

	Attribute holdAtt = new Attribute(domain, ATTHOLD,
		Attribute.AttributeType.DISC);
	holdAtt.setDiscValuesForRange(0, 1, 1);

	Attribute heightAtt = new Attribute(domain, ATTHEIGHT,
		Attribute.AttributeType.DISC);
	heightAtt.setDiscValuesForRange(miny, maxy, 1);

	// setup object classes
	ObjectClass aclass = new ObjectClass(domain, CLASSAGENT);
	aclass.addAttribute(xAtt);
	aclass.addAttribute(yAtt);
	aclass.addAttribute(dirAtt);
	aclass.addAttribute(holdAtt);

	ObjectClass bclass = new ObjectClass(domain, CLASSBLOCK);
	bclass.addAttribute(xAtt);
	bclass.addAttribute(yAtt);

	ObjectClass pclass = new ObjectClass(domain, CLASSPLATFORM);
	pclass.addAttribute(xAtt);
	pclass.addAttribute(heightAtt);

	ObjectClass eclass = new ObjectClass(domain, CLASSEXIT);
	eclass.addAttribute(xAtt);
	eclass.addAttribute(yAtt);

	// setup actions
	Action upAction = new UpAction(ACTIONUP, domain, "");
	Action eastAction = new EastAction(ACTIONEAST, domain, "");
	Action westAction = new WestAction(ACTIONWEST, domain, "");
	Action pickupAction = new PickupAction(ACTIONPICKUP, domain, "");
	Action putdownAction = new PutdownAction(ACTIONPUTDOWN, domain, "");

	// setup propositional functions
	PropositionalFunction holdingBlockPF = new HoldingBlockPF(
		PFHOLDINGBLOCK, domain, new String[] { CLASSAGENT, CLASSBLOCK });
	PropositionalFunction atExitPF = new AtExitPF(PFATEXIT, domain,
		new String[] { CLASSAGENT, CLASSEXIT });

	return domain;
    }

    public static State getCleanState(Domain domain, List<Integer> platformX,
	    List<Integer> platformH, int nb) {

	State s = new State();

	// start by creating the platform objects
	for (int i = 0; i < platformX.size(); i++) {
	    int x = platformX.get(i);
	    int h = platformH.get(i);

	    ObjectInstance plat = new ObjectInstance(
		    domain.getObjectClass(CLASSPLATFORM), CLASSPLATFORM + x);
	    plat.setValue(ATTX, x);
	    plat.setValue(ATTHEIGHT, h);

	    s.addObject(plat);

	}

	// create n blocks
	for (int i = 0; i < nb; i++) {
	    s.addObject(new ObjectInstance(domain.getObjectClass(CLASSBLOCK),
		    CLASSBLOCK + i));
	}

	// create exit
	s.addObject(new ObjectInstance(domain.getObjectClass(CLASSEXIT),
		CLASSEXIT + 0));

	// create agent
	s.addObject(new ObjectInstance(domain.getObjectClass(CLASSAGENT),
		CLASSAGENT + 0));

	return s;

    }

    public static void setAgent(State s, int x, int y, int dir, int holding) {
	ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);
	agent.setValue(ATTX, x);
	agent.setValue(ATTY, y);
	agent.setValue(ATTDIR, dir);
	agent.setValue(ATTHOLD, holding);
    }

    public static void setExit(State s, int x, int y) {
	ObjectInstance exit = s.getObjectsOfTrueClass(CLASSEXIT).get(0);
	exit.setValue(ATTX, x);
	exit.setValue(ATTY, y);
    }

    public static void setBlock(State s, int i, int x, int y) {
	ObjectInstance block = s.getObjectsOfTrueClass(CLASSBLOCK).get(i);
	block.setValue(ATTX, x);
	block.setValue(ATTY, y);
    }

    public static void moveHorizontally(State s, int dx) {

	ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);

	// always set direction
	if (dx > 0) {
	    agent.setValue(ATTDIR, 1);
	} else {
	    agent.setValue(ATTDIR, 0);
	}

	int ax = agent.getDiscValForAttribute(ATTX);
	int ay = agent.getDiscValForAttribute(ATTY);

	int nx = ax + dx;

	int heightAtNX = totalHeightAtXPos(s, nx);

	// can only move if new position is below agent height
	if (heightAtNX >= ay) {
	    return; // do nothing; walled off
	}

	int ny = heightAtNX + 1; // stand on top of stack

	agent.setValue(ATTX, nx);
	agent.setValue(ATTY, ny);

	moveCarriedBlockToNewAgentPosition(s, agent, ax, ay, nx, ny);

    }

    public static void moveUp(State s) {

	ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);

	int ax = agent.getDiscValForAttribute(ATTX);
	int ay = agent.getDiscValForAttribute(ATTY);
	int dir = agent.getDiscValForAttribute(ATTDIR);

	if (dir == 0) {
	    dir = -1;
	}

	int nx = ax + dir;
	int ny = ay + 1;

	int heightAtNX = totalHeightAtXPos(s, nx);

	// in order to move up, the height of world in new x position must be at
	// the same current agent position
	if (heightAtNX != ay) {
	    return; // not a viable move up condition, so do nothing
	}

	agent.setValue(ATTX, nx);
	agent.setValue(ATTY, ny);

	moveCarriedBlockToNewAgentPosition(s, agent, ax, ay, nx, ny);

    }

    public static void pickupBlock(State s) {

	ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);

	int holding = agent.getDiscValForAttribute(ATTHOLD);
	if (holding == 1) {
	    return; // already holding a block
	}

	int ax = agent.getDiscValForAttribute(ATTX);
	int ay = agent.getDiscValForAttribute(ATTY);
	int dir = agent.getDiscValForAttribute(ATTDIR);

	if (dir == 0) {
	    dir = -1;
	}

	// can only pick up blocks one unit away in agent facing direction and
	// at same height as agent
	int bx = ax + dir;
	ObjectInstance block = getBlockAt(s, bx, ay);

	if (block != null) {

	    // make sure that block is the top of the world, otherwise something
	    // is stacked above it and you cannot pick it up
	    int mxh = totalHeightAtXPos(s, bx);
	    if (mxh > block.getDiscValForAttribute(ATTY)) {
		return;
	    }

	    block.setValue(ATTX, ax);
	    block.setValue(ATTY, ay + 1);

	    agent.setValue(ATTHOLD, 1);

	}

    }

    public static void putdownBlock(State s) {

	ObjectInstance agent = s.getObjectsOfTrueClass(CLASSAGENT).get(0);

	int holding = agent.getDiscValForAttribute(ATTHOLD);
	if (holding == 0) {
	    return; // not holding a block
	}

	int ax = agent.getDiscValForAttribute(ATTX);
	int ay = agent.getDiscValForAttribute(ATTY);
	int dir = agent.getDiscValForAttribute(ATTDIR);

	if (dir == 0) {
	    dir = -1;
	}

	int nx = ax + dir;

	int heightAtNX = totalHeightAtXPos(s, nx);
	if (heightAtNX > ay) {
	    return; // cannot drop block if walled off from throw position
	}

	ObjectInstance block = getBlockAt(s, ax, ay + 1); // carried block is
							  // one unit above
							  // agent
	/*
	 * if (getBlockAt(s, nx, ay) != null || (getBlockAt(s, nx, ay-1) != null
	 * && (nx != 11 && nx != 3))) { return; }
	 */
	block.setValue(ATTX, nx);
	block.setValue(ATTY, heightAtNX + 1); // stacked on top of this position

	agent.setValue(ATTHOLD, 0);

    }

    private static void moveCarriedBlockToNewAgentPosition(State s,
	    ObjectInstance agent, int ax, int ay, int nx, int ny) {
	int holding = agent.getDiscValForAttribute(ATTHOLD);
	if (holding == 1) {
	    // then move the box being carried too
	    ObjectInstance carriedBlock = getBlockAt(s, ax, ay + 1); // carried
								     // block is
								     // one unit
								     // above
								     // agent
	    carriedBlock.setValue(ATTX, nx);
	    carriedBlock.setValue(ATTY, ny + 1);
	}
    }

    private static ObjectInstance getBlockAt(State s, int x, int y) {

	List<ObjectInstance> blocks = s.getObjectsOfTrueClass(CLASSBLOCK);
	for (ObjectInstance block : blocks) {
	    int bx = block.getDiscValForAttribute(ATTX);
	    int by = block.getDiscValForAttribute(ATTY);
	    if (bx == x && by == y) {
		return block;
	    }
	}

	return null;

    }

    private static int totalHeightAtXPos(State s, int x) {

	// first see if there are any blocks at this x pos and if so, what
	// highest one is
	List<ObjectInstance> blocks = s.getObjectsOfTrueClass(CLASSBLOCK);
	int maxBlock = -1;
	for (ObjectInstance block : blocks) {
	    int bx = block.getDiscValForAttribute(ATTX);
	    if (bx != x) {
		continue;
	    }
	    int by = block.getDiscValForAttribute(ATTY);
	    if (by > maxBlock) {
		maxBlock = by;
	    }

	}

	if (maxBlock > -1) {
	    return maxBlock; // there are stacked blocks here which must be the
			     // highest point
	}

	// get platform at pos x
	ObjectInstance plat = s.getObject(CLASSPLATFORM + x);

	if (plat != null) {
	    return plat.getDiscValForAttribute(ATTHEIGHT);
	}

	return -1;
    }

    public class UpAction extends Action {

	public UpAction(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public UpAction(String name, Domain domain, String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	protected State performActionHelper(State st, String[] params) {
	    moveUp(st);
	    return st;
	}

    }

    public class EastAction extends Action {

	public EastAction(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public EastAction(String name, Domain domain, String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	protected State performActionHelper(State st, String[] params) {
	    moveHorizontally(st, 1);
	    return st;
	}

    }

    public class WestAction extends Action {

	public WestAction(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public WestAction(String name, Domain domain, String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	protected State performActionHelper(State st, String[] params) {
	    moveHorizontally(st, -1);
	    return st;
	}

    }

    public class PickupAction extends Action {

	public PickupAction(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public PickupAction(String name, Domain domain,
		String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	protected State performActionHelper(State st, String[] params) {
	    pickupBlock(st);
	    return st;
	}

    }

    public class PutdownAction extends Action {

	public PutdownAction(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public PutdownAction(String name, Domain domain,
		String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	protected State performActionHelper(State st, String[] params) {
	    putdownBlock(st);
	    return st;
	}

    }

    public class HoldingBlockPF extends PropositionalFunction {

	public HoldingBlockPF(String name, Domain domain,
		String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public HoldingBlockPF(String name, Domain domain,
		String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	public boolean isTrue(State st, String[] params) {

	    ObjectInstance agent = st.getObject(params[0]);
	    ObjectInstance block = st.getObject(params[1]);

	    int ax = agent.getDiscValForAttribute(ATTX);
	    int ay = agent.getDiscValForAttribute(ATTY);
	    int ah = agent.getDiscValForAttribute(ATTHOLD);

	    int bx = block.getDiscValForAttribute(ATTX);
	    int by = block.getDiscValForAttribute(ATTY);

	    if (ax == bx && ay == by - 1 && ah == 1) {
		return true;
	    }

	    return false;
	}

    }

    public class AtExitPF extends PropositionalFunction {

	public AtExitPF(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public AtExitPF(String name, Domain domain, String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	public boolean isTrue(State st, String[] params) {

	    ObjectInstance agent = st.getObject(params[0]);
	    ObjectInstance exit = st.getObject(params[1]);
	    int ax;
	    try {
		ax = agent.getDiscValForAttribute(ATTX);
	    } catch (Exception e) {

		ax = agent.getDiscValForAttribute(ATTX);
	    }
	    int ay = agent.getDiscValForAttribute(ATTY);

	    int ex = exit.getDiscValForAttribute(ATTX);
	    int ey = exit.getDiscValForAttribute(ATTY);

	    if (ax == ex && ay == ey) {
		return true;
	    }

	    return false;
	}

    }

    public static final char AGENT_LEFT_TOKEN = '<';
    public static final char AGENT_RIGHT_TOKEN = '>';
    public static final char GOAL_TOKEN = 'g';
    public static final char BLOCK_TOKEN = 'b';
    public static final char PLATFORM_TOKEN = 't';

    public static class DomainData {
	public BlockDudeDomain c;
	public Domain d;
	public State s;

	public DomainData(BlockDudeDomain c, Domain d, State s) {
	    this.c = c;
	    this.d = d;
	    this.s = s;
	}
    }

    public static class Coordinates implements Comparable<Coordinates> {
	public int x;
	public int y;

	public Coordinates(int x, int y) {
	    this.x = x;
	    this.y = y;
	}

	@Override
	public int compareTo(Coordinates o) {
	    if (x - o.x != 0)
		return x - o.x;
	    else
		return y - o.y;
	}
    }

    public static DomainData createDomain(char[][] lvl) {
	if (lvl == null || lvl.length == 0)
	    throw new IllegalArgumentException();

	int maxx = lvl[0].length;
	int maxy = lvl.length;
	List<Integer> px = new ArrayList<Integer>();
	List<Integer> ph = new ArrayList<Integer>();
	List<Coordinates> pc = new ArrayList<Coordinates>();
	List<Coordinates> bc = new ArrayList<Coordinates>();
	int goalx = 0, goaly = 0;
	int agentx = 0, agenty = 0;
	int agentdir = 0;
	int needed = 0;

	for (int i = 0; i < maxy; i++) {
	    for (int j = 0; j < maxx; j++) {
		switch (lvl[i][j]) {
		case AGENT_LEFT_TOKEN:
		    agentx = j;
		    agenty = maxy - i;
		    agentdir = 0;
		    needed++;
		    break;
		case AGENT_RIGHT_TOKEN:
		    agentx = j;
		    agenty = maxy - i;
		    agentdir = 1;
		    needed++;
		    break;
		case GOAL_TOKEN:
		    goalx = j;
		    goaly = maxy - i;
		    needed++;
		    break;
		case BLOCK_TOKEN:
		    bc.add(new Coordinates(j, maxy - i));
		    break;
		case PLATFORM_TOKEN:
		    pc.add(new Coordinates(j, maxy - i));
		    break;
		}
	    }
	}

	// Needs 1 agent and 1 goal
	if (needed != 2)
	    throw new IllegalArgumentException();

	Collections.sort(bc);
	Collections.sort(pc);

	for (int i = 0; i < pc.size(); i++) {
	    Coordinates c = pc.get(i);
	    px.add(c.x);
	    ph.add(c.y);
	}

	BlockDudeDomain c = new BlockDudeDomain(maxx, maxy);
	Domain d = c.generateDomain();

	State s = getCleanState(d, px, ph, bc.size());
	setAgent(s, agentx, agenty, agentdir, 0);
	setExit(s, goalx, goaly);

	for (int i = 0; i < bc.size(); i++) {
	    int x = bc.get(i).x;
	    int y = bc.get(i).y;
	    setBlock(s, i, x, y);
	}

	return new DomainData(c, d, s);
    }

    public static char[][] genLevel(int numBlocks) {
	/*
	 * WARNING: Platforms only store height! This means that platforms
	 * always extend from the bottom of the level to the given height. This
	 * makes it impossible to implement certain levels where block dude can
	 * walk under platforms. Possibly fix this in the future although we
	 * don't have to for the levels given.
	 */

	if (numBlocks < 6 || numBlocks > 10) {
	    throw new IllegalArgumentException(
		    "Number of blocks must be [6, 10].");
	}

	int[][] f_blocks = new int[][] { { 4, 12 }, { 4, 13 }, { 4, 14 },
		{ 4, 15 }, { 4, 16 }, { 4, 17 } };
	int[][] s_blocks = new int[][] { { 4, 3 }, { 4, 4 }, { 4, 5 },
		{ 4, 6 }, { 4, 7 } };

	char[][] lvl = {
		{ 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', 't' },
		{ ' ', 'g', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', 't', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 't', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' },
		{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ',
			' ', ' ', ' ', ' ', ' ', ' ', '<', ' ' },
		{ ' ', ' ', 't', 't', 't', 't', 't', 't', 't', ' ', ' ', 't',
			't', 't', 't', 't', 't', 't', 't', ' ' } };

	Random rand = new Random();
	// Set up the first two blocks
	int r = rand.nextInt(f_blocks.length);
	lvl[f_blocks[r][0]][f_blocks[r][1]] = 'b';
	r = rand.nextInt(s_blocks.length);
	lvl[s_blocks[r][0]][s_blocks[r][1]] = 'b';
	int f = 2;
	int s = 2;
	int c = numBlocks - 2;
	while (c > 0) {
	    if (f > 0 || (s == 0 && rand.nextBoolean())) {
		r = rand.nextInt(f_blocks.length);
		if (lvl[f_blocks[r][0]][f_blocks[r][1]] == 'b') {
		    continue;
		}
		lvl[f_blocks[r][0]][f_blocks[r][1]] = 'b';
		c--;
		f--;
	    } else {
		r = rand.nextInt(s_blocks.length);
		if (lvl[s_blocks[r][0]][s_blocks[r][1]] == 'b') {
		    continue;
		}
		lvl[s_blocks[r][0]][s_blocks[r][1]] = 'b';
		c--;
		s--;
	    }
	}

	return lvl;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	DomainData dd = BlockDudeDomain.createDomain(genLevel(2));

	final Domain d = dd.d;
	State s = dd.s;
	BlockDudeDomain constructor = dd.c;

	int expMode = 2;

	if (expMode == 1) {
	    Visualizer v = BlockDudeVisualizer.getVisualizer(constructor.minx,
		    constructor.maxx, constructor.miny, constructor.maxy);
	    VisualExplorer exp = new VisualExplorer(d, v, s);
	    exp.enableEpisodeRecording("'", "]");

	    // use w-s-a-d-e
	    exp.addKeyAction("w", ACTIONUP);
	    exp.addKeyAction("s", ACTIONPICKUP);
	    exp.addKeyAction("a", ACTIONWEST);
	    exp.addKeyAction("d", ACTIONEAST);
	    exp.addKeyAction("e", ACTIONPUTDOWN);

	    exp.initGUI();
	} else {
	    DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	    hf.setAttributesForClass(
		    BlockDudeDomain.CLASSAGENT,
		    dd.d.getObjectClass(BlockDudeDomain.CLASSAGENT).attributeList);
	    hf.setAttributesForClass(
		    BlockDudeDomain.CLASSBLOCK,
		    dd.d.getObjectClass(BlockDudeDomain.CLASSBLOCK).attributeList);
	    hf.setAttributesForClass(
		    BlockDudeDomain.CLASSEXIT,
		    dd.d.getObjectClass(BlockDudeDomain.CLASSEXIT).attributeList);
	    double gamma = 0.995;
	    double learningRate = 0.99;
	    TerminalFunction tf = new GoalConditionTF(new StateConditionTest() {
		private PropositionalFunction pf = d
			.getPropFunction(BlockDudeDomain.PFATEXIT);

		@Override
		public boolean satisfies(State s) {
		    return pf.isTrue(s, new String[] { "agent0", "exit0" });
		}
	    });

	    PolicyBlocksPolicy p = new PolicyBlocksPolicy(0.05);
	    QLearning agent = new IOQLearning(dd.d, new UniformCostRF(), tf,
		    gamma, hf, -1, learningRate, p, 10000);
	    p.qplanner = agent;
	    long t = System.currentTimeMillis();

	    for (int i = 0; i < 1000; i++) {
		System.out.println("Running episode " + (i + 1) + " ["
			+ (System.currentTimeMillis() - t) / 1000.0 + "]");

		t = System.currentTimeMillis();
		agent.runLearningEpisodeFrom(dd.s);
	    }
	}
    }
}
