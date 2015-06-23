package burlap.behavior.singleagent.pod;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedTabularPolicy extends Policy {
    public class AbstractedTabularPolicyFactory extends AbstractedPolicyFactory<PolicyBlocksPolicy> {

	@Override
	public List<AbstractedPolicy> abstractAll(
		List<PolicyBlocksPolicy> policies) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public AbstractedPolicy merge(List<AbstractedPolicy> abstractedPolicies) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public Map<AbstractedPolicy, Double> powerMerge(
		List<PolicyBlocksPolicy> policies, int depth, int maxPol) {
	    // TODO Auto-generated method stub
	    return null;
	}
    }
    
    @Override
    public AbstractGroundedAction getAction(State s) {
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
