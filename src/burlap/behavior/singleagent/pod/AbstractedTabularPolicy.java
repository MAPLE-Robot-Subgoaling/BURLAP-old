package burlap.behavior.singleagent.pod;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import burlap.behavior.singleagent.pod.PolicyBlocksPolicy;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedTabularPolicy extends AbstractedPolicy<PolicyBlocksPolicy> {
    public class AbstractedTabularPolicyFactory extends AbstractedPolicyFactory<PolicyBlocksPolicy> {
	@Override
	public List<AbstractedPolicy<PolicyBlocksPolicy>> abstractAll(
		List<PolicyBlocksPolicy> policies) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public AbstractedPolicy<PolicyBlocksPolicy> merge(
		List<AbstractedPolicy<PolicyBlocksPolicy>> abstractedPolicies) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public Map<AbstractedPolicy<PolicyBlocksPolicy>, Double> powerMerge(
		List<PolicyBlocksPolicy> policies, int depth, int maxPol) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public State sampleState(PolicyBlocksPolicy policy) {
	    return policy.getPolicy().keySet().iterator().next().s;
	}
    }
    
    private AbstractedTabularPolicy() {
	super.gcg = new HashMap<ObjectClass, Integer>();
	super.apf = new AbstractedTabularPolicyFactory();
	super.originalPolicies = new ArrayList<PolicyBlocksPolicy>();
	super.hf = null;
    }
    
    public AbstractedTabularPolicy(AbstractedTabularPolicy other) {
	this();
	this.gcg = other.gcg;
	this.hf = other.hf;
	this.originalPolicies.addAll(other.originalPolicies);
	this.abstractedPolicy = other.abstractedPolicy;
    }
    
    public AbstractedTabularPolicy(StateHashFactory hf, PolicyBlocksPolicy ip, PolicyBlocksPolicy p) {
	this(hf, ip, AbstractedTabularPolicyFactory.singletonList(p));
    }
    
    public AbstractedTabularPolicy(StateHashFactory hf, PolicyBlocksPolicy ip, List<PolicyBlocksPolicy> ps) {
	this();
	if (ps.contains(ip)) {
	    ps.remove(ip);
	}
	
	State ipS = apf.sampleState(ip);
	List<State> psS = new ArrayList<State>(ps.size());
	for (PolicyBlocksPolicy p: ps) {
	    psS.add(apf.sampleState(p));
	}
	psS.add(ipS);
	
	// Generate the GCG respective to the sampled states
	gcg = AbstractedPolicyFactory.greatestCommonGeneralization(psS);
	List<List<ObjectClass>> oiCombs = AbstractedPolicyFactory.generateAllCombinations(ipS, gcg);
	List<Entry<PolicyBlocksPolicy, Double>> policyCandidates = generatePolicyCandidates(ip, oiCombs, 1);
	
	if (policyCandidates.size() > 0) {
	    abstractedPolicy = policyCandidates.get(0).getKey();
	}
	
	originalPolicies.addAll(ps);
	originalPolicies.add(ip);
    }

    @Override
    public List<Entry<PolicyBlocksPolicy, Double>> generatePolicyCandidates(PolicyBlocksPolicy ip, List<List<ObjectClass>> oiCombs, int n) {
	List<Entry<PolicyBlocksPolicy, Double>> policyCandidates = new ArrayList<Entry<PolicyBlocksPolicy, Double>>();

	for (List<ObjectClass> combination : oiCombs) {
	    Map<StateHashTuple, List<StateHashTuple>> stateOccurence = new HashMap<StateHashTuple, List<StateHashTuple>>();
	    Map<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();
	    for (Entry<StateHashTuple, GroundedAction> e : ip.getPolicy()
		    .entrySet()) {
		State curState = AbstractedPolicyFactory.formState(e.getKey().s, combination);
		List<StateHashTuple> temp;
		if (!stateOccurence.containsKey(hf.hashState(curState))) {
		    temp = new ArrayList<StateHashTuple>();
		    temp.add(e.getKey());
		    stateOccurence.put(hf.hashState(curState), temp);
		} else {
		    stateOccurence.get(hf.hashState(curState)).add(e.getKey());
		}
	    }

	    for (Entry<StateHashTuple, List<StateHashTuple>> e : stateOccurence
		    .entrySet()) {
		newPolicy.put(e.getKey(),
			getAction(ip, e.getKey().s, e.getValue()));
	    }

	    if (newPolicy.size() == 0) {
		continue;
	    }
	    
	    PolicyBlocksPolicy np = new PolicyBlocksPolicy(newPolicy, 0.);
	    double score = scoreAbstraction(ip, np);
	    boolean toSort = false;

	    if (policyCandidates.size() < n) {
		policyCandidates
			.add(new AbstractMap.SimpleEntry<PolicyBlocksPolicy, Double>(
				np, score));

		toSort = true;
	    } else {
		if (policyCandidates.get(policyCandidates.size() - 1)
			.getValue() < score) {
		    policyCandidates
			    .set(policyCandidates.size() - 1,
				    new AbstractMap.SimpleEntry<PolicyBlocksPolicy, Double>(
					    np, score));

		    toSort = true;
		}
	    }

	    if (toSort) {
		Collections
			.sort(policyCandidates,
				new Comparator<Entry<PolicyBlocksPolicy, Double>>() {
				    @Override
				    public int compare(
					    Entry<PolicyBlocksPolicy, Double> arg0,
					    Entry<PolicyBlocksPolicy, Double> arg1) {
					return -arg0.getValue().compareTo(
						arg1.getValue());
				    }
				});
	    }
	}

	return policyCandidates;
    }
    

    /**
     * Averages the Q-values and gets the best action
     * 
     * @param ip
     *            - initial policy (must have a QComputablePlanner present and
     *            non-null)
     * @param st
     *            - abstracted state to sample from
     * @param origStates
     *            - original states from ip that map into st
     * @return the best fitting action
     */
    private static GroundedAction getAction(PolicyBlocksPolicy ip, State st,
	    List<StateHashTuple> origStates) {
	List<List<QValue>> qs = new ArrayList<List<QValue>>();
	QLearning Q = ip.getLearner();
	for (StateHashTuple origState : origStates) {
	    QLearningStateNode qNode = Q.getStateNode(origState);
	    qs.add(qNode.qEntry);
	}

	return getActionFromQs(findAverages(st, qs));
    }

    private static GroundedAction getAction(List<GroundedAction> origStates) {
	// return mostCommonElement(origStates);
	return origStates.get(new Random().nextInt(origStates.size()));
    }

    /**
     * Gets the highest ranking action; dice roll if there are multiple
     * 
     * @param av
     *            - the list of state-action values
     * @return the best fitting action
     */
    private static GroundedAction getActionFromQs(List<QValue> av) {
	List<QValue> best = new ArrayList<QValue>();
	best.add(av.get(0));
	double highest = av.get(0).q;

	for (QValue q : av) {
	    if (q.q > highest) {
		highest = q.q;
		best.clear();
		best.add(q);
	    } else if (q.q == highest) {
		best.add(q);
	    }
	}

	Random rand = new Random();
	return (GroundedAction) best.get(rand.nextInt(best.size())).a;
    }

    /**
     * Averages all of the provided Q-values
     * 
     * @param s
     *            - initial state to sample from
     * @param toAverage
     *            - list of Q-values to average
     * @return the list of averages Q-values
     */
    private static List<QValue> findAverages(State s,
	    List<List<QValue>> toAverage) {
	List<QValue> averageQ = new ArrayList<QValue>();
	int i = 0;
	int j = 0;
	double sum = 0;
	double average = 0;

	while (i < toAverage.get(0).size()) {
	    sum = 0;

	    for (List<QValue> qList : toAverage) {
		sum += qList.get(i).q;
		j += 1;
	    }

	    average = sum / j;
	    j = 0;
	    QValue newQ = new QValue(s, toAverage.get(0).get(i).a, average);
	    averageQ.add(newQ);
	    i += 1;
	}

	return averageQ;
    }
    
    @Override
    public double scoreAbstraction(PolicyBlocksPolicy ip, PolicyBlocksPolicy np) {
	double accuracy = 0.;
	State withRespectTo = apf.sampleState(np);
	Map<StateHashTuple, List<Boolean>> correct = new HashMap<StateHashTuple, List<Boolean>>();
	List<ObjectClass> oiClasses = new ArrayList<ObjectClass>();

	for (ObjectInstance oi : withRespectTo.getAllObjects()) {
	    oiClasses.add(oi.getObjectClass());
	}

	for (Entry<StateHashTuple, GroundedAction> e : ip.getPolicy().entrySet()) {
	    State newS = AbstractedPolicyFactory.formState(e.getKey().s, oiClasses);

	    if (e.getValue().equals(np.getPolicy().get(hf.hashState(newS)))) {
		if (correct.get(hf.hashState(newS)) == null) {
		    List<Boolean> temp = new ArrayList<Boolean>();
		    temp.add(true);
		    correct.put(hf.hashState(newS), temp);
		} else {
		    correct.get(hf.hashState(newS)).add(true);
		}
	    }
	}

	for (List<Boolean> bs : correct.values()) {
	    double c = 0.;
	    for (Boolean b : bs) {
		if (b) {
		    c++;
		}
	    }

	    accuracy += (c / bs.size());
	}

	return accuracy / np.size();
    }
    
    @Override
    public AbstractedPolicy<PolicyBlocksPolicy> mergeWith(
	    AbstractedPolicy<PolicyBlocksPolicy> otherPolicy) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isSameAbstraction(
	    AbstractedPolicy<PolicyBlocksPolicy> otherPolicy) {
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