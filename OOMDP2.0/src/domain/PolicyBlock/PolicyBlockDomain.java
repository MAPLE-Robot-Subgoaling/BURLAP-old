package domain.PolicyBlock;

import burlap.domain.singleagent.gridworld.*;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.oomdp.core.*;

public class PolicyBlockDomain {

	GridWorldDomain policyDomain;
	Domain domain;
	StateParser sp;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalCondition;
	State initialState;
	DiscreteStateHashFactory hashFactory;
	
	
	public static void main(String[] args) {
		
	}

}
