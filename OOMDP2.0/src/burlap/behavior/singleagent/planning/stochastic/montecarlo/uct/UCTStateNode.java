package burlap.behavior.singleagent.planning.stochastic.montecarlo.uct;

import java.util.ArrayList;
import java.util.List;

import burlap.behavior.singleagent.planning.stochastic.montecarlo.uct.UCTActionNode.UCTActionConstructor;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * UCT State Node that wraps a hashed state object and provided additional state statistics necessary for UCT.
 * 
 * 
 * @author James MacGlashan
 *
 */
public class UCTStateNode {

	/**
	 * The (hashed) state this node wraps
	 */
	public StateHashTuple			state;
	
	/**
	 * The depth the UCT tree
	 */
	public int						depth;
	
	/**
	 * The number of times this node has been visited
	 */
	public int						n;
	
	/**
	 * The possible actions (nodes) that can be performed from this state.
	 */
	public List<UCTActionNode>		actionNodes;
	
	
	/**
	 * Initializes the UCT state node.
	 * @param s the state that this node wraps
	 * @param d the depth of the node
	 * @param actions the possible OO-MDP actions that can be taken
	 * @param constructor a {@link UCTActionNode} factory that can be used to create ActionNodes for each of the actions.
	 */
	public UCTStateNode(StateHashTuple s, int d, List <Action> actions, UCTActionConstructor constructor){
		
		state = s;
		depth = d;
		
		n = 0;
		
		actionNodes = new ArrayList<UCTActionNode>();
		
		for(Action a : actions){
			List <GroundedAction> gas = s.s.getAllGroundedActionsFor(a);
			for(GroundedAction ga : gas){
				UCTActionNode an = constructor.generate(ga);
				actionNodes.add(an);
			}
		}
		
		
		
	}
	
	
	@Override
	public boolean equals(Object o){
		
		if(!(o instanceof UCTStateNode)){
			return false;
		}
		
		UCTStateNode os = (UCTStateNode)o;
		
		return state.equals(os.state) && depth == os.depth;
		
	}
	
	
	
	/**
	 * A factory for generating UCTStateNode objects
	 * @author James MacGlashan
	 *
	 */
	public static class UCTStateConstructor{
		
		/**
		 * Generates an instance of a {@link UCTStateNode}
		 * @param s the state that this node wraps
		 * @param d the depth of the node
		 * @param actions the possible OO-MDP actions that can be taken
		 * @param constructor a {@link UCTActionNode} factory that can be used to create ActionNodes for each of the actions.
		 * @return a {@link UCTStateNode} instance.
		 */
		public UCTStateNode generate(StateHashTuple s, int d, List <Action> actions, UCTActionConstructor constructor){
			return new UCTStateNode(s, d, actions, constructor);
		}
		
		
	}
	
}
