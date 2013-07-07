package oomdptb.behavior.learning.tdmethods;

import java.util.ArrayList;
import java.util.List;

import oomdptb.behavior.QValue;
import oomdptb.behavior.planning.StateHashTuple;
import oomdptb.oomdp.GroundedAction;

public class QLearningStateNode {

	public StateHashTuple			s;
	public List<QValue>				qEntry;
	
	
	public QLearningStateNode(StateHashTuple s) {
		this.s = s;
		qEntry = new ArrayList<QValue>();
	}

	public void addQValue(GroundedAction a, double q){
		QValue qv = new QValue(s.s, a, q);
		qEntry.add(qv);
	}
	
	
}
