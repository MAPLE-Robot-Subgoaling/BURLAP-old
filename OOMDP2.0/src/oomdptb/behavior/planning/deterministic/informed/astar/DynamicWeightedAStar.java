package oomdptb.behavior.planning.deterministic.informed.astar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oomdptb.behavior.options.Option;
import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.behavior.planning.deterministic.informed.Heuristic;
import oomdptb.behavior.planning.deterministic.informed.PrioritizedSearchNode;
import oomdptb.behavior.planning.deterministic.informed.astar.AStar;
import oomdptb.datastructures.HashIndexedHeap;
import oomdptb.behavior.planning.StateHashTuple;
import oomdptb.debugtools.DPrint;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.RewardFunction;
import oomdptb.oomdp.State;

public class DynamicWeightedAStar extends AStar {

	protected double										epsilon;
	protected int											expectedDepth;
	protected Map <StateHashTuple, Integer>					depthMap;
	protected int											lastComputedDepth;
	
	public DynamicWeightedAStar(Domain domain, RewardFunction rf, StateConditionTest gc, Map<String, List<Attribute>> attributesForHashCode,Heuristic heuristic, double epsilon, int expectedDepth) {
		super(domain, rf, gc, attributesForHashCode, heuristic);
		this.epsilon = epsilon;
		this.expectedDepth = expectedDepth;
	}
	
	@Override
	public void prePlanPrep(){
		super.prePlanPrep();
		depthMap = new HashMap<StateHashTuple, Integer>();
	}
	
	@Override
	public void postPlanPrep(){
		super.postPlanPrep();
		depthMap = null; //clear out to reclaim memory
	}
	
	@Override
	public void insertIntoOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode psn){
		super.insertIntoOpen(openQueue, psn);
		depthMap.put(psn.s, lastComputedDepth);
	}
	
	@Override
	public void updateOpen(HashIndexedHeap<PrioritizedSearchNode> openQueue, PrioritizedSearchNode openPSN, PrioritizedSearchNode npsn){
		super.updateOpen(openQueue, openPSN, npsn);
		depthMap.put(npsn.s, lastComputedDepth);
	}

	
	/*
	 * (non-Javadoc)
	 * @see edu.brown.cs.ai.behavior.oomdp.planning.deterministic.informed.BestFirst#planFromState(edu.umbc.cs.maple.oomdp.State)
	 * This method is being overriden because to avoid reopening closed states that are not actually better due to the dynamic
	 * h weight, the reopen check needs to be based on the g score, note the f score
	 */
	@Override
	public void planFromState(State initialState) {
		
		//first determine if there is even a need to plan
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return ; //no need to plan since this is already solved
		}
		
		
		//a plan is not cached so being planning process
		this.prePlanPrep();

		HashIndexedHeap<PrioritizedSearchNode> openQueue = new HashIndexedHeap<PrioritizedSearchNode>(new PrioritizedSearchNode.PSNComparator());
		Map<PrioritizedSearchNode, PrioritizedSearchNode> closedSet = new HashMap<PrioritizedSearchNode,PrioritizedSearchNode>();
		
		PrioritizedSearchNode ipsn = new PrioritizedSearchNode(sih, this.computeF(null, null, sih));
		this.insertIntoOpen(openQueue, ipsn);
		
		int nexpanded = 0;
		PrioritizedSearchNode lastVistedNode = null;
		double minF = ipsn.priority;
		while(openQueue.size() > 0){
			
			PrioritizedSearchNode node = openQueue.poll();
			closedSet.put(node, node);
			
			nexpanded++;
			if(node.priority < minF){
				minF = node.priority;
				DPrint.cl(debugCode, "Min F Expanded: " + minF + "; Nodes expanded so far: " + nexpanded + "; Open size: " + openQueue.size());
			}
			
			
			State s = node.s.s;
			if(gc.satisfies(s)){
				lastVistedNode = node;
				break;
			}
		
			//generate successors
			for(Action a : actions){
				List<GroundedAction> gas = s.getAllGroundedActionsFor(a);
				for(GroundedAction ga : gas){
					State ns = ga.executeIn(s);
					StateHashTuple nsh = this.stateHash(ns);
					
					double F = this.computeF(node, ga, nsh);
					PrioritizedSearchNode npsn = new PrioritizedSearchNode(nsh, ga, node, F);
					
					//check closed
					PrioritizedSearchNode closedPSN = closedSet.get(npsn);
					if(closedPSN != null){
						
						if(lastComputedCumR <= cumulatedRewardMap.get(closedPSN.s)){
							continue; //no need to reopen because this is a worse path to an already explored node
						}
						
					}
					
					
					//check open
					PrioritizedSearchNode openPSN = openQueue.containsInstance(npsn);
					if(openPSN == null){
						this.insertIntoOpen(openQueue, npsn);
					}
					else if(lastComputedCumR > cumulatedRewardMap.get(openPSN.s)){
						this.updateOpen(openQueue, openPSN, npsn);
					}
					
					
				}
				
				
			}
			
			
			
			
		}
		
		
		
		//search to goal complete. Now follow back pointers to set policy
		this.encodePlanIntoPolicy(lastVistedNode);
		
		DPrint.cl(debugCode, "Num Expanded: " + nexpanded);
		
		this.postPlanPrep();
		
	}
	
	
	
	@Override
	public double computeF(PrioritizedSearchNode parentNode, GroundedAction generatingAction, StateHashTuple successorState) {
		double cumR = 0.;
		double r = 0.;
		int d = 0;
		if(parentNode != null){
			double pCumR = cumulatedRewardMap.get(parentNode.s);
			r = rf.reward(parentNode.s.s, generatingAction, successorState.s);
			cumR = pCumR + r;
			
			int pD = depthMap.get(parentNode.s);
			if(generatingAction.action.isPrimitive()){
				d = pD + 1;
			}
			else{
				Option o = (Option)generatingAction.action;
				d = pD + o.getLastNumSteps();
			}
		}
		
		double H  = heuristic.h(successorState.s);
		lastComputedCumR = cumR;
		lastComputedDepth = d;
		double weightedE = this.epsilon * this.epsilonWeight(d);
		double F = cumR + ((1. + weightedE)*H);
		
		return F;
	}
	
	protected double epsilonWeight(int depth){
		
		double ratio = ((double)depth)/((double)expectedDepth);
		return Math.max(1.-ratio, 0.0);
		//return 1.;
		
	}
	
}
