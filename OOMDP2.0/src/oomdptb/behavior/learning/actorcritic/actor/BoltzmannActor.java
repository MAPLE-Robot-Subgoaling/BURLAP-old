package oomdptb.behavior.learning.actorcritic.actor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.management.RuntimeErrorException;

import oomdptb.behavior.learning.actorcritic.Actor;
import oomdptb.behavior.learning.actorcritic.CritiqueResult;
import oomdptb.behavior.statehashing.StateHashFactory;
import oomdptb.behavior.statehashing.StateHashTuple;
import oomdptb.debugtools.RandomFactory;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;

public class BoltzmannActor extends Actor {

	protected List<Action>							actions;
	protected StateHashFactory						hashingFactory;
	protected double								learningRate;
	
	protected Map<StateHashTuple, PolicyNode>		preferences;
	
	protected Random								rand;
	
	protected boolean								containsParameterizedActions = false;
	
	
	public BoltzmannActor(Domain domain, StateHashFactory hashingFactory, double learningRate) {
		this.actions = new ArrayList<Action>(domain.getActions());
		this.hashingFactory = hashingFactory;
		this.learningRate = learningRate;
		
		this.preferences = new HashMap<StateHashTuple, BoltzmannActor.PolicyNode>();
		
		this.rand = RandomFactory.getMapped(0);
		
		for(Action a : actions){
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
				break;
			}
		}
		
	}

	@Override
	public void updateFromCritqique(CritiqueResult critqiue) {
		
		StateHashTuple sh = this.hashingFactory.hashState(critqiue.getS());
		PolicyNode node = this.preferences.get(sh);
		if(node == null){
			List <GroundedAction> gas = sh.s.getAllGroundedActionsFor(this.actions);
			node = new PolicyNode(sh);
			for(GroundedAction ga : gas){
				node.addPreference(new ActionPreference(ga, 0.0));
			}
			this.preferences.put(sh, node);
		}
		
		ActionPreference pref = this.getMatchingPreference(sh, critqiue.getA(), node);
		pref.preference += this.learningRate * critqiue.getCritique();
		

	}

	@Override
	public void addNonDomainReferencedAction(Action a) {
		
		if(!actions.contains(a)){
			this.actions.add(a);
			if(a.getParameterClasses().length > 0){
				containsParameterizedActions = true;
			}
		}
	}

	@Override
	public GroundedAction getAction(State s) {
		return this.sampleFromActionDistribution(s);
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		
		List <GroundedAction> gas = s.getAllGroundedActionsFor(this.actions);
		
		StateHashTuple sh = this.hashingFactory.hashState(s);
		PolicyNode node = this.preferences.get(sh);
		if(node == null){
			node = new PolicyNode(sh);
			for(GroundedAction ga : gas){
				node.addPreference(new ActionPreference(ga, 0.0));
			}
			this.preferences.put(sh, node);
		}
		
		List <ActionProb> probs = new ArrayList<ActionProb>(gas.size());
		double [] prefs = new double[gas.size()];
		for(int i = 0; i < node.preferences.size(); i++){
			prefs[i] = node.preferences.get(i).preference;
		}
		double max = this.max(prefs);
		double [] translatedP = this.getTranslatedPrefs(prefs, max);
		double sumexp = 0.;
		for(int i = 0; i < translatedP.length; i++){
			sumexp += Math.exp(translatedP[i]);
		}
		double loggedSumExp = Math.log(sumexp);
		double shift = max + loggedSumExp;
		
		for(int i = 0; i < prefs.length; i++){
			double p = Math.exp(prefs[i] - shift);
			if(Double.isNaN(p)){
				throw new RuntimeErrorException(new Error("Probability in Boltzmann policy distribution is NaN"));
			}
			ActionPreference ap = node.preferences.get(i);
			probs.add(new ActionProb(ap.ga, p));
		}
		
		if(this.containsParameterizedActions){
			//then convert back to this states space
			Map <String, String> matching = node.sh.s.getObjectMatchingTo(s, false);
			
			List <ActionProb> translated = new ArrayList<ActionProb>(probs.size());
			for(ActionProb ap : probs){
				if(ap.ga.params.length == 0){
					translated.add(ap);
				}
				else{
					ActionProb tap = new ActionProb(this.translateAction(ap.ga, matching), ap.pSelection);
					translated.add(tap);
				}
			}
			
			return translated;
			
		}
		
		
		return probs;
	}

	@Override
	public boolean isStochastic() {
		return true;
	}
	
	
	
	protected ActionPreference getMatchingPreference(StateHashTuple sh, GroundedAction ga, PolicyNode node){
		
		GroundedAction translatedAction = ga;
		if(ga.params.length > 0){
			Map <String, String> matching = sh.s.getObjectMatchingTo(node.sh.s, false);
			translatedAction = this.translateAction(ga, matching);
		}
		
		for(ActionPreference p : node.preferences){
			if(p.ga.equals(translatedAction)){
				return p;
			}
		}
		
		return null;
	}
	
	
	protected GroundedAction translateAction(GroundedAction a, Map <String,String> matching){
		String [] newParams = new String[a.params.length];
		for(int i = 0; i < a.params.length; i++){
			newParams[i] = matching.get(a.params[i]);
		}
		return new GroundedAction(a.action, newParams);
	}
	
	
	
	
	protected double [] getTranslatedPrefs(double [] prefs, double c){
		double [] translated = new double[prefs.length];
		for(int i = 0; i < prefs.length; i++){
			translated[i] = prefs[i] - c;
		}
		
		return translated;
	}
	
	protected double max(double [] darray){
		double max = Double.NEGATIVE_INFINITY;
		for(double d : darray){
			max = Math.max(max, d);
		}
		return max;
	}
	
	
	
	
	
	
	
	
	
	class PolicyNode{
		
		public StateHashTuple			sh;
		public List <ActionPreference>	preferences;
		
		
		public PolicyNode(StateHashTuple sh){
			this.sh = sh;
			this.preferences = new ArrayList<BoltzmannActor.ActionPreference>();
		}
		
		public void addPreference(ActionPreference pr){
			this.preferences.add(pr);
		}
		
		
	}
	
	
	class ActionPreference{
		
		public GroundedAction 	ga;
		public double			preference;
		
		public ActionPreference(GroundedAction ga, double preference){
			this.ga = ga;
			this.preference = preference;
		}
		
	}

}
