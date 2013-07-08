package oomdptb.behavior.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oomdptb.behavior.options.Option;
import oomdptb.debugtools.DPrint;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public abstract class OOMDPPlanner {

	protected Domain												domain;
	protected Map <String, List<Attribute>>							attributesForHashCode;
	protected RewardFunction										rf;
	protected TerminalFunction										tf;
	protected double												gamma;
	protected List <Action>											actions;
	
	protected Map <StateHashTuple, StateHashTuple>					mapToStateIndex; //this is useful because two states may be equal but have different object name references and this mapping lets the user pull out which exact state (and object names) was used for the action dynamics
	
	
	protected boolean												containsParameterizedActions;
	
	protected int													debugCode;
	
	
	public abstract void planFromState(State initialState);
	
	public void PlannerInit(Domain domain, RewardFunction rf, TerminalFunction tf, double gamma, Map <String, List<Attribute>> attributesForHashCode){
		
		this.domain = domain;
		this.rf = rf;
		this.tf = tf;
		this.gamma = gamma;
		this.attributesForHashCode = attributesForHashCode;
		
		mapToStateIndex = new HashMap<StateHashTuple, StateHashTuple>();
		
		containsParameterizedActions = false;
		List <Action> actions = domain.getActions();
		this.actions = new ArrayList<Action>(actions.size());
		for(Action a : actions){
			this.actions.add(a);
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
				break;
			}
		}
		
	}
	
	public final void addNonDomainReferencedAction(Action a){
		//make sure it doesn't already exist in the list
		if(!actions.contains(a)){
			actions.add(a);
			if(a instanceof Option){
				Option o = (Option)a;
				o.keepTrackOfRewardWith(rf, 1.);
				o.setExernalTermination(tf);
			}
			if(a.getParameterClasses().length > 0){
				this.containsParameterizedActions = true;
			}
		}
		
	}
	
	
	public void setAttributesForHashCode(Map<String, List<Attribute>> attributesForHashCode){
		this.attributesForHashCode = attributesForHashCode;
	}
	
	public void setAttributesForClass(String classname, List <Attribute> atts){
		if(attributesForHashCode == null){
			attributesForHashCode = new HashMap<String, List<Attribute>>();
		}
		attributesForHashCode.put(classname, atts);
	}
	
	public void addAttributeForClass(String classname, Attribute att){
		if(attributesForHashCode == null){
			attributesForHashCode = new HashMap<String, List<Attribute>>();
		}
		List <Attribute> atts = attributesForHashCode.get(classname);
		if(atts == null){
			atts = new ArrayList<Attribute>();
			attributesForHashCode.put(classname, atts);
		}
		//check if already there or not
		for(Attribute attInList : atts){
			if(attInList.name.equals(att.name)){
				return ;
			}
		}
		//if reached here then this att is not already added
		atts.add(att);
	}
	
	
	public TerminalFunction getTF(){
		return tf;
	}
	
	public RewardFunction getRF(){
		return rf;
	}
	
	public void setDebugCode(int code){
		this.debugCode = code;
	}
	
	public int getDebugCode(){
		return debugCode;
	}
	
	public void toggleDebugPrinting(boolean toggle){
		DPrint.toggleCode(debugCode, toggle);
	}
	
	protected GroundedAction translateAction(GroundedAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedAction(a.action, newParams);
	}
	
	
	public StateHashTuple stateHash(State s){
		return new StateHashTuple(s, attributesForHashCode);
	}
	
	
	protected List <GroundedAction> getAllGroundedActions(State s){
		
		List <GroundedAction> res = new ArrayList<GroundedAction>();
		
		for(Action a : actions){
			List <GroundedAction> gas = s.getAllGroundedActionsFor(a);
			res.addAll(gas);
		}
		
		return res;
		
	}
	
}
