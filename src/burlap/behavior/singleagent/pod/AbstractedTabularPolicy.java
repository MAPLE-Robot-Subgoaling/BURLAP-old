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
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedTabularPolicy extends
	AbstractedPolicy<PolicyBlocksPolicy> {
    public class AbstractedTabularPolicyFactory extends
	    AbstractedPolicyFactory<PolicyBlocksPolicy> {
	@Override
	public List<AbstractedPolicy<PolicyBlocksPolicy>> abstractAll(
		List<PolicyBlocksPolicy> policies) {
	    if (policies.size() < 2) {
		throw new IllegalArgumentException("Need at least 2 policies.");
	    }

	    List<AbstractedPolicy<PolicyBlocksPolicy>> abstractedPolicies = new ArrayList<AbstractedPolicy<PolicyBlocksPolicy>>();

	    for (int i = 0; i < policies.size(); i++) {
		List<PolicyBlocksPolicy> newPolicies = new ArrayList<PolicyBlocksPolicy>();
		newPolicies.addAll(policies);

		PolicyBlocksPolicy temp = newPolicies.remove(i);
		AbstractedPolicy<PolicyBlocksPolicy> absPolicy = new AbstractedTabularPolicy(
			hf, temp, newPolicies);
		abstractedPolicies.add(absPolicy);
	    }

	    return abstractedPolicies;
	}

	@Override
	public List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>> powerMerge(
		List<PolicyBlocksPolicy> policies, int depth, int maxPol) {
	    return powerMergeCache(policies, depth, maxPol, true,
		    new Integer[] { depth }).get(depth);
	}

	public Map<Integer, List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>>> powerMergeCache(
		List<PolicyBlocksPolicy> policies, int depth, int maxPol,
		boolean greedyMerge, Integer[] toCache) {
	    Map<Integer, List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>>> mappedPolicies = new HashMap<Integer, List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>>>(
		    toCache.length);

	    List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>> mergedPolicies = new ArrayList<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>>();
	    int c = 1;

	    for (List<PolicyBlocksPolicy> ps : getSubsets(policies, 2, depth)) {
		c++;
		if (!Arrays.asList(toCache).contains(c)) {
		    continue;
		}

		boolean toSort = false;
		AbstractedPolicy<PolicyBlocksPolicy> abs;

		if (greedyMerge) {
		    abs = greedyMerge(ps);
		} else {
		    abs = merge(abstractAll(ps));
		}

		if (abs.size() == 0) {
		    continue;
		}

		double score = scoreMerge(abs);

		if (mergedPolicies.size() < maxPol) {
		    mergedPolicies
			    .add(new AbstractMap.SimpleEntry<AbstractedPolicy<PolicyBlocksPolicy>, Double>(
				    abs, score));

		    toSort = true;
		} else if (score > mergedPolicies
			.get(mergedPolicies.size() - 1).getValue()) {
		    mergedPolicies
			    .set(mergedPolicies.size() - 1,
				    new AbstractMap.SimpleEntry<AbstractedPolicy<PolicyBlocksPolicy>, Double>(
					    abs, score));

		    toSort = true;
		}

		if (toSort) {
		    Collections
			    .sort(mergedPolicies,
				    new Comparator<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>>() {
					@Override
					public int compare(
						Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double> arg0,
						Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double> arg1) {
					    return -arg0.getValue().compareTo(
						    arg1.getValue());
					}

				    });
		}

		subtractAll(mergedPolicies);
		mappedPolicies.put(c, mergedPolicies);
	    }

	    return mappedPolicies;
	}

	@Override
	public void subtractAll(
		List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>> merged) {
	    for (int i = 0; i < merged.size(); i++) {
		Map<StateHashTuple, GroundedAction> toSubtract = merged.get(i)
			.getKey().abstractedPolicy.getPolicy();

		for (StateHashTuple sh : toSubtract.keySet()) {
		    for (int j = i + 1; j < merged.size(); j++) {
			merged.get(j).getKey().abstractedPolicy.policy
				.remove(sh);

			if (merged.get(j).getKey().size() == 0) {
			    merged.remove(j);
			}
		    }
		}
	    }
	}

	@Override
	public AbstractedPolicy<PolicyBlocksPolicy> merge(
		List<AbstractedPolicy<PolicyBlocksPolicy>> abstractedPolicies) {
	    if (abstractedPolicies == null || abstractedPolicies.isEmpty()) {
		throw new IllegalArgumentException(
			"Cannot pass a null or empty list of abstracted policies to merge.");
	    }

	    List<AbstractedPolicy<PolicyBlocksPolicy>> newPolicies = new ArrayList<AbstractedPolicy<PolicyBlocksPolicy>>();
	    newPolicies.addAll(abstractedPolicies);
	    AbstractedPolicy<PolicyBlocksPolicy> merged = new AbstractedTabularPolicy(
		    newPolicies.get(0));

	    for (int i = 1; i < newPolicies.size(); i++) {
		merged = merged.mergeWith(newPolicies.get(i));
	    }

	    return merged;
	}

	public AbstractedPolicy<PolicyBlocksPolicy> greedyMerge(
		List<PolicyBlocksPolicy> policies) {
	    List<PolicyBlocksPolicy> ps = policies;
	    List<State> ss = new ArrayList<State>();
	    for (int i = 0; i < ps.size(); i++) {
		// Sample a state from each source policy
		ss.add(sampleState(ps.get(i)));
	    }

	    Map<ObjectClass, Integer> gcg = greatestCommonGeneralization(ss);
	    List<ObjectClass> tempO = new ArrayList<ObjectClass>(gcg.keySet());
	    // Randomly order the gcg for now
	    Collections.shuffle(tempO);
	    final List<ObjectClass> ordering = tempO;
	    // ps <- [(3O, 2X), (2O, 3X), (3O, 3X)]
	    // gcg <- {'O': 2, 'X': 2}
	    // ordering <- ['O', 'X']

	    // Sort source policies according the the gcg's ordering
	    Collections.sort(ps, new Comparator<PolicyBlocksPolicy>() {
		@Override
		public int compare(PolicyBlocksPolicy arg0,
			PolicyBlocksPolicy arg1) {
		    State s0 = sampleState(arg0);
		    State s1 = sampleState(arg1);

		    for (int i = 0; i < ordering.size(); i++) {
			Integer c0 = s0.getObjectsOfTrueClass(
				ordering.get(i).name).size();
			Integer c1 = s1.getObjectsOfTrueClass(
				ordering.get(i).name).size();

			if (c0 == c1) {
			    continue;
			} else {
			    // Reverse order to allow the most counted object
			    // classes to be first
			    return c1.compareTo(c0);
			}
		    }

		    return 0;
		}
	    });

	    // ps <- [(3O, 3X), (3O, 2X), (2O, 3X)]
	    AbstractedPolicy<PolicyBlocksPolicy> finalAbs = naiveAbstractAll(
		    singletonList(ps.get(0))).get(0);
	    Set<PolicyBlocksPolicy> originals = new HashSet<PolicyBlocksPolicy>(
		    policies);
	    finalAbs.originalPolicies = originals;
	    for (int i = 1; i < ps.size(); i++) {
		if (finalAbs.size() == 0) {
		    return finalAbs;
		}

		// Through every policy
		AbstractedPolicy<PolicyBlocksPolicy> curAbs = naiveAbstractAll(
			singletonList(ps.get(i))).get(0);
		curAbs.originalPolicies = originals;

		State s0 = sampleState(finalAbs);
		State s1 = sampleState(curAbs);
		Map<ObjectClass, Integer> m0 = getObjectCounts(s0);
		Map<ObjectClass, Integer> m1 = getObjectCounts(s1);

		while (true) {
		    boolean flag = true;
		    Set<ObjectClass> objSet = new HashSet<ObjectClass>();
		    objSet.addAll(m0.keySet());
		    objSet.addAll(m1.keySet());

		    for (ObjectClass obj : objSet) {
			if (m0.get(obj) == null) {
			    m0.put(obj, 0);
			}
			if (m1.get(obj) == null) {
			    m1.put(obj, 0);
			}

			if (m0.get(obj) < 1 && m1.get(obj) < 1) {
			} else if (m0.get(obj) > m1.get(obj)) {
			    m0.put(obj, m0.get(obj) - 1);
			    finalAbs = new AbstractedTabularPolicy(hf,
				    finalAbs, originals, m0);

			    flag = false;
			    break;
			} else if (m1.get(obj) > m0.get(obj)) {
			    m1.put(obj, m1.get(obj) - 1);
			    curAbs = new AbstractedTabularPolicy(hf, curAbs,
				    originals, m1);

			    flag = false;
			    break;
			}
		    }

		    if (flag) {
			break;
		    }
		}

		finalAbs = finalAbs.mergeWith(curAbs);
	    }

	    return finalAbs;
	}

	@Override
	public double scoreMerge(AbstractedPolicy<PolicyBlocksPolicy> np) {
	    double totalMatch = 0.;

	    for (PolicyBlocksPolicy orig : np.getOriginalPolicies()) {
		PolicyBlocksPolicy tempP = new PolicyBlocksPolicy(
			np.abstractedPolicy.getPolicy(), orig.getEpsilon());

		// Abstracts the original policy with respect to the abstracted
		// policy
		AbstractedPolicy<PolicyBlocksPolicy> origAb = new AbstractedTabularPolicy(
			hf, orig, tempP);
		double stateSize = origAb.abstractedPolicy.size();
		double stateMatch = 0.;

		for (Entry<StateHashTuple, GroundedAction> e : np.abstractedPolicy
			.getPolicy().entrySet()) {
		    if (e.getValue()
			    .equals(origAb.abstractedPolicy.getPolicy().get(
				    e.getKey()))) {
			// Check for a state->action match
			stateMatch += 1;
		    }
		}

		totalMatch += stateMatch / stateSize;
	    }

	    return totalMatch;
	}

	private AbstractedPolicy<PolicyBlocksPolicy> naiveAbstraction(
		PolicyBlocksPolicy p, List<PolicyBlocksPolicy> origPs) {
	    AbstractedPolicy<PolicyBlocksPolicy> abs = new AbstractedTabularPolicy();
	    abs.hf = hf;
	    abs.originalPolicies.add(p);
	    abs.originalPolicies.addAll(origPs);
	    abs.abstractedPolicy = p;
	    abs.gcg = greatestCommonGeneralization(singletonList(sampleState(p)));

	    return abs;
	}

	/**
	 * Abstracts all provides policies with respect to themselves
	 * 
	 * @param hf
	 *            - state hash factory
	 * @param policies
	 *            - policies to abstract
	 * @return list of abstracted policies
	 */
	private List<AbstractedPolicy<PolicyBlocksPolicy>> naiveAbstractAll(
		List<PolicyBlocksPolicy> policies) {
	    List<AbstractedPolicy<PolicyBlocksPolicy>> abstractedPolicies = new ArrayList<AbstractedPolicy<PolicyBlocksPolicy>>();

	    for (int i = 0; i < policies.size(); i++) {
		List<PolicyBlocksPolicy> newPolicies = new ArrayList<PolicyBlocksPolicy>();
		newPolicies.addAll(policies);

		PolicyBlocksPolicy temp = newPolicies.remove(i);
		AbstractedPolicy<PolicyBlocksPolicy> absPolicy = naiveAbstraction(
			temp, newPolicies);
		abstractedPolicies.add(absPolicy);
	    }

	    return abstractedPolicies;
	}

	@Override
	public State sampleState(PolicyBlocksPolicy policy) {
	    return policy.getPolicy().keySet().iterator().next().s;
	}

	@Override
	public State sampleState(AbstractedPolicy<PolicyBlocksPolicy> policy) {
	    return policy.abstractedPolicy.getPolicy().keySet().iterator()
		    .next().s;
	}
    }

    private AbstractedTabularPolicy() {
	super.gcg = new HashMap<ObjectClass, Integer>();
	super.apf = new AbstractedTabularPolicyFactory();
	super.originalPolicies = new HashSet<PolicyBlocksPolicy>();
	super.hf = null;
    }

    public AbstractedTabularPolicy(AbstractedPolicy<PolicyBlocksPolicy> other) {
	this();
	this.gcg = other.gcg;
	this.hf = other.hf;
	this.originalPolicies.addAll(other.originalPolicies);
	this.abstractedPolicy = other.abstractedPolicy;
    }

    public AbstractedTabularPolicy(StateHashFactory hf, PolicyBlocksPolicy ip,
	    PolicyBlocksPolicy p) {
	this(hf, ip, AbstractedTabularPolicyFactory.singletonList(p));
    }

    private AbstractedTabularPolicy(StateHashFactory hf,
	    AbstractedPolicy<PolicyBlocksPolicy> ip,
	    Set<PolicyBlocksPolicy> sourcePolicies,
	    Map<ObjectClass, Integer> gcg) {
	this();
	this.hf = hf;
	this.originalPolicies = sourcePolicies;

	List<List<ObjectClass>> oiCombs = AbstractedPolicyFactory
		.generateAllCombinations(gcg, apf.sampleState(ip));
	List<Entry<PolicyBlocksPolicy, Double>> policyCandidates = generatePolicyCandidates(
		ip.abstractedPolicy, oiCombs, 1);

	if (policyCandidates.size() > 0) {
	    this.abstractedPolicy = policyCandidates.get(0).getKey();
	}
    }

    public AbstractedTabularPolicy(StateHashFactory hf, PolicyBlocksPolicy ip,
	    List<PolicyBlocksPolicy> ps) {
	this();
	if (ps.contains(ip)) {
	    ps.remove(ip);
	}

	State ipS = apf.sampleState(ip);
	List<State> psS = new ArrayList<State>(ps.size());
	for (PolicyBlocksPolicy p : ps) {
	    psS.add(apf.sampleState(p));
	}
	psS.add(ipS);

	// Generate the GCG respective to the sampled states
	gcg = AbstractedPolicyFactory.greatestCommonGeneralization(psS);
	List<List<ObjectClass>> oiCombs = AbstractedPolicyFactory
		.generateAllCombinations(gcg, ipS);
	List<Entry<PolicyBlocksPolicy, Double>> policyCandidates = generatePolicyCandidates(
		ip, oiCombs, 1);

	if (policyCandidates.size() > 0) {
	    abstractedPolicy = policyCandidates.get(0).getKey();
	}

	originalPolicies.addAll(ps);
	originalPolicies.add(ip);
    }

    @Override
    public List<Entry<PolicyBlocksPolicy, Double>> generatePolicyCandidates(
	    PolicyBlocksPolicy ip, List<List<ObjectClass>> oiCombs, int n) {
	List<Entry<PolicyBlocksPolicy, Double>> policyCandidates = new ArrayList<Entry<PolicyBlocksPolicy, Double>>();

	for (List<ObjectClass> combination : oiCombs) {
	    Map<StateHashTuple, List<StateHashTuple>> stateOccurence = new HashMap<StateHashTuple, List<StateHashTuple>>();
	    Map<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();
	    for (Entry<StateHashTuple, GroundedAction> e : ip.getPolicy()
		    .entrySet()) {
		State curState = AbstractedPolicyFactory.formState(
			e.getKey().s, combination);
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
		Collections.sort(policyCandidates,
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

	for (Entry<StateHashTuple, GroundedAction> e : ip.getPolicy()
		.entrySet()) {
	    State newS = AbstractedPolicyFactory.formState(e.getKey().s,
		    oiClasses);

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
	if (!isSameAbstraction(otherPolicy)) {
	    throw new IllegalArgumentException(
		    "Not the same level of abstraction.");
	}

	Map<StateHashTuple, GroundedAction> merged = new HashMap<StateHashTuple, GroundedAction>();
	State withRespectTo = apf.sampleState(otherPolicy).copy();

	for (Entry<StateHashTuple, GroundedAction> e : abstractedPolicy
		.getPolicy().entrySet()) {
	    GroundedAction ga;
	    if (hf instanceof DiscreteStateHashFactory) {
		ga = otherPolicy.abstractedPolicy.getPolicy().get(e.getKey());
		if (e.getValue().equals(ga)) {
		    merged.put(e.getKey(), e.getValue());
		}
	    } else {
		List<State> possibleStates = AbstractedPolicyFactory
			.generatePossibleStates(e.getKey().s, withRespectTo);
		// This is done in the event that while two abstractions will
		// share
		// the GCG, they may have different object names (e.g. [p1, p2]
		// versus [p3, p2])
		// DiscreteStateHashFactory doesn't care about names, but this
		// is in
		// here just as a safety measure for other hashing methods.
		for (State possibleState : possibleStates) {
		    ga = otherPolicy.abstractedPolicy.getPolicy().get(
			    hf.hashState(possibleState));
		    if (e.getValue().equals(ga)) {
			merged.put(e.getKey(), e.getValue());
		    }
		}
	    }
	}

	AbstractedPolicy<PolicyBlocksPolicy> ret = new AbstractedTabularPolicy();
	ret.hf = hf;
	ret.gcg = gcg;
	ret.originalPolicies.addAll(originalPolicies);
	ret.apf = apf;
	ret.abstractedPolicy = new PolicyBlocksPolicy(merged, 0.);

	return ret;
    }

    @Override
    public boolean isSameAbstraction(
	    AbstractedPolicy<PolicyBlocksPolicy> otherPolicy) {
	for (ObjectClass c : gcg.keySet()) {
	    if (gcg.get(c) != otherPolicy.gcg.get(c)) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public Policy getPolicy() {
	return abstractedPolicy;
    }

    @Override
    public int size() {
	return abstractedPolicy.size();
    }
}