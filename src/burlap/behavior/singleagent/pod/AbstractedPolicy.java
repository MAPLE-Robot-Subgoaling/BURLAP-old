package burlap.behavior.singleagent.pod;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.ObjectClass;

public abstract class AbstractedPolicy<P extends Policy> {
    protected AbstractedPolicyFactory<P> apf;
    protected StateHashFactory hf;
    protected Map<ObjectClass, Integer> gcg;
    protected Set<P> originalPolicies;
    protected P abstractedPolicy;

    public abstract List<Entry<P, Double>> generatePolicyCandidates(P ip,
	    List<List<ObjectClass>> oiCombs, int n);

    public abstract double scoreAbstraction(P ip, P np);

    public abstract AbstractedPolicy<P> mergeWith(
	    AbstractedPolicy<P> otherPolicy);

    public abstract boolean isSameAbstraction(AbstractedPolicy<P> otherPolicy);

    public abstract Policy getPolicy();

    public Map<ObjectClass, Integer> getGCG() {
	return gcg;
    }

    public Set<P> getOriginalPolicies() {
	return originalPolicies;
    }

    public StateHashFactory getHashFactory() {
	return hf;
    }

    public abstract int size();
}
