package burlap.behavior.policyblocks;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedPolicy {
    private Map<StateHashTuple, GroundedAction> abstractedPolicy;
    private Set<PolicyBlocksPolicy> originalPolicies;
    private StateHashFactory hashFactory;

    private AbstractedPolicy() {
	this.abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
	this.originalPolicies = new HashSet<PolicyBlocksPolicy>();
    }

    public AbstractedPolicy(AbstractedPolicy p) {
	this();
	this.abstractedPolicy.putAll(p.abstractedPolicy);
	this.originalPolicies.addAll(p.originalPolicies);
	this.hashFactory = p.hashFactory;
    }

    /**
     * Accepts a single initial policy and a set of other initial policies,
     * abstracts a single abstract policy (the abstraction of the first initial
     * policy relative to the others)
     * 
     * @param sh
     *            - StateHashFactory is used to go from State - > StateHashTuple
     *            after abstraction
     * @param initialPolicy
     * @param policyList
     */
    public AbstractedPolicy(StateHashFactory hf, PolicyBlocksPolicy ip,
	    List<PolicyBlocksPolicy> ps) {
	this();
	this.hashFactory = hf;
	// Generate the GCI of the state
	// Generate every combination of GCI mappings for the original policy
	// Score each generated combination of GCI mappings
	State ipS = ip.policy.keySet().iterator().next().s;
	List<State> psS = new ArrayList<State>();
	for (PolicyBlocksPolicy p : ps) {
	    psS.add(p.policy.keySet().iterator().next().s);
	}
	psS.add(ipS);
	Map<String, Integer> gci = greatestCommonIntersection(psS);
	psS.remove(ipS);

	List<List<String>> oiCombs = generateAllCombinations(ipS, gci);

	List<Map<StateHashTuple, GroundedAction>> policyCandidates = generatePolicyCandidates(
		hf, ip, oiCombs);

	this.abstractedPolicy = getBestCandidate(hf, ip, policyCandidates);
	ps.add(ip);
	this.originalPolicies.addAll(ps);
    }

    /**
     * Abstracts each policy with respect to each other
     * 
     * @param policies
     *            - the list of policies to be abstracted with respect to
     *            another
     * @return the list of abstracted policies
     */
    public static List<AbstractedPolicy> abstractAll(StateHashFactory hf,
	    List<PolicyBlocksPolicy> policies) {
	if (policies.size() < 2) {
	    throw new IllegalArgumentException("Need at least 2 policies.");
	}

	List<AbstractedPolicy> abstractedPolicies = new ArrayList<AbstractedPolicy>();
	for (int i = 0; i < policies.size(); i++) {
	    List<PolicyBlocksPolicy> newPolicies = new ArrayList<PolicyBlocksPolicy>();
	    newPolicies.addAll(policies);

	    PolicyBlocksPolicy temp = newPolicies.remove(i);
	    abstractedPolicies.add(new AbstractedPolicy(hf, temp, newPolicies));
	    newPolicies.add(temp);
	}

	return abstractedPolicies;
    }

    /**
     * Finds the greatest common intersection between all states
     * 
     * @param ss
     * @return a mapping from object class to occurrence
     */
    public static Map<String, Integer> greatestCommonIntersection(List<State> ss) {
	List<Map<String, Integer>> mappings = new ArrayList<Map<String, Integer>>(
		ss.size());
	Map<String, List<Attribute>> attributes = new HashMap<String, List<Attribute>>();
	Map<String, Integer> gci = new HashMap<String, Integer>();
	int i = 0;

	for (State s : ss) {
	    mappings.add(new HashMap<String, Integer>());
	    for (ObjectInstance oi : s.getAllObjects()) {
		String className = oi.getTrueClassName();
		if (!mappings.get(i).containsKey(className)) {
		    mappings.get(i).put(className, 1);
		    List<Attribute> atts = oi.getObjectClass().attributeList;
		    if (!attributes.containsKey(className)) {
			// Attributes of this class haven't been set yet
			attributes.put(className, atts);
		    } else {
			if (!attributes.get(className).equals(atts)) {
			    throw new IllegalArgumentException(
				    "Attributes belonging to the class "
					    + className + " don't match.");
			}
		    }
		} else {
		    mappings.get(i).put(className,
			    mappings.get(i).get(className) + 1);
		}
	    }

	    i++;
	}

	List<String> toRemove = new ArrayList<String>();
	// Initialize the GCI
	for (Entry<String, Integer> e : mappings.get(0).entrySet()) {
	    gci.put(e.getKey(), e.getValue());
	    toRemove.add(e.getKey());
	}
	mappings.remove(0);

	// Fill up the GCI with the greatest intersection between all states
	for (Map<String, Integer> mapping : mappings) {
	    for (Entry<String, Integer> e : mapping.entrySet()) {
		if (gci.containsKey(e.getKey())) {
		    if (gci.get(e.getKey()) > e.getValue()) {
			gci.put(e.getKey(), e.getValue());
		    }
		    toRemove.remove(e.getKey());
		}
	    }
	}
	for (String remove : toRemove) {
	    gci.remove(remove);
	}

	return gci;
    }

    /**
     * Finds all combinations of object names in the provided state
     * 
     * @param s
     * @param gci
     * @return a list of all possible combinations of object names
     */
    public static List<List<String>> generateAllCombinations(State s,
	    Map<String, Integer> gci) {
	Map<String, List<List<String>>> combs = new HashMap<String, List<List<String>>>();

	for (Entry<String, Integer> e : gci.entrySet()) {
	    if (s.getFirstObjectOfClass(e.getKey()) == null) {
		throw new IllegalArgumentException(
			"State provided does not match the GCI.");
	    }
	    List<String> objNames = new ArrayList<String>();
	    for (ObjectInstance oi : s.getObjectsOfTrueClass(e.getKey())) {
		objNames.add(oi.getName());
	    }
	    // Only want the kth subset
	    List<List<String>> subsets = getSubsets(objNames, e.getValue(),
		    e.getValue());
	    combs.put(e.getKey(), subsets);
	}

	List<List<String>> names = new ArrayList<List<String>>();
	boolean firstPass = true;
	for (List<List<String>> objClass : combs.values()) {
	    if (firstPass) {
		names.addAll(objClass);
		firstPass = false;
		continue;
	    }
	    int originalSize = names.size();
	    int c = 0;
	    int f = objClass.size();
	    names = multiplyList(names, f);
	    for (List<String> objComb : objClass) {
		for (int i = c * originalSize; i < (c + 1) * originalSize; i++) {
		    List<String> temp = names.get(i);
		    temp.addAll(objComb);
		    names.set(i, temp);
		}
		c++;
	    }
	}

	return names;
    }

    /**
     * Gets the best candidate of those provided
     * 
     * @param hf
     * @param ip
     * @param policyCandidates
     * @return the highest scoring candidate
     */
    public static Map<StateHashTuple, GroundedAction> getBestCandidate(
	    StateHashFactory hf, PolicyBlocksPolicy ip,
	    List<Map<StateHashTuple, GroundedAction>> policyCandidates) {
	Map<StateHashTuple, GroundedAction> absPolicy = new HashMap<StateHashTuple, GroundedAction>();
	double bestScore = 0.;

	for (Map<StateHashTuple, GroundedAction> policyCandidate : policyCandidates) {
	    double curScore = scoreAbstraction(hf, ip, policyCandidate);
	    if (curScore > bestScore) {
		absPolicy = policyCandidate;
		bestScore = curScore;
	    }
	}

	return absPolicy;
    }

    /**
     * Scores the abstraction relative to its source policies
     * 
     * @param hf
     * @param ip
     * @param np
     * @return 1 - (occurrence in the source policy (normalized by redundancy) /
     *         size of new policy)
     */
    private static double scoreAbstraction(StateHashFactory hf,
	    PolicyBlocksPolicy ip, Map<StateHashTuple, GroundedAction> np) {
	double accuracy = 0;
	State withRespectTo = np.keySet().iterator().next().s;
	Map<StateHashTuple, List<Boolean>> correct = new HashMap<StateHashTuple, List<Boolean>>();

	for (Entry<StateHashTuple, GroundedAction> e : ip.policy.entrySet()) {
	    List<String> onames = new ArrayList<String>();
	    for (ObjectInstance oi : withRespectTo.getAllObjects()) {
		onames.add(oi.getName());
	    }
	    State newS = formState(e.getKey().s, onames);

	    if (e.getValue().equals(np.get(hf.hashState(newS)))) {
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
	    double c = 0;
	    for (Boolean b : bs) {
		if (b) {
		    c++;
		}
	    }

	    accuracy += (c / bs.size());
	}

	return 1 - (accuracy / np.size());
    }

    /**
     * Creates a state that only contains the objects in object names
     * 
     * @param s
     * @param onames
     * @return a new reduced state
     */
    public static State formState(State s, List<String> onames) {
	State newS = new State();
	for (String oname : onames) {
	    newS.addObject(s.getObject(oname));
	}

	return newS;
    }

    /**
     * Generates all policy candidates from the list of combinations
     * 
     * @param hf
     * @param ip
     * @param combinations
     * @return all policy candidates
     */
    private static List<Map<StateHashTuple, GroundedAction>> generatePolicyCandidates(
	    StateHashFactory hf, PolicyBlocksPolicy ip,
	    List<List<String>> combinations) {
	List<Map<StateHashTuple, GroundedAction>> policyCandidates = new ArrayList<Map<StateHashTuple, GroundedAction>>();

	for (List<String> combination : combinations) {
	    Map<StateHashTuple, List<StateHashTuple>> stateOccurence = new HashMap<StateHashTuple, List<StateHashTuple>>();
	    Map<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();
	    for (Entry<StateHashTuple, GroundedAction> e : ip.policy.entrySet()) {
		State curState = formState(e.getKey().s, combination);
		List<StateHashTuple> temp;
		if (!stateOccurence.containsKey(hf.hashState(curState))) {
		    temp = new ArrayList<StateHashTuple>();
		    temp.add(e.getKey());
		    stateOccurence.put(hf.hashState(curState), temp);
		} else {
		    temp = stateOccurence.get(hf.hashState(curState));
		    temp.add(e.getKey());
		    stateOccurence.put(hf.hashState(curState), temp);
		}
	    }
	    for (Entry<StateHashTuple, List<StateHashTuple>> e : stateOccurence
		    .entrySet()) {
		newPolicy.put(e.getKey(),
			getAction(ip, e.getKey().s, e.getValue()));
	    }

	    policyCandidates.add(newPolicy);
	}

	return policyCandidates;
    }

    /**
     * Gets the proper action for an abstracted state
     * 
     * @param ip
     * @param st
     * @param origStates
     * @return correct action weighted by q-values
     */
    private static GroundedAction getAction(PolicyBlocksPolicy ip, State st,
	    List<StateHashTuple> origStates) {
	List<List<QValue>> qs = new ArrayList<List<QValue>>();

	for (StateHashTuple origState : origStates) {
	    QLearning Q = (QLearning) ip.qplanner;
	    QLearningStateNode qNode = Q.getStateNode(origState);
	    qs.add(qNode.qEntry);
	}

	List<QValue> av = findAverages(st, qs);

	return getActionFromQs(av);
    }

    /**
     * Gets the best action from the list. If more than one action have the same
     * q-value, there's a dice roll.
     * 
     * @param av
     * @return the action with the highest q-value
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
     * Reduce the lists of q-values by averaging each of the same action
     * 
     * @param s
     * @param toAverage
     * @return the averaged q-values.
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

    /**
     * Abstraction happens on a per-group basis of policies to merge.
     * 
     * @param policies
     *            - the set of policies to be merged
     * @param depth
     *            - the upper bound of k
     * @return performs the merge operation on all k-subsets of the list of
     *         policies
     */
    public static List<Entry<AbstractedPolicy, Double>> unionMerge(
	    StateHashFactory hf, List<PolicyBlocksPolicy> policies, int depth) {
	List<Entry<AbstractedPolicy, Double>> mergedPolicies = new ArrayList<Entry<AbstractedPolicy, Double>>();
	for (List<PolicyBlocksPolicy> ps : getSubsets(policies, 2, depth)) {
	    AbstractedPolicy abs = merge(abstractAll(hf, ps));
	    if (abs.size() == 0) {
		continue;
	    }
	    double score = scoreMerge(hf, abs);
	    mergedPolicies
		    .add(new AbstractMap.SimpleEntry<AbstractedPolicy, Double>(
			    abs, score));
	}
	Collections.sort(mergedPolicies,
		new Comparator<Entry<AbstractedPolicy, Double>>() {
		    @Override
		    public int compare(Entry<AbstractedPolicy, Double> arg0,
			    Entry<AbstractedPolicy, Double> arg1) {
			return -arg0.getValue().compareTo(arg1.getValue());
		    }

		});

	return mergedPolicies;
    }

    /**
     * Merges the provided policies
     * 
     * @param abstractedPolicies
     *            - this list is modified as part of the recursion (becomes 1
     *            element after all recursion is done).
     * @return
     */
    public static AbstractedPolicy merge(
	    List<AbstractedPolicy> abstractedPolicies) {
	if (abstractedPolicies == null || abstractedPolicies.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Cannot pass a null or empty list of abstracted policies to merge.");
	}

	List<AbstractedPolicy> newPolicies = new ArrayList<AbstractedPolicy>();
	newPolicies.addAll(abstractedPolicies);
	AbstractedPolicy merged = new AbstractedPolicy(newPolicies.get(0));

	for (int i = 1; i < newPolicies.size(); i++) {
	    merged = merged.mergeWith(newPolicies.get(i));
	}

	return merged;
    }

    /**
     * Scores by comparing to source policies
     * 
     * @param hf
     * @param abs
     * @return score
     */
    public static double scoreMerge(StateHashFactory hf, AbstractedPolicy abs) {
	double totalMatch = 0.;

	for (PolicyBlocksPolicy orig : abs.getOriginalPolicies()) {
	    List<PolicyBlocksPolicy> temp = new ArrayList<PolicyBlocksPolicy>();
	    PolicyBlocksPolicy tempP = new PolicyBlocksPolicy(orig.getEpsilon());
	    tempP.policy = abs.abstractedPolicy;
	    temp.add(tempP);

	    // Will throw a null pointer exception if the policy is of size
	    // 0
	    AbstractedPolicy origAb = new AbstractedPolicy(hf, orig, temp);
	    double stateSize = origAb.abstractedPolicy.size();
	    double stateMatch = 0.;

	    for (Entry<StateHashTuple, GroundedAction> e : abs.abstractedPolicy
		    .entrySet()) {
		if (origAb.abstractedPolicy.containsKey(e.getKey())
			&& origAb.abstractedPolicy.get(e.getKey()).equals(
				e.getValue())) {
		    // Check for state match and then action match
		    stateMatch += 1;
		}
	    }
	    totalMatch += stateMatch / stateSize;
	}

	return totalMatch;
    }

    /**
     * Creates a list initialized to its indices
     * 
     * @param size
     * @return
     */
    public static int[] range(int size) {
	int[] range = new int[size];
	for (int i = 0; i < size; i++) {
	    range[i] = i;
	}

	return range;
    }

    /**
     * Creates a list of size * factory through item repetition
     * 
     * @param l
     * @param factor
     * @return the multiplied list
     */
    public static <T> List<T> multiplyList(List<T> l, int factor) {
	List<T> ret = new ArrayList<T>(l.size() * factor);

	for (int i = 0; i < factor; i++) {
	    ret.addAll(l);
	}

	return ret;
    }

    /**
     * Generates all k-subsets from alpha to beta. Runs O(Sum from k=alpha to
     * beta of n! / (k! * (n - k)!))
     * 
     * @param set
     *            - the set to be permuted
     * @param alpha
     *            - lower bound of k
     * @param beta
     *            - upper bound of k
     * @return a list of all k-subsets from alpha to beta
     */
    public static <T> List<List<T>> getSubsets(List<T> set, int alpha, int beta) {
	if (alpha < 1 || alpha > beta || alpha > set.size()) {
	    throw new IllegalArgumentException("Invalid minimum depth size: "
		    + alpha + ".");
	} else if (beta > set.size() || beta < 1) {
	    throw new IllegalArgumentException("Invalid maximum depth size: "
		    + beta + ".");
	}

	List<List<T>> ret = new ArrayList<List<T>>();

	for (int i = alpha; i <= beta; i++) {
	    int[][] combs = nexksb(range(set.size()), i);
	    for (int[] cs : combs) {
		List<T> temp = new ArrayList<T>();
		for (int c : cs) {
		    temp.add(set.get(c));
		}
		ret.add(temp);
	    }
	}

	return ret;
    }

    /**
     * Implements the NEXKSB algorithm. Gets all k-subsets of the set provided.
     * 
     * @param k
     *            - size of subset tuples
     * @param set
     *            - the set to be generated from
     * @return all subsets of size k
     */
    public static int[][] nexksb(int[] set, int k) {
	int c = (int) binomial(set.length, k);
	int[][] res = new int[c][Math.max(0, k)];
	int[] ind = k < 0 ? null : new int[k];
	// initialize red squares
	for (int i = 0; i < k; ++i) {
	    ind[i] = i;
	}
	// for every combination
	for (int i = 0; i < c; ++i) {
	    // get its elements (red square indexes)
	    for (int j = 0; j < k; ++j) {
		res[i][j] = set[ind[j]];
	    }
	    // update red squares, starting by the last
	    int x = ind.length - 1;
	    boolean loop;
	    do {
		loop = false;
		// move to next
		ind[x] = ind[x] + 1;
		// if crossing boundaries, move previous
		if (ind[x] > set.length - (k - x)) {
		    --x;
		    loop = x >= 0;
		} else {
		    // update every following square
		    for (int x1 = x + 1; x1 < ind.length; ++x1) {
			ind[x1] = ind[x1 - 1] + 1;
		    }
		}
	    } while (loop);
	}

	return res;
    }

    /**
     * Performs the binomial coefficient function C(n, k)
     * 
     * @param n
     * @param k
     * @return n! / (k! * (n - k)!)
     */
    public static long binomial(int n, int k) {
	if (k < 0 || k > n) {
	    return 0;
	}
	if (k > n - k) {
	    k = n - k;
	}

	long c = 1;
	for (int i = 1; i < k + 1; ++i) {
	    c = c * (n - (k - i));
	    c = c / i;
	}

	return c;
    }

    public AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy) {
	if (!isSameAbstraction(otherPolicy)) {
	    throw new IllegalArgumentException(
		    "Not the same level of abstraction.");
	}

	AbstractedPolicy merged = new AbstractedPolicy();
	merged.originalPolicies.addAll(this.originalPolicies);

	for (Entry<StateHashTuple, GroundedAction> e : this.abstractedPolicy
		.entrySet()) {
	    // Comparison is simply whether the given state corresponds to the
	    // same action
	    GroundedAction a = otherPolicy.abstractedPolicy.get(e.getKey());
	    if (e.getValue().equals(a)) {
		merged.abstractedPolicy.put(e.getKey(), e.getValue());
	    }
	}

	return merged;
    }

    /**
     * Checks if the original policies are equal
     * 
     * @param other
     * @return whether or not the source policies are the same
     */
    public boolean isSameAbstraction(AbstractedPolicy other) {
	return this.getOriginalPolicies().equals(other.getOriginalPolicies());
    }

    /**
     * Gets the abstracted policy
     * 
     * @return abstractedPolicy
     */
    public Map<StateHashTuple, GroundedAction> getPolicy() {
	return abstractedPolicy;
    }

    /**
     * Gets the set of original policies to avoid changing them by mistake
     * 
     * @return set of original policies
     */
    public Set<PolicyBlocksPolicy> getOriginalPolicies() {
	return originalPolicies;
    }

    /**
     * Gets the hash factory
     * 
     * @return hashFactory
     */
    public StateHashFactory getHashingFactory() {
	return hashFactory;
    }

    /**
     * Gets the size of the state space for which the policy is defined
     * 
     * @return the size of the policy
     */
    public int size() {
	return abstractedPolicy.size();
    }

    /**
     * Gets the string representation of this policy
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();

	for (Entry<StateHashTuple, GroundedAction> e : abstractedPolicy
		.entrySet()) {
	    builder.append(e.getKey().hashCode() + ": " + e.getValue() + "\n");
	}

	return builder.toString();
    }
}