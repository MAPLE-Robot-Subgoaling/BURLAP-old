package domain.PolicyBlock;

import java.util.HashMap;
import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
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
 * This is a Determinisitc Policy Object, no probability asssociate with this item.
 *
 */
public class PolicyBlockPolicy extends Policy{
	
	//Main Data Struture to Simulate a merged Policy
	public HashMap<StateHashTuple, GroundedAction> stateSpace;
	
	//Main Policy Constructor, creates a blank 
	public PolicyBlockPolicy(){
		stateSpace = new HashMap<StateHashTuple, GroundedAction>();
	}
	
	//adds elements to the end of the StateHashTuple
	public void addEntry(StateHashTuple s, GroundedAction a){
		stateSpace.put(s, a);
	}
	
	//Secondary PolicyBlock Constructor
	public PolicyBlockPolicy(HashMap<StateHashTuple, GroundedAction> stateSpace){
		this.stateSpace = stateSpace;
	}

	//Retrieves the action from the Mapping
	@Override
	public GroundedAction getAction(State s) {
		// TODO Auto-generated method stub
		
		for(StateHashTuple item: stateSpace.keySet()){
			if(s.equals(item.s)){
				return stateSpace.get(item);
			}
		}
		
		return null;
	}

	//Will always return the correspond item for deterministic
	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		// TODO Auto-generated method stub
		return this.getDeterministicPolicy(s);
	}

	//Is a Determinisitic Policy
	@Override
	public boolean isStochastic() {
		// TODO Auto-generated method stub
		return false;
	}

	
	@Override
	public boolean isDefinedFor(State s) {
		// TODO Auto-generated method stub
		return true;
	}
	
	public EpisodeAnalysis evaluateBehavior(RewardFunction rf){
		EpisodeAnalysis res = new EpisodeAnalysis();
		
		int size = 0;
		
		for(StateHashTuple s: stateSpace.keySet()){
			
			if(size <= stateSpace.keySet().size() - 1)
				break;
			
			res.addState(s.s); //add the state
			
			State cur = s.s;
			this.followAndRecordPolicy(res, cur, rf);
			size++;
		}
		
		return res;
	}
	
	private void followAndRecordPolicy(EpisodeAnalysis ea, State cur, RewardFunction rf){
		State next = null;
		
		//follow Policy - no support for options (atm)
		GroundedAction ga = this.getAction(cur);
		
		if(ga == null){
			throw new PolicyUndefinedException();
		}
		
		if(ga.action.isPrimitive() || !this.evaluateDecomposesOptions){
			next = ga.executeIn(cur);
			double r = rf.reward(cur, ga, next);
			
			//record result
			ea.recordTransitionTo(next, ga, r);
		}
	}
	
	public static class PolicyUndefinedException extends RuntimeException{

		private static final long serialVersionUID = 1L;
		
		public PolicyUndefinedException(){
			super("Policy is undefined for provided state");
		}
		
	}

}
