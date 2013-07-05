package oomdptb.behavior.planning.deterministic;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.behavior.planning.OOMDPPlanner;
import oomdptb.behavior.planning.StateHashTuple;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public abstract class DeterministicPlanner extends OOMDPPlanner{

	protected StateConditionTest						gc;
	protected Map <StateHashTuple, GroundedAction>		internalPolicy;
	
	
	
	
	
	
	
	
	
	public void deterministicPlannerInit(Domain domain, RewardFunction rf, StateConditionTest gc, Map <String, List<Attribute>> attributesForHashCode){
		
		this.PlannerInit(domain, rf, null, 1., attributesForHashCode); //goal condition doubles as termination function for detemrinistic planners 
		this.gc = gc;
		this.internalPolicy = new HashMap<StateHashTuple, GroundedAction>();
	

	}
	

	
	public boolean cachedPlanForState(State s){
		StateHashTuple sh = this.stateHash(s);
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		return indexSH != null;
	}
	
	public GroundedAction querySelectedActionForState(State s){
		
		StateHashTuple sh = this.stateHash(s);
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		if(indexSH == null){
			this.planFromState(s);
			return internalPolicy.get(sh); //no need to translate because if the state didn't exist then it got indexed with this state's rep
		}
		
		//otherwise it's already computed
		GroundedAction res = internalPolicy.get(sh);
		
		//do object matching and return result
		if(containsParameterizedActions){
			Map<String,String> matching = indexSH.s.getExactStateObjectMatchingTo(s);
			for(int i = 0; i < res.params.length; i++){
				res.params[i] = matching.get(res.params[i]);
			}
		}
		
				
		return res;
		
		
	}
	
	
	protected void encodePlanIntoPolicy(SearchNode lastVisitedNode){
		
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			StateHashTuple bpsh = curNode.backPointer.s;
			if(!mapToStateIndex.containsKey(bpsh)){ //makes sure earlier plan duplicate nodes do not replace the correct later visits
				internalPolicy.put(bpsh, curNode.generatingAction);
				mapToStateIndex.put(bpsh, bpsh);
			}
			
			curNode = curNode.backPointer;
		}
	}
	
	
	protected boolean planContainsOption(SearchNode lastVisitedNode){
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			
			if(!curNode.generatingAction.action.isPrimitive()){
				return true;
			}
			
			curNode = curNode.backPointer;
		}
		return false;
	}
	
	protected boolean planHasDupilicateStates(SearchNode lastVisitedNode){
		
		Set<StateHashTuple> statesInPlan = new HashSet<StateHashTuple>();
		SearchNode curNode = lastVisitedNode;
		while(curNode.backPointer != null){
			if(statesInPlan.contains(curNode.s)){
				return true;
			}
			statesInPlan.add(curNode.s);
			curNode = curNode.backPointer;
		}
		return false;
		
	}
	
	
	
	
	
	
}
