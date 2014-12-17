package domain.singleagent.sokoban2;

import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class Sokoban2RF extends UniformCostRF {
    private double goodReward;

    public Sokoban2RF(double goodReward) {
	this.goodReward = goodReward;
    }

    @Override
    public double reward(State s, GroundedAction a, State sprime) {
	List<ObjectInstance> blocks = sprime
		.getObjectsOfTrueClass(Sokoban2Domain.CLASSBLOCK);
	List<ObjectInstance> rooms = sprime
		.getObjectsOfTrueClass(Sokoban2Domain.CLASSROOM);

	for (int i = 0; i < blocks.size(); i++) {
	    int bx = blocks.get(i).getDiscValForAttribute(Sokoban2Domain.ATTX);
	    int by = blocks.get(i).getDiscValForAttribute(Sokoban2Domain.ATTY);
	    ObjectInstance room = Sokoban2Domain.roomContainingPoint(sprime,
		    bx, by);
	    String bc = blocks.get(i).getStringValForAttribute(
		    Sokoban2Domain.ATTCOLOR);
	    String rc = room.getStringValForAttribute(Sokoban2Domain.ATTCOLOR);
	    if (bc.equals(rc)) {
		continue;
	    }

	    int wallsprimeouched = 0;
	    if (Sokoban2Domain.wallCheck(sprime, room, bx + 1, by))
		wallsprimeouched++;
	    if (Sokoban2Domain.wallCheck(sprime, room, bx - 1, by))
		wallsprimeouched++;
	    if (Sokoban2Domain.wallCheck(sprime, room, bx, by + 1))
		wallsprimeouched++;
	    if (Sokoban2Domain.wallCheck(sprime, room, bx, by - 1))
		wallsprimeouched++;

	    if (wallsprimeouched >= 2
		    && Sokoban2Domain.doorContainingPoint(sprime, bx, by) == null) {
		return -goodReward;
	    }
	}

	int blockCount = 0;
	for (int i = 0; i < blocks.size(); i++) {
	    int bx = blocks.get(i).getDiscValForAttribute(Sokoban2Domain.ATTX);
	    int by = blocks.get(i).getDiscValForAttribute(Sokoban2Domain.ATTY);
	    String bc = blocks.get(i).getStringValForAttribute(
		    Sokoban2Domain.ATTCOLOR);

	    for (int j = 0; j < rooms.size(); j++) {
		String rc = rooms.get(j).getStringValForAttribute(
			Sokoban2Domain.ATTCOLOR);
		ObjectInstance door = Sokoban2Domain.doorContainingPoint(
			sprime, bx, by);
		if (Sokoban2Domain.regionContainsPoint(rooms.get(j), bx, by)
			&& rc.equals(bc) && door == null) {
		    blockCount++;
		}
	    }
	}

	return blockCount == blocks.size() ? goodReward : -1;
    }
}
