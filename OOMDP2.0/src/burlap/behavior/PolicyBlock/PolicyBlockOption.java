package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class PolicyBlockOption extends Option {
	private Map<StateHashTuple, GroundedAction> staticPolicy;
	private StateHashFactory hashFactory;
	private List<Action> actions;
	
	public PolicyBlockOption(StateHashFactory shf, List<Action> actions, Map<StateHashTuple, GroundedAction> policy) {
		staticPolicy = policy;
		hashFactory = shf;
		this.actions = actions;
	}
	
	@Override
	public boolean isMarkov() {
		return true;
	}

	@Override
	public boolean usesDeterministicTermination() {
		return true;
	}

	@Override
	public boolean usesDeterministicPolicy() {
		return true;
	}

	@Override
	public double probabilityOfTermination(State s, String[] params) {
		if (staticPolicy.get(hashFactory.hashState(s)) == null)
			return 1.;
		
		return 0.;
	}

	@Override
	public void initiateInStateHelper(State s, String[] params) { }
	
	//private boolean flag = true;
	
	@Override
	public GroundedAction oneStepActionSelection(State s, String[] params) {
		/*if (flag) {
			flag = false;
			for (GroundedAction ga : staticPolicy.values()) {
				return ga;
			}
		}*/
		return staticPolicy.get(hashFactory.hashState(s));
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s,
			String[] params) {
		/*GroundedAction ga = staticPolicy.get(hashFactory.hashState(s));
		List<ActionProb> aprobs = new ArrayList<ActionProb>();
		for (Action a : actions) {
			if (ga.action.equals(a)) {
				ActionProb p = new ActionProb(new GroundedAction(a, a.getName()), 1.);
				aprobs.add(p);
			}
			else {
				ActionProb p = new ActionProb(new GroundedAction(a, a.getName()), 0.);
				aprobs.add(p);
			}
		}
		
		return aprobs;*/
		return new ArrayList<ActionProb>();
	}

	@Override
	public boolean applicableInState(State s, String [] params){
		return staticPolicy.get(hashFactory.hashState(s)) != null;
	}
}
