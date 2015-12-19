package burlap.behavior.singleagent.learning.modellearning.rmax;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.learning.modellearning.Model;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;

/**
 * @author James MacGlashan.
 */
public class UnmodeledFavoredPolicy extends Policy {

	protected Policy sourcePolicy;
	protected Model model;
	protected List<Action> allActions;

	public UnmodeledFavoredPolicy(Policy sourcePolicy, Model model,
			List<Action> actions) {
		this.sourcePolicy = sourcePolicy;
		this.model = model;
		this.allActions = actions;
	}

	@Override
	public AbstractGroundedAction getAction(State s) {

		List<AbstractGroundedAction> unmodeled = this.model
				.getUnmodeledActionsForState(s);

		if (unmodeled.size() > 0) {
			return unmodeled.get(RandomFactory.getMapped(0).nextInt(
					unmodeled.size()));
		}

		return this.sourcePolicy.getAction(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {

		List<AbstractGroundedAction> unmodeled = this.model
				.getUnmodeledActionsForState(s);

		if (unmodeled.size() > 0) {
			List<ActionProb> aps = new ArrayList<ActionProb>(unmodeled.size());
			double p = 1. / unmodeled.size();
			for (AbstractGroundedAction ga : unmodeled) {
				aps.add(new ActionProb(ga, p));
			}
			return aps;
		}

		return this.sourcePolicy.getActionDistributionForState(s);
	}

	@Override
	public boolean isDefinedFor(State s) {
		return true;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}
}
