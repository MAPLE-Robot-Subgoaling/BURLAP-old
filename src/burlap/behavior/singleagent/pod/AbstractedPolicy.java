package burlap.behavior.singleagent.pod;

import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;

public abstract class AbstractedPolicy {
    AbstractedPolicyFactory<? extends Policy> apf;
    StateHashFactory hf;
    Map<String, Integer> gcg;
    List<? extends Policy> originalPolicies;

    public abstract AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy);

    public abstract boolean isSameAbstraction(AbstractedPolicy otherPolicy);

    public abstract Policy getPolicy();

    public Map<String, Integer> getGCG() {
	return gcg;
    }

    public List<? extends Policy> getOriginalPolicies() {
	return originalPolicies;
    }

    public StateHashFactory getHashFactory() {
	return hf;
    }

    public abstract int size();
}
