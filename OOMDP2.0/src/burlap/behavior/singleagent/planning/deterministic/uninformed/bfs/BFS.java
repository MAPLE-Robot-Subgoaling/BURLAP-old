package burlap.behavior.singleagent.planning.deterministic.uninformed.bfs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SearchNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.DPrint;
import burlap.oomdp.auxiliary.common.NullTermination;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.common.UniformCostRF;

public class BFS extends DeterministicPlanner {

	public BFS(Domain domain, StateConditionTest gc, StateHashFactory hashingFactory){
		this.deterministicPlannerInit(domain, new UniformCostRF(), new NullTermination(), gc, hashingFactory);
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
			if(gc.satisfies(s)){
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
