package burlap.behavior.singleagent.learning.modellearning;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.planning.Planner;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.states.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * Interface for defining planning algorithms that operate on iteratively
 * learned models. Planning algorithms that operate on iteratively learned
 * models must support features for replanning when the model changes and
 * returning the policy of the plan under the current model.
 * 
 * @author James MacGlashan
 * 
 */
public interface ModelLearningPlanner extends Planner {

	/**
	 * This is method is expected to be called at the beginning of any new
	 * learning episode. This may be useful for planning algorithms that do not
	 * solve the policy for every state since new episodes may starts in states
	 * the planning algorithm had not previously considered. before a learning
	 * episode begins.
	 * 
	 * @param s
	 *            the input state
	 */
	public void initializePlannerIn(State s);

	/**
	 * Tells the valueFunction that the model has changed and that it will need
	 * to replan accordingly
	 * 
	 * @param changedState
	 *            the source state that caused a change in the model.
	 */
	public void modelChanged(State changedState);

	/**
	 * Returns a policy encoding the planner's results.
	 * 
	 * @return a policy object
	 */
	public Policy modelPlannedPolicy();

}
