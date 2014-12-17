package burlap.behavior.singleagent.auxiliary;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;

/**
 * This class provides methods for finding the set of reachable states from a
 * source state.
 * 
 * @author James MacGlashan
 * 
 */
public class StateReachability {

    /**
     * The debugID used for making calls to {@link burlap.debugtools.DPrint}.
     */
    public static int debugID = 837493;

    /**
     * Returns the list of {@link burlap.oomdp.core.State} objects that are
     * reachable from a source state.
     * 
     * @param from
     *            the source state
     * @param inDomain
     *            the domain of the state
     * @param usingHashFactory
     *            the state hashing factory to use for indexing states and
     *            testing equality.
     * @return the list of {@link burlap.oomdp.core.State} objects that are
     *         reachable from a source state.
     */
    public static List<State> getReachableStates(State from, SADomain inDomain,
	    StateHashFactory usingHashFactory) {
	return getReachableStates(from, inDomain, usingHashFactory,
		new NullTermination());
    }

    /**
     * Returns the list of {@link burlap.oomdp.core.State} objects that are
     * reachable from a source state.
     * 
     * @param from
     *            the source state
     * @param inDomain
     *            the domain of the state
     * @param usingHashFactory
     *            the state hashing factory to use for indexing states and
     *            testing equality.
     * @param tf
     *            a terminal function that prevents expansion from terminal
     *            states.
     * @return the list of {@link burlap.oomdp.core.State} objects that are
     *         reachable from a source state.
     */
    public static List<State> getReachableStates(State from, SADomain inDomain,
	    StateHashFactory usingHashFactory, TerminalFunction tf) {
	Set<StateHashTuple> hashedStates = getReachableHashedStates(from,
		inDomain, usingHashFactory, tf);
	List<State> states = new ArrayList<State>(hashedStates.size());
	for (StateHashTuple sh : hashedStates) {
	    states.add(sh.s);
	}

	return states;
    }

    /**
     * Returns the set of {@link burlap.oomdp.core.State} objects that are
     * reachable from a source state.
     * 
     * @param from
     *            the source state
     * @param inDomain
     *            the domain of the state
     * @param usingHashFactory
     *            the state hashing factory to use for indexing states and
     *            testing equality.
     * @return the set of {@link burlap.oomdp.core.State} objects that are
     *         reachable from a source state.
     */
    public static Set<StateHashTuple> getReachableHashedStates(State from,
	    SADomain inDomain, StateHashFactory usingHashFactory) {
	return getReachableHashedStates(from, inDomain, usingHashFactory,
		new NullTermination());
    }

    /**
     * Returns the set of {@link burlap.oomdp.core.State} objects that are
     * reachable from a source state.
     * 
     * @param from
     *            the source state
     * @param inDomain
     *            the domain of the state
     * @param usingHashFactory
     *            the state hashing factory to use for indexing states and
     *            testing equality.
     * @param tf
     *            a terminal function that prevents expansion from terminal
     *            states.
     * @return the set of {@link burlap.oomdp.core.State} objects that are
     *         reachable from a source state.
     */
    public static Set<StateHashTuple> getReachableHashedStates(State from,
	    SADomain inDomain, StateHashFactory usingHashFactory,
	    TerminalFunction tf) {

	Set<StateHashTuple> hashedStates = new HashSet<StateHashTuple>();
	StateHashTuple shi = usingHashFactory.hashState(from);
	List<Action> actions = inDomain.getActions();
	int nGenerated = 0;

	LinkedList<StateHashTuple> openList = new LinkedList<StateHashTuple>();
	openList.offer(shi);
	hashedStates.add(shi);
	while (openList.size() > 0) {
	    StateHashTuple sh = openList.poll();

	    if (tf.isTerminal(sh.s)) {
		continue; // don't expand
	    }

	    List<GroundedAction> gas = sh.s.getAllGroundedActionsFor(actions);
	    for (GroundedAction ga : gas) {
		List<TransitionProbability> tps = ga.action.getTransitions(
			sh.s, ga.params);
		for (TransitionProbability tp : tps) {
		    StateHashTuple nsh = usingHashFactory.hashState(tp.s);
		    nGenerated++;
		    if (!hashedStates.contains(nsh)) {
			openList.offer(nsh);
			hashedStates.add(nsh);
		    }
		}

	    }

	}

	DPrint.cl(debugID, "Num generated: " + nGenerated + "; num unique: "
		+ hashedStates.size());

	return hashedStates;
    }
}
