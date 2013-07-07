package oomdptb.behavior.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oomdptb.behavior.QValue;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public abstract class ValueFunctionPlanner extends OOMDPPlanner implements QComputablePlanner{

	
	protected Map <StateHashTuple, List<ActionTransitions>>			transitionDynamics;
	protected Map <StateHashTuple, Double>							valueFunction;
	

	
	public abstract void planFromState(State initialState);
	
	
	
	
	public void VFPInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Map <String, List<Attribute>> attributesForHashCode){
		
		this.PlannerInit(domain, rf, tf, gamma, attributesForHashCode);
		
		this.transitionDynamics = new HashMap<StateHashTuple, List<ActionTransitions>>();
		this.valueFunction = new HashMap<StateHashTuple, Double>();
		
		
		
	}
	
	
	
	public List <QValue> getQs(State s){
		
		StateHashTuple sh = this.stateHash(s);
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		
		if(this.containsParameterizedActions){
			matching = s.getExactStateObjectMatchingTo(indexSH.s);
		}
		
		
		List <QValue> res = new ArrayList<QValue>();
		for(Action a : actions){
			List <GroundedAction> applications = s.getAllGroundedActionsFor(a);
			for(GroundedAction ga : applications){
				res.add(this.getQ(sh, ga, matching));
			}
		}
		
		return res;
		
	}
	
	
	public QValue getQ(State s, GroundedAction a){
		StateHashTuple sh = this.stateHash(s);
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		
		if(indexSH == null){
			//then this is an unexplored state
			indexSH = sh;
			mapToStateIndex.put(indexSH, indexSH);
		}
		
		if(this.containsParameterizedActions){
			matching = s.getExactStateObjectMatchingTo(indexSH.s);
		}
		return this.getQ(sh, a, matching);
	}
	
	
	protected QValue getQ(StateHashTuple sh, GroundedAction a, Map <String, String> matching){
		
		//translate grounded action if necessary
		GroundedAction ta = a;
		if(matching != null){
			ta = this.translateAction(ta, matching);
		}
		
		//find ActionTransition for the designated GA
		List <ActionTransitions> allTransitions = this.getActionsTransitions(sh);
		ActionTransitions matchingAt = null;
		for(ActionTransitions at : allTransitions){
			if(at.matchingTransitions(ta)){
				matchingAt = at;
				break;
			}
		}
		
		double q = this.computeQ(sh.s, matchingAt);
		
		return new QValue(sh.s, a, q);
	}
	
	
	protected List <ActionTransitions> getActionsTransitions(StateHashTuple sh){
		List <ActionTransitions> allTransitions = transitionDynamics.get(sh);
		
		if(allTransitions == null){
			//need to create them
			//first get all grounded actions for this state
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(sh.s.getAllGroundedActionsFor(a));
			}
			
			//now add transitions
			allTransitions = new ArrayList<ActionTransitions>(gas.size());
			for(GroundedAction ga : gas){
				ActionTransitions at = new ActionTransitions(sh.s, ga, attributesForHashCode);
				allTransitions.add(at);
			}
			
			//set it
			transitionDynamics.put(sh, allTransitions);
			
		}
		
		return allTransitions;
	}
	
	
	
	protected double computeQ(State s, ActionTransitions trans){
		
		double q = this.getDefaultValue(s);
		for(HashedTransitionProbability tp : trans.transitions){
			double r = rf.reward(s, trans.ga, tp.sh.s);
			double vp = this.getComputedVForSH(tp.sh);
			q += tp.p * (r + (this.gamma * vp));
		}
		
		
		return q;
	}
	
	protected double getDefaultValue(State s){
		return 0.;
	}
	
	protected double getComputedVForSH(StateHashTuple sh){
		Double res = valueFunction.get(sh);
		if(res == null){
			return this.getDefaultValue(sh.s);
		}
		return res;
	}
	
	
	
	
	
}
