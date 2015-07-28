package domain.attributetest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;

public class AttributeTestDomain implements DomainGenerator {
    public static final String ATTX = "x";
    public static final String ATTY = "y";
    public static final String ATTCOLOR = "color";
    public static final String ATTSHAPE = "shape";
    public static final String ATTSIZE = "size";

    public static final String CLASSAGENT = "agent";
    public static final String CLASSBLOCK = "block";
    public static final String CLASSGOAL = "goal";

    public static final String ACTIONN = "north";
    public static final String ACTIONS = "south";
    public static final String ACTIONE = "east";
    public static final String ACTIONW = "west";

    public static final String PFGOAL = "at-goal";

    protected static Integer x;
    protected static Integer y;

    public AttributeTestDomain(int nX, int nY) {
	x = nX;
	y = nY;
    }

    @SuppressWarnings("unused")
    @Override
    public Domain generateDomain() {
	Domain d = new SADomain();

	Attribute attX = new Attribute(d, ATTX, Attribute.AttributeType.DISC);
	attX.setDiscValuesForRange(0, x, 1);

	Attribute attY = new Attribute(d, ATTY, Attribute.AttributeType.DISC);
	attY.setDiscValuesForRange(0, y, 1);

	Attribute attColor = new Attribute(d, ATTCOLOR,
		Attribute.AttributeType.DISC);
	attColor.setDiscValues(new String[] { "nil", "red", "green", "blue" });

	Attribute attShape = new Attribute(d, ATTSHAPE,
		Attribute.AttributeType.DISC);
	attShape.setDiscValues(new String[] { "nil", "square", "circle",
		"triangle" });

	Attribute attSize = new Attribute(d, ATTSIZE,
		Attribute.AttributeType.DISC);
	attSize.setDiscValues(new String[] { "nil", "small", "medium", "large" });

	ObjectClass classAgent = new ObjectClass(d, CLASSAGENT);
	classAgent.addAttribute(attX);
	classAgent.addAttribute(attY);

	ObjectClass classBlock = new ObjectClass(d, CLASSBLOCK);
	classBlock.addAttribute(attX);
	classBlock.addAttribute(attY);
	classBlock.addAttribute(attColor);
	classBlock.addAttribute(attShape);
	classBlock.addAttribute(attSize);

	ObjectClass classGoal = new ObjectClass(d, CLASSGOAL);
	classGoal.addAttribute(attX);
	classGoal.addAttribute(attY);

	Action actionN = new MoveAction(ACTIONN, d, "");
	Action actionS = new MoveAction(ACTIONS, d, "");
	Action actionE = new MoveAction(ACTIONE, d, "");
	Action actionW = new MoveAction(ACTIONW, d, "");

	PropositionalFunction pfGoal = new GoalPF(PFGOAL, d, "");

	return d;
    }

    public static State getCleanState(Domain d, int aX, int aY, int gX, int gY,
	    int numBlocks) {
	if (numBlocks < 0 || numBlocks > (x * y) / 2) {
	    throw new IllegalArgumentException("Number of blocks is invalid.");
	}
	if (aX < 0 || aX > x || aY < 0 || aY > y) {
	    throw new IllegalArgumentException("Agent position is invalid.");
	}
	if (gX < 0 || gX > x || gY < 0 || gY > y) {
	    throw new IllegalArgumentException("Goal position is invalid.");
	}
	// Blocks are randomly placed.
	State s = new State();

	ObjectInstance agent = new ObjectInstance(d.getObjectClass(CLASSAGENT),
		CLASSAGENT);
	agent.setValue(ATTX, aX);
	agent.setValue(ATTY, aY);
	s.addObject(agent);

	ObjectInstance goal = new ObjectInstance(d.getObjectClass(CLASSGOAL),
		CLASSGOAL);
	goal.setValue(ATTX, gX);
	goal.setValue(ATTY, gY);
	s.addObject(goal);

	Random rand = new Random();

	for (int i = 0; i < numBlocks; i++) {
	    ObjectInstance block = new ObjectInstance(
		    d.getObjectClass(CLASSBLOCK), CLASSBLOCK + i);
	    block.setValue(ATTX, rand.nextInt(x));
	    block.setValue(ATTY, rand.nextInt(y));

	    block.setValue(ATTCOLOR, rand.nextInt(4));
	    block.setValue(ATTSHAPE, rand.nextInt(4));
	    block.setValue(ATTSIZE, rand.nextInt(4));

	    s.addObject(block);
	}

	return s;
    }

