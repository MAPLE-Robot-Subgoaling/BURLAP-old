package oomdptb.behavior.planning;

import java.util.List;

import oomdptb.behavior.QValue;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public interface QComputablePlanner {

	public List <QValue> getQs(State s);
	public QValue getQ(State s, GroundedAction a);

}
