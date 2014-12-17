package burlap.behavior.singleagent.learning.tdmethods;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * This class is used to store the associated
 * {@link burlap.behavior.singleagent.QValue} objects for a given hashed sated.
 * 
 * @author James MacGlashan
 * 
 */
public class QLearningStateNode {

    /**
     * A hashed state entry for which Q-value will be stored.
     */
    public StateHashTuple s;

    /**
     * The Q-values for this object's state.
     */
    public List<QValue> qEntry;

    /**
     * Creates a new object for the given hashed state. The list of
     * {@link burlap.behavior.singleagent.QValue} objects is initialized to be
     * empty.
     * 
     * @param s
     *            the hashed state for which to associate Q-values
     */
    public QLearningStateNode(StateHashTuple s) {
	this.s = s;
	qEntry = new ArrayList<QValue>();
    }

    /**
     * Adds a Q-value to this state with the given numeric Q-value.
     * 
     * @param a
     *            the action this Q-value is fore
     * @param q
     *            the numeric Q-value
     */
    public void addQValue(GroundedAction a, double q) {
	QValue qv = new QValue(s.s, a, q);
	qEntry.add(qv);
    }

}