    public static void printState(State s) {
	List<Integer> blockXs = new ArrayList<Integer>();
	List<Integer> blockYs = new ArrayList<Integer>();
	ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
	ObjectInstance goal = s.getFirstObjectOfClass(CLASSGOAL);

	int aX = agent.getDiscValForAttribute(ATTX);
	int aY = agent.getDiscValForAttribute(ATTY);
	int gX = goal.getDiscValForAttribute(ATTX);
	int gY = goal.getDiscValForAttribute(ATTY);

	for (ObjectInstance block : s.getObjectsOfTrueClass(CLASSBLOCK)) {
	    blockXs.add(block.getDiscValForAttribute(ATTX));
	    blockYs.add(block.getDiscValForAttribute(ATTY));
	}

	for (int i = y; i >= 0; i--) {
	    for (int j = 0; j <= x; j++) {
		boolean flag = false;
		if (j == 0 || j == x) {
		    System.out.print("|");
		    flag = true;
		} else if (i == 0 || i == x) {
		    System.out.print("X");
		    flag = true;
		}
		if (aX == j && aY == i && !flag) {
		    System.out.print("a");
		    flag = true;
		}
		if (gX == j && gY == i && !flag) {
		    System.out.print("g");
		    flag = true;
		}

		for (int bX = 0; bX < blockXs.size(); bX++) {
		    if (blockXs.get(bX).equals(j) && blockYs.get(bX).equals(i)
			    && !flag) {
			System.out.print("b");
			flag = true;
			break;
		    }
		}

		if (!flag) {
		    System.out.print("-");
		}
	    }
	    System.out.println();
	}
    }

    public class MoveAction extends Action {
	public MoveAction(String name, Domain d, String params) {
	    super(name, d, params);
	}

	public MoveAction(String name, Domain d, String[] params) {
	    super(name, d, params);
	}

	@Override
	protected State performActionHelper(State s, String[] params) {
	    ObjectInstance agent = s.getFirstObjectOfClass(CLASSAGENT);
	    int oldX = agent.getDiscValForAttribute(ATTX);
	    int oldY = agent.getDiscValForAttribute(ATTY);
	    List<ObjectInstance> blocks = s.getObjectsOfTrueClass(CLASSBLOCK);

	    if (name.equals(ACTIONN)) {
		if (oldY < y) {
		    agent.setValue(ATTY, oldY + 1);
		    for (ObjectInstance block : blocks) {
			int bX = block.getDiscValForAttribute(ATTX);
			int bY = block.getDiscValForAttribute(ATTY);
			if (bX == oldX && bY == oldY + 1 && bY < y) {
			    block.setValue(ATTY, bY + 1);
			}
		    }
		}
	    } else if (name.equals(ACTIONS)) {
		if (oldY > 0) {
		    agent.setValue(ATTY, oldY - 1);
		    for (ObjectInstance block : blocks) {
			int bX = block.getDiscValForAttribute(ATTX);
			int bY = block.getDiscValForAttribute(ATTY);
			if (bX == oldX && bY == oldY - 1 && bY > 0) {
			    block.setValue(ATTY, bY - 1);
			}
		    }
		}
	    } else if (name.equals(ACTIONE)) {
		if (oldX < x) {
		    agent.setValue(ATTX, oldX + 1);
		    for (ObjectInstance block : blocks) {
			int bX = block.getDiscValForAttribute(ATTX);
			int bY = block.getDiscValForAttribute(ATTY);
			if (bX == oldX + 1 && bY == oldY && bX < x) {
			    block.setValue(ATTY, bX + 1);
			}
		    }
		}
	    } else if (name.equals(ACTIONW)) {
		if (oldX > 0) {
		    agent.setValue(ATTX, oldX - 1);
		    for (ObjectInstance block : blocks) {
			int bX = block.getDiscValForAttribute(ATTX);
			int bY = block.getDiscValForAttribute(ATTY);
			if (bX == oldX - 1 && bY == oldY && bX > 0) {
			    block.setValue(ATTY, bX - 1);
			}
		    }
		}
	    }

	    return s;
	}
    }

    public class GoalPF extends PropositionalFunction {

	public GoalPF(String name, Domain domain, String parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	public GoalPF(String name, Domain domain, String[] parameterClasses) {
	    super(name, domain, parameterClasses);
	}

	@Override
	public boolean isTrue(State s, String[] params) {
	    List<ObjectInstance> blocks = s.getObjectsOfTrueClass(CLASSBLOCK);
	    ObjectInstance goal = s.getFirstObjectOfClass(CLASSGOAL);

	    int gX = goal.getDiscValForAttribute(ATTX);
	    int gY = goal.getDiscValForAttribute(ATTY);

	    for (ObjectInstance block : blocks) {
		int bX = block.getDiscValForAttribute(ATTX);
		int bY = block.getDiscValForAttribute(ATTY);

		if (bX == gX && bY == gY) {
		    return true;
		}
	    }

	    return false;
	}

    }
}