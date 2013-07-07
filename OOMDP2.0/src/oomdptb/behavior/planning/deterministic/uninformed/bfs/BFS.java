package oomdptb.behavior.planning.deterministic.uninformed.bfs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oomdptb.behavior.planning.StateConditionTest;
import oomdptb.behavior.planning.deterministic.DeterministicPlanner;
import oomdptb.behavior.planning.deterministic.SearchNode;
import oomdptb.behavior.planning.StateHashTuple;
import oomdptb.debugtools.DPrint;
import oomdptb.oomdp.Action;
import oomdptb.oomdp.Attribute;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.GroundedAction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.common.UniformCostRF;

public class BFS extends DeterministicPlanner {

	public BFS(Domain domain, StateConditionTest gc, Map <String, List<Attribute>> attributesForHashCode){
		this.deterministicPlannerInit(domain, new UniformCostRF(), gc, attributesForHashCode);
	}
	
	
	@Override
	public void planFromState(State initialState) {
		
		StateHashTuple sih = this.stateHash(initialState);
		
		if(mapToStateIndex.containsKey(sih)){
			return ; //no need to plan since this is already solved
		}
		
		
		LinkedList<SearchNode> openQueue = new LinkedList<SearchNode>();
		Set <SearchNode> openedSet = new HashSet<SearchNode>();
		
		
		SearchNode initialSearchNode = new SearchNode(sih);
		openQueue.offer(initialSearchNode);
		openedSet.add(initialSearchNode);
		
		SearchNode lastVistedNode = null;
		
		
		int nexpanded = 0;
		while(openQueue.size() > 0){
			
			SearchNode node = openQueue.poll();
			nexpanded++;
			
			
			
			State s = node.s.s;
			if(gc.satisfies(s) || mapToStateIndex.containsKey(node.s)){
				lastVistedNode = node;
				break;
			}
			
			//first get all grounded actions for this state
			List <GroundedAction> gas = new ArrayList<GroundedAction>();
			for(Action a : actions){
				gas.addAll(s.getAllGroundedActionsFor(a));
			}
			
			
			
			//add children reach from each deterministic action
			for(GroundedAction ga : gas){
				State ns = ga.executeIn(s);
				StateHashTuple nsh = this.stateHash(ns);
				SearchNode nsn = new SearchNode(nsh, ga, node);
				
				if(openedSet.contains(nsn)){
					continue;
				}
				
				//otherwise add for expansion
				openQueue.offer(nsn);
				openedSet.add(nsn);
				
				
			}
			
			
		}
		
	
		
		this.encodePlanIntoPolicy(lastVistedNode);

		
		DPrint.cl(debugCode,"Num Expanded: " + nexpanded);
		
	}

}
