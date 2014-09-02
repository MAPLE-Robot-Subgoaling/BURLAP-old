package domain.singleagent.sokoban2;

import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class Sokoban2RF extends UniformCostRF {
    private double badReward;
    
    public Sokoban2RF(double badReward) {
	this.badReward = badReward;
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

	    for (int j = 0; j < rooms.size(); j++) {
		if (Sokoban2Domain.wallCheck(sprime, rooms.get(j), bx + 1, by)
			|| Sokoban2Domain
				.wallCheck(sprime, rooms.get(j), bx - 1, by)
			|| Sokoban2Domain
				.wallCheck(sprime, rooms.get(j), bx, by + 1)
			|| Sokoban2Domain
				.wallCheck(sprime, rooms.get(j), bx, by - 1)) {
		    ObjectInstance door = Sokoban2Domain.doorContainingPoint(sprime, bx, by);
		    if (door != null) {
			continue;
		    }

		    // return badReward;
		}
	    }
	}

	return -1;
    }
}
