package burlap.behavior.singleagent.pod;

import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;

public class AbstractedVFAPolicy extends AbstractedPolicy {
    private class AbstractedVFAPolicyFactory extends AbstractedPolicyFactory<Policy> {

	@Override
	public List<AbstractedPolicy> abstractAll(List<Policy> policies) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public AbstractedPolicy merge(List<AbstractedPolicy> abstractedPolicies) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public Map<AbstractedPolicy, Double> powerMerge(List<Policy> policies,
		int depth, int maxPol) {
	    // TODO Auto-generated method stub
	    return null;
	}
	
    }
    
    @Override
    public AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isSameAbstraction(AbstractedPolicy otherPolicy) {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public Policy getPolicy() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int size() {
	// TODO Auto-generated method stub
	return 0;
    }

}
