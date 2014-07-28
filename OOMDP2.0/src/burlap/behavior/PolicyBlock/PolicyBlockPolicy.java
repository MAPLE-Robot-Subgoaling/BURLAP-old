package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.SubgoalOption;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.StateConditionTestIterable;
import burlap.behavior.singleagent.planning.commonpolicies.EpsilonGreedy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * For PolicyBlocks:
 * 
 * @author Tenji Tembo
 * 
 * This class replicates a policy defined by the intersection of a StateHashTuple
 * and it's corresponding grounded action. Note that the reward function and termination
 * function are used to write the policy to an episode analysis object for visualization.
 * This is a Deterministic Policy Object, no probability associated with this item.
 *
 */
public class PolicyBlockPolicy extends EpsilonGreedy {
	public Map<StateHashTuple, GroundedAction> policy;
	
	public PolicyBlockPolicy(double epsilon) {
		this(null, epsilon);
	}
	public PolicyBlockPolicy(QLearning qplanner, double epsilon) {
		super(qplanner, epsilon);
		policy = new HashMap<StateHashTuple, GroundedAction>();
	}
	
	public Option createOption(String name, TerminalFunction tf) {
		return new SubgoalOption(name, this, new StartPolicyTest(), new TFGoalCondition(tf));		
	}
	
	public void addEntry(State s, GroundedAction a) {
		policy.put(((OOMDPPlanner) qplanner).stateHash(s), a);
	}

	/**
	 * For getting the learned state-action mapping offline
	 * If Q-values are equal for two actions, it picks the first action
	 * @param s - the state
	 * @return the action corresponding to the state
	 */
	public AbstractGroundedAction getCorrectAction(State s) {
		List<QValue> qValues = super.qplanner.getQs(s);
		List<QValue> maxActions = new ArrayList<QValue>();
		maxActions.add(qValues.get(0));
		
		double maxQ = qValues.get(0).q;
		for (int i = 1; i < qValues.size(); i++) {
			QValue q = qValues.get(i);
			if (q.q == maxQ) {
				maxActions.add(q);
			} else if (q.q > maxQ) {
				maxActions.clear();
				maxActions.add(q);
				maxQ = q.q;
			}
		}
		
		return maxActions.get(0).a;
	}
	@Override
	public AbstractGroundedAction getAction(State s) {
		List<QValue> qValues = super.qplanner.getQs(s);
		AbstractGroundedAction corr = getCorrectAction(s);
		policy.put(((OOMDPPlanner) qplanner).stateHash(s), (GroundedAction) corr);
		
		double roll = rand.nextDouble();
		if (roll <= epsilon) {
			return qValues.get(rand.nextInt(qValues.size())).a;
		}
		
		return corr;
	}
	
	/*
	 * Badly named
	 * justDoIt() - takes a Policy Object and evaluates it's behavior with a 
	 * reward of 1, instead of the actual received reward. 
	 * --Note: all policyblock learning algorithms use a uniform reward of 1.
	 */
	public EpisodeAnalysis justDoIt() {
		EpisodeAnalysis result = new EpisodeAnalysis();
		int steps = 0;
		
		for (Entry<StateHashTuple, GroundedAction> e: policy.entrySet()) {
			// limiter - no action taken at goal state, so stop one before.
			if (steps >= policy.size() - 1) {
				break;
			}
			
			// add the information and increment
			result.addState(e.getKey().s);
			result.addAction(e.getValue());
			result.addReward(1);
			steps++;
		}
		
		return result;
	}
	
	/*
	 * evaluates the behavior but requires the reward function used
	 * in the learning algorithm previously 
	 */
	public EpisodeAnalysis evaluateBehavior(RewardFunction rf) {
		EpisodeAnalysis res = new EpisodeAnalysis();
		int size = 0;
		
		for (Entry<StateHashTuple, GroundedAction> e: policy.entrySet()) {
			if (size >= policy.size() - 1) {
				break;
			}
			
			res.addState(e.getKey().s);
			
			State cur = e.getKey().s;
			this.followAndRecordPolicy(res, cur, rf);
			size++;
		}
		
		return res;
	}
	
	/*
	 * Records the policy into an Episode analysis object. 
	 */
	private void followAndRecordPolicy(EpisodeAnalysis ea, State cur, RewardFunction rf) {
		State next = null;
		
		//follow Policy - no support for options (atm)
		GroundedAction ga = (GroundedAction) this.getAction(cur);
		
		if(ga == null) {
			throw new PolicyUndefinedException();
		}
		
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions) {
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(next, ga, r);
		}
	}
	
	public static class PolicyUndefinedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public PolicyUndefinedException() {
			super("Policy is undefined for provided state");
		}
	}
	
	public class StartPolicyTest implements StateConditionTestIterable {
		/*
		 * (non-Javadoc)
		 * @see burlap.behavior.singleagent.planning.StateConditionTest#satisfies(burlap.oomdp.core.State)
		 * This class is defined for the option methods. Checks if the state given exists in the policy.
		 */
		@Override
		public boolean satisfies(State s) {
			for (Entry<StateHashTuple, GroundedAction> e: policy.entrySet()) {
				if (e.getKey().equals(s)) {
					return true;
				}
			}
			
			return false;
		}

		@Override
		public Iterator<State> iterator() {
			return new Iterator<State>() {
				StateHashTuple[] ss = (StateHashTuple[]) policy.keySet().toArray();
				int i = 0;
				
				@Override
				public boolean hasNext() {
					return i < ss.length - 1;
				}

				@Override
				public State next() {
					State s = ss[i].s;
					i++;
					return s;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public void setStateContext(State s) { }
	}
}
