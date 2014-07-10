package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.PolicyBlock.PolicyBlockDomain;
import domain.fourroomsdomain.FourRooms;
import domain.taxiworld.TaxiWorldDomain;
import examples.BasicBehavior;
import burlap.oomdp.core.*;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.ValueFunctionPlanner;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyDeterministicQPolicy;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.statehashing.DiscreteMaskHashingFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class AbstractedPolicy extends Policy {
	
	
	public static void main(String args[]){	
		
		PolicyBlockPolicy pbp = new PolicyBlockPolicy(.99);		
		
		PolicyGenerator generator = new PolicyGenerator("PolicyBlocks/");
		generator.generatePolicies("GW-", 3);
		//generator.environ.getPolicyMap().get();
		
		Map <String, List <ObjectInstance>>	objectIndexByTrueClass = new HashMap <String, List <ObjectInstance>>();;
		List<ObjectInstance> tmp = objectIndexByTrueClass.get("agent");
		
		//ArrayList<ObjectInstance> hello = new ArrayList <ObjectInstance>(tmp);
		
		GridWorldDomain policyBlock = new GridWorldDomain(11,11);
	
		TaxiWorldDomain txd = new TaxiWorldDomain();
		txd.MAXPASS = 2;
        Domain d = txd.generateDomain();
		
		AbstractedPolicy ap = new AbstractedPolicy(d);
		
		//ObjectInstance o = s.getObjectsOfTrueClass("agent").get(0);
		
		//Policy p = new GreedyDeterministicQPolicy((QComputablePlanner)plan);
		//p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + str + k, sp);	
		//generator.printStates();
		
		//generator.writePolicies();
	}

	public AbstractedPolicy(Domain d) {
		List<ObjectClass> allObj = d.getObjectClasses();
		List<ObjectClass> tempObj = new ArrayList<ObjectClass>();
		List<Attribute> tempAttr;
		
		for (ObjectClass obj : allObj) tempObj.add(obj);
		
		for (ObjectClass o : allObj) {
			
			
			
			List <Attribute> attrList = o.attributeList;
			
			for (Attribute a : attrList) {
				
				
				//tempObj = allObj;
				//tempObj.remove(o);

			}
		}
		
		
	//	for (Attribute attr : domainAttr) {
		//	System.out.println(attr.name);
		//}
		
		
		
		//p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + str + k, sp);	
		//here we need to get all the objects out that are in the policy
		
		//ValueFunctionPlanner plan = new ValueIteration(d, d.getRf(), d.getTf(), d.getDISCOUNTFACTOR(), d.getHashFactory(), 0.001, 100);
		//Policy p1 = new GreedyDeterministicQPolicy((QComputablePlanner)plan);
		//p1.evaluateBehavior(s,d.getRf(),d.getTf());
		
		
		//QLearning Q = new QLearning(d,d.rf,d.tf,d.DISCOUNTFACTOR,d.hashFactory, 0.2,.99, Integer.MAX_VALUE);

		//PolicyBlockPolicy abstracted = new PolicyBlockPolicy(Q,.99);
		

	}

	@Override
	public GroundedAction getAction(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isStochastic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefinedFor(State s) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
}