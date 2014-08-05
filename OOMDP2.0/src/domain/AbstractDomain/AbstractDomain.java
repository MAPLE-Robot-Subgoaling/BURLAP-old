package domain.AbstractDomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.fourroomsdomain.FourRooms;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SingleGoalPFRF;
import burlap.oomdp.singleagent.common.SinglePFTF;

public class AbstractDomain implements DomainGenerator {
	public Domain DOMAIN = null;
	public State state = null;
	public LearningAgent Q;
	public OOMDPPlanner planner;
	public RewardFunction rf;
	public TerminalFunction tf;
	
	
	public static void main(String[] args) {
		AbstractDomain ad = new AbstractDomain();
		
		FourRooms fr = new FourRooms();
		Domain newD = fr.generateDomain();
		Domain d = ad.generateDomain(newD);

		ad.DOMAIN = ad.removeAttributeFromClass(d.getObjectClass("agent"),d.getObjectClass("agent").getAttribute("x"));
		
		for (ObjectClass objc : ad.DOMAIN.getObjectClasses()) {
			System.out.print(objc.name + " ");
			
			for (Attribute a : objc.attributeList) {
				System.out.print(a.name + " " + a.discValues + " ");
			}
			
			System.out.println();
		}
	}
	
	public Domain generateDomain(Domain d){		
		if(DOMAIN != null)
			return DOMAIN;
		
		DOMAIN = d;
		
		rf = new SingleGoalPFRF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));
		tf = new SinglePFTF(DOMAIN.getPropFunction(FourRooms.PFATGOAL));
		DiscreteStateHashFactory hashFactory = new DiscreteStateHashFactory();
		hashFactory.setAttributesForClass("agent", DOMAIN.getObjectClass("agent").attributeList);
		Q = new QLearning(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.2, FourRooms.LEARNINGRATE, Integer.MAX_VALUE);		
		planner = new ValueIteration(DOMAIN, rf, tf, FourRooms.DISCOUNTFACTOR, hashFactory, 0.001, 100);
		
		return DOMAIN;
	}
	

	public Domain removeObjectClass(ObjectClass o) {
		Domain d = new SADomain();
		
		for (ObjectClass oc : DOMAIN.getObjectClasses()) {
			if (!oc.equals(o)) {
				d.addObjectClass(oc);
			}
		}
		DOMAIN = d;
		return d;
	}
	
	
	public void addAttributeToClass(ObjectClass o,Attribute a) {
		DOMAIN.getObjectClass(o.name).addAttribute(a);
	}
	
	public Domain removeAttributeFromClass(ObjectClass o,Attribute a){
		Domain d = new SADomain();
		ObjectClass copy;
		
		for (ObjectClass oc : DOMAIN.getObjectClasses()) {
			if (!oc.equals(o)) {
				d.addObjectClass(oc);
			} else {
				copy = new ObjectClass(DOMAIN,oc.name);
				
				for (Attribute attr : oc.attributeList) {
					if (!attr.equals(a)) {
						copy.addAttribute(attr);
					}
				}
				
				d.addObjectClass(copy);
			}	
		}
		
		return d;
	}

	public Domain generateDomain() {
		if (DOMAIN !=  null) {
			return DOMAIN;
		}
		
		DOMAIN = new SADomain();
		
		return DOMAIN;
	}
	
	public State getCleanState() {
		state = new State();
		return state;
	}
	
	
	public State addObjectInstance(ObjectInstance oi) {
		int i = 0;
		
		for (ObjectClass o : DOMAIN.getObjectClasses()) {
			if (oi.getName().contains(o.name)) {
				for (ObjectInstance oii : state.getAllObjects()) {
					if (oii.getName().contains(oi.getName())) {
						i += 1;
					}
				}
				
				oi.setName(o.name+i);
				
				state.addObject(oi);
				
			}
		}
		return state;
	}

}
