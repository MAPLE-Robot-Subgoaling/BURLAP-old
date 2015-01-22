package burlap.behavior.policyblocks;

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

public class AbstractedPolicy extends Policy {
    private Map<StateHashTuple, GroundedAction> abstractedPolicy;
    private Set<PolicyBlocksPolicy> originalPolicies;
    private Map<String, Integer> gcg;
    private StateHashFactory hashFactory;

    private AbstractedPolicy() {
	this.abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
	this.originalPolicies = new HashSet<PolicyBlocksPolicy>();
	this.gcg = new HashMap<String, Integer>();
    }

    public AbstractedPolicy(AbstractedPolicy p) {
	this();
	this.abstractedPolicy.putAll(p.abstractedPolicy);
	this.originalPolicies.addAll(p.originalPolicies);
	this.hashFactory = p.hashFactory;
	this.gcg = p.gcg;
    }

    private AbstractedPolicy(StateHashFactory hf, AbstractedPolicy ip,
	    Set<PolicyBlocksPolicy> sourcePolicies, Map<String, Integer> gcg) {
	this();
	this.hashFactory = hf;
	this.originalPolicies = sourcePolicies;

	List<List<String>> oiCombs = generateAllCombinations(sampleState(ip),
		gcg);
	List<Entry<Map<StateHashTuple, GroundedAction>, Double>> policyCandidates = generatePolicyCandidates(
		hf, ip.getPolicy(), oiCombs, 1);

	if (policyCandidates.size() > 0) {
	    this.abstractedPolicy = policyCandidates.get(0).getKey();
	}
    }

    public AbstractedPolicy(StateHashFactory hf, PolicyBlocksPolicy ip,
	    PolicyBlocksPolicy p) {
	this(hf, ip, singletonList(p));
    }

    public AbstractedPolicy(StateHashFactory hf, PolicyBlocksPolicy ip,
	    List<PolicyBlocksPolicy> ps) {
	this();
	this.hashFactory = hf;
	// Generate the GCG of the state
	// Generate every combination of GCG mappings for the original policy
	// Score each generated combination of GCG mappings
	State ipS = sampleState(ip);
	List<State> psS = new ArrayList<State>();
	for (PolicyBlocksPolicy p : ps) {
	    psS.add(sampleState(p));
	}
	psS.add(ipS);

	// If there exists no GCG between the states provided, the final
	// abstracted policy will be empty (this doesn't cause a problem in
	// merging)
	Map<String, Integer> gcg = greatestCommonGeneralization(psS);
	this.gcg = gcg;

	List<List<String>> oiCombs = generateAllCombinations(ipS, gcg);

	List<Entry<Map<StateHashTuple, GroundedAction>, Double>> policyCandidates = generatePolicyCandidates(
		hf, ip, oiCombs, 1);

	if (policyCandidates.size() > 0) {
	    this.abstractedPolicy = policyCandidates.get(0).getKey();
	}

	ps.add(ip);
	this.originalPolicies.addAll(ps);
    }

    /**
     * Abstracts each policy with respect to the others
     * 
     * @param hf
     *            - state hash factory
     * @param policies
     *            - the policies to abstract
     * @return a list of all abstracted policies
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
	    AbstractedPolicy absPolicy = new AbstractedPolicy(hf, temp,
		    newPolicies);
	    abstractedPolicies.add(absPolicy);
	}

	return abstractedPolicies;
    }

    /**
     * Generates the GCG of a list of states
     * 
     * @param ss
     *            - list of states to sample
     * @return the GCG of object classes
     */
    public static Map<String, Integer> greatestCommonGeneralization(
	    List<State> ss) {
	List<Map<String, Integer>> mappings = new ArrayList<Map<String, Integer>>(
		ss.size());
	Map<String, List<Attribute>> attributes = new HashMap<String, List<Attribute>>();
	Map<String, Integer> gcg = new HashMap<String, Integer>();
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

	Map<String, Integer> classCount = new HashMap<String, Integer>();
	// Initialize the GCG
	for (Entry<String, Integer> e : mappings.get(0).entrySet()) {
	    gcg.put(e.getKey(), e.getValue());
	    classCount.put(e.getKey(), 1);
	}
	mappings.remove(0);

	// Fill up the GCG with the greatest intersection between all states
	for (Map<String, Integer> mapping : mappings) {
	    for (Entry<String, Integer> e : mapping.entrySet()) {
		if (gcg.containsKey(e.getKey())) {
		    if (gcg.get(e.getKey()) > e.getValue()) {
			gcg.put(e.getKey(), e.getValue());
		    }

		    if (!classCount.containsKey(e.getKey())) {
			classCount.put(e.getKey(), 1);
		    } else {
			classCount.put(e.getKey(),
				classCount.get(e.getKey()) + 1);
		    }
		}
	    }
	}
	for (Entry<String, Integer> e : classCount.entrySet()) {
	    if (gcg.containsKey(e.getKey()) && e.getValue() < ss.size()) {
		// If the object class does not exist for all states
		gcg.remove(e.getKey());
	    }
	}

	return gcg;
    }

    /**
     * Generates all combinations of objects with respect to the GCG
     * 
     * @param s
     *            - state to sample from
     * @param gcg
     *            - the greatest common generalization
     * @return the list of all possible object combinations
     */
    public static List<List<String>> generateAllCombinations(State s,
	    Map<String, Integer> gcg) {
	Map<String, List<List<String>>> combs = new HashMap<String, List<List<String>>>();

	for (Entry<String, Integer> e : gcg.entrySet()) {
	    if (s.getFirstObjectOfClass(e.getKey()) == null) {
		throw new IllegalArgumentException(
			"State provided does not match the GCG.");
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
     * Scores the abstraction with respect to the initial policy
     * 
     * @param hf
     *            - the state hashing factory
     * @param ip
     *            - the initial policy
     * @param np
     *            - the new policy
     * @return a floating point score of similarity to the initial policy
     */
    private static double scoreAbstraction(StateHashFactory hf,
	    Map<StateHashTuple, GroundedAction> ip,
	    Map<StateHashTuple, GroundedAction> np) {
	double accuracy = 0.;
	State withRespectTo = sampleState(np);
	Map<StateHashTuple, List<Boolean>> correct = new HashMap<StateHashTuple, List<Boolean>>();
	List<String> onames = new ArrayList<String>();

	for (ObjectInstance oi : withRespectTo.getAllObjects()) {
	    onames.add(oi.getName());
	}

	for (Entry<StateHashTuple, GroundedAction> e : ip.entrySet()) {
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

    /**
     * Generates maxCand policy candidates, ranked by score
     * 
     * @param hf
     *            - the state hash factory
     * @param ip
     *            - the initial policy
     * @param combinations
     *            - all possible object combinations
     * @param maxCand
     *            - maximum number of policies to generate
     * @return the list of maxCand generated policies
     */
    private static List<Entry<Map<StateHashTuple, GroundedAction>, Double>> generatePolicyCandidates(
	    StateHashFactory hf, PolicyBlocksPolicy ip,
	    List<List<String>> combinations, int maxCand) {
	List<Entry<Map<StateHashTuple, GroundedAction>, Double>> policyCandidates = new ArrayList<Entry<Map<StateHashTuple, GroundedAction>, Double>>();

	for (List<String> combination : combinations) {
	    Map<StateHashTuple, List<StateHashTuple>> stateOccurence = new HashMap<StateHashTuple, List<StateHashTuple>>();
	    Map<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();
	    for (Entry<StateHashTuple, GroundedAction> e : ip.getPolicy()
		    .entrySet()) {
		State curState = formState(e.getKey().s, combination);
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

	    double score = scoreAbstraction(hf, ip.getPolicy(), newPolicy);
	    boolean toSort = false;

	    if (policyCandidates.size() < maxCand) {
		policyCandidates
			.add(new AbstractMap.SimpleEntry<Map<StateHashTuple, GroundedAction>, Double>(
				newPolicy, score));

		toSort = true;
	    } else {
		if (policyCandidates.get(policyCandidates.size() - 1)
			.getValue() < score) {
		    policyCandidates
			    .set(policyCandidates.size() - 1,
				    new AbstractMap.SimpleEntry<Map<StateHashTuple, GroundedAction>, Double>(
					    newPolicy, score));

		    toSort = true;
		}
	    }

	    if (toSort) {
		Collections
			.sort(policyCandidates,
				new Comparator<Entry<Map<StateHashTuple, GroundedAction>, Double>>() {
				    @Override
				    public int compare(
					    Entry<Map<StateHashTuple, GroundedAction>, Double> arg0,
					    Entry<Map<StateHashTuple, GroundedAction>, Double> arg1) {
					return -arg0.getValue().compareTo(
						arg1.getValue());
				    }
				});
	    }
	}

	return policyCandidates;
    }

    /**
     * Generates maxCand policy candidates, using a Q-value free tabular policy
     * 
     * @param hf
     *            - state hash factory
     * @param ip
     *            - initial policy
     * @param combinations
     *            - all object combinations
     * @param maxCand
     *            - max number of policies to generate
     * @return maxCand policies, ranked by score
     */
    private static List<Entry<Map<StateHashTuple, GroundedAction>, Double>> generatePolicyCandidates(
	    StateHashFactory hf, Map<StateHashTuple, GroundedAction> ip,
	    List<List<String>> combinations, int maxCand) {
	List<Entry<Map<StateHashTuple, GroundedAction>, Double>> policyCandidates = new ArrayList<Entry<Map<StateHashTuple, GroundedAction>, Double>>();

	for (List<String> combination : combinations) {
	    Map<StateHashTuple, List<GroundedAction>> stateOccurence = new HashMap<StateHashTuple, List<GroundedAction>>();
	    Map<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();

	    for (Entry<StateHashTuple, GroundedAction> e : ip.entrySet()) {
		State curState = formState(e.getKey().s, combination);
		List<GroundedAction> temp;

		if (!stateOccurence.containsKey(hf.hashState(curState))) {
		    temp = new ArrayList<GroundedAction>();
		    temp.add(e.getValue());
		    stateOccurence.put(hf.hashState(curState), temp);
		} else {
		    stateOccurence.get(hf.hashState(curState))
			    .add(e.getValue());
		}
	    }

	    for (Entry<StateHashTuple, List<GroundedAction>> e : stateOccurence
		    .entrySet()) {
		newPolicy.put(e.getKey(), getAction(e.getValue()));
	    }

	    if (newPolicy.size() == 0) {
		continue;
	    }

	    double score = scoreAbstraction(hf, ip, newPolicy);
	    boolean toSort = false;

	    if (policyCandidates.size() < maxCand) {
		policyCandidates
			.add(new AbstractMap.SimpleEntry<Map<StateHashTuple, GroundedAction>, Double>(
				newPolicy, score));

		toSort = true;
	    } else {
		if (policyCandidates.get(policyCandidates.size() - 1)
			.getValue() < score) {
		    policyCandidates
			    .set(policyCandidates.size() - 1,
				    new AbstractMap.SimpleEntry<Map<StateHashTuple, GroundedAction>, Double>(
					    newPolicy, score));

		    toSort = true;
		}
	    }

	    if (toSort) {
		Collections
			.sort(policyCandidates,
				new Comparator<Entry<Map<StateHashTuple, GroundedAction>, Double>>() {
				    @Override
				    public int compare(
					    Entry<Map<StateHashTuple, GroundedAction>, Double> arg0,
					    Entry<Map<StateHashTuple, GroundedAction>, Double> arg1) {
					return arg1.getValue().compareTo(
						arg0.getValue());
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

    /**
     * Abstracts a policy with respect to itself
     * 
     * @param hf
     *            - state hash factory
     * @param p
     *            - policy to abstract
     * @param origPs
     *            - policies to set as source policies (not sampled from)
     * @return the new abstracted policy
     */
    public static AbstractedPolicy naiveAbstraction(StateHashFactory hf,
	    PolicyBlocksPolicy p, List<PolicyBlocksPolicy> origPs) {
	AbstractedPolicy abs = new AbstractedPolicy();
	abs.hashFactory = hf;
	abs.originalPolicies.add(p);
	abs.originalPolicies.addAll(origPs);
	abs.abstractedPolicy = p.getPolicy();
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
    public static List<AbstractedPolicy> naiveAbstractAll(StateHashFactory hf,
	    List<PolicyBlocksPolicy> policies) {
	List<AbstractedPolicy> abstractedPolicies = new ArrayList<AbstractedPolicy>();

	for (int i = 0; i < policies.size(); i++) {
	    List<PolicyBlocksPolicy> newPolicies = new ArrayList<PolicyBlocksPolicy>();
	    newPolicies.addAll(policies);

	    PolicyBlocksPolicy temp = newPolicies.remove(i);
	    AbstractedPolicy absPolicy = naiveAbstraction(hf, temp, newPolicies);
	    abstractedPolicies.add(absPolicy);
	}

	return abstractedPolicies;
    }

    /**
     * Performs the power merge as described by PolicyBlocks
     * 
     * @param hf
     *            - state hash factory
     * @param policies
     *            - policies to merge
     * @param depth
     *            - depth of the merge
     * @param maxPol
     *            - max number of candidates
     * @return the list of abstracted policies, along with scores
     */
    public static List<Entry<AbstractedPolicy, Double>> naivePowerMerge(
	    StateHashFactory hf, List<PolicyBlocksPolicy> policies, int depth,
	    int maxPol) {
	List<Entry<AbstractedPolicy, Double>> mergedPolicies = new ArrayList<Entry<AbstractedPolicy, Double>>();

	for (List<PolicyBlocksPolicy> ps : getSubsets(policies, 2, depth)) {
	    boolean toSort = false;

	    AbstractedPolicy abs = merge(naiveAbstractAll(hf, ps), true);
	    if (abs.size() == 0) {
		continue;
	    }

	    double score = naiveScoreMerge(hf, abs, policies);

	    if (mergedPolicies.size() < maxPol) {
		mergedPolicies
			.add(new AbstractMap.SimpleEntry<AbstractedPolicy, Double>(
				abs, score));

		toSort = true;
	    } else if (score > mergedPolicies.get(mergedPolicies.size() - 1)
		    .getValue()) {
		mergedPolicies.set(mergedPolicies.size() - 1,
			new AbstractMap.SimpleEntry<AbstractedPolicy, Double>(
				abs, score));

		toSort = true;
	    }

	    if (toSort) {
		Collections.sort(mergedPolicies,
			new Comparator<Entry<AbstractedPolicy, Double>>() {
			    @Override
			    public int compare(
				    Entry<AbstractedPolicy, Double> arg0,
				    Entry<AbstractedPolicy, Double> arg1) {
				return -arg0.getValue().compareTo(
					arg1.getValue());
			    }

			});
	    }
	}

	subtractAll(mergedPolicies);

	return mergedPolicies;
    }

    /**
     * Scores the policy with respect to sources as defined by PolicyBlocks
     * 
     * @param hf
     *            - state hash factory
     * @param p
     *            - newly abstracted policy
     * @param policies
     *            - source policies
     * @return floating point score of fitness
     */
    public static double naiveScoreMerge(StateHashFactory hf,
	    AbstractedPolicy p, List<PolicyBlocksPolicy> policies) {
	int freq = 0;

	for (Entry<StateHashTuple, GroundedAction> e : p.abstractedPolicy
		.entrySet()) {
	    for (PolicyBlocksPolicy pbp : policies) {
		if (e.getValue().equals(pbp.getPolicy().get(e.getKey()))) {
		    freq++;
		}
	    }
	}

	return p.size() * freq;
    }

    /**
     * Performs the power merge as defined by P-MODAL
     * 
     * @param hf
     *            - state hash factory
     * @param policies
     *            - source policies
     * @param depth
     *            - depth of the merge
     * @param maxPol
     *            - max number of option candidates to create
     * @return list of option candidates, along with score
     */
    public static List<Entry<AbstractedPolicy, Double>> powerMerge(
	    StateHashFactory hf, List<PolicyBlocksPolicy> policies, int depth,
	    int maxPol) {
	return powerMerge(hf, policies, depth, maxPol, true, true);
    }

    /**
     * Performs the power merge as defined by P-MODAL
     * 
     * @param hf
     *            - state hash factory
     * @param policies
     *            - source policies
     * @param depth
     *            - depth of the merge
     * @param maxPol
     *            - max number of or option candidates to create
     * @param toSubtract
     *            - whether or not to subtract redundant states in all options
     * @param greedyMerge
     *            - whether or not to perform the greedy step-wise merging
     * @return list of learned policies + score
     */
    public static List<Entry<AbstractedPolicy, Double>> powerMerge(
	    StateHashFactory hf, List<PolicyBlocksPolicy> policies, int depth,
	    int maxPol, boolean toSubtract, boolean greedyMerge) {
	return powerMergeCache(hf, policies, depth, maxPol, toSubtract,
		greedyMerge, new int[] { depth }).get(depth);
    }

    /**
     * Performs the power merge as defined by P-MODAL
     * 
     * @param hf
     *            - state hash factory
     * @param policies
     *            - source policies
     * @param depth
     *            - depth of the merge
     * @param maxPol
     *            - max number of or option candidates to create
     * @param toSubtract
     *            - whether or not to subtract redundant states in all options
     * @param greedyMerge
     *            - whether or not to perform the greedy step-wise merging
     * @param toCache
     *            - whether or not data from all depths in cached in the map to
     *            be returned
     * @return Map of depth -> list of learned policies + score
     */
    public static Map<Integer, List<Entry<AbstractedPolicy, Double>>> powerMergeCache(
	    StateHashFactory hf, List<PolicyBlocksPolicy> policies, int depth,
	    int maxPol, boolean toSubtract, boolean greedyMerge, int[] toCache) {
	Map<Integer, List<Entry<AbstractedPolicy, Double>>> mappedPolicies = new HashMap<Integer, List<Entry<AbstractedPolicy, Double>>>(
		toCache.length);
	List<Entry<AbstractedPolicy, Double>> mergedPolicies = new ArrayList<Entry<AbstractedPolicy, Double>>();
	int c = 2;

	for (List<PolicyBlocksPolicy> ps : getSubsets(policies, 2, depth)) {
	    if (!Arrays.asList(toCache).contains(c)) {
		c++;
		continue;
	    }

	    boolean toSort = false;
	    AbstractedPolicy abs;

	    if (greedyMerge) {
		abs = greedyMerge(hf, ps);
	    } else {
		abs = merge(abstractAll(hf, ps));
	    }

	    if (abs.size() == 0) {
		continue;
	    }

	    double score = scoreMerge(hf, abs);

	    if (mergedPolicies.size() < maxPol) {
		mergedPolicies
			.add(new AbstractMap.SimpleEntry<AbstractedPolicy, Double>(
				abs, score));

		toSort = true;
	    } else if (score > mergedPolicies.get(mergedPolicies.size() - 1)
		    .getValue()) {
		mergedPolicies.set(mergedPolicies.size() - 1,
			new AbstractMap.SimpleEntry<AbstractedPolicy, Double>(
				abs, score));

		toSort = true;
	    }

	    if (toSort) {
		Collections.sort(mergedPolicies,
			new Comparator<Entry<AbstractedPolicy, Double>>() {
			    @Override
			    public int compare(
				    Entry<AbstractedPolicy, Double> arg0,
				    Entry<AbstractedPolicy, Double> arg1) {
				return -arg0.getValue().compareTo(
					arg1.getValue());
			    }

			});
	    }

	    subtractAll(mergedPolicies);
	    mappedPolicies.put(c, mergedPolicies);
	    c++;
	}

	return mappedPolicies;
    }

    /**
     * Performs the subtract operation, as defined by PolicyBlocks. Removes all
     * states in the top scoring policy from the policies with lower scores and
     * repeats
     * 
     * @param mergedPolicies
     */
    public static void subtractAll(
	    List<Entry<AbstractedPolicy, Double>> mergedPolicies) {
	// Subtract operation
	for (int i = 0; i < mergedPolicies.size(); i++) {
	    Map<StateHashTuple, GroundedAction> toSubtract = mergedPolicies
		    .get(i).getKey().getPolicy();

	    for (StateHashTuple sh : toSubtract.keySet()) {
		for (int j = i + 1; j < mergedPolicies.size(); j++) {
		    mergedPolicies.get(j).getKey().getPolicy().remove(sh);

		    if (mergedPolicies.get(j).getKey().size() == 0) {
			mergedPolicies.remove(j);
		    }
		}
	    }
	}
    }

    /**
     * Performs greedy step-wise merging on the provided source policies
     * 
     * @param hf
     *            - state hashing function
     * @param ops
     *            - source policies
     * @return the merged policy
     */
    public static AbstractedPolicy greedyMerge(StateHashFactory hf,
	    List<PolicyBlocksPolicy> ops) {
	List<PolicyBlocksPolicy> ps = ops;
	List<State> ss = new ArrayList<State>();
	for (int i = 0; i < ps.size(); i++) {
	    // Sample a state from each source policy
	    ss.add(sampleState(ps.get(i)));
	}
	Map<String, Integer> gcg = greatestCommonGeneralization(ss);
	List<String> tempO = new ArrayList<String>(gcg.keySet());
	// Randomly order the gcg for now
	Collections.shuffle(tempO);
	final List<String> ordering = tempO;
	// ps <- [(3O, 2X), (2O, 3X), (3O, 3X)]
	// gcg <- {'O': 2, 'X': 2}
	// ordering <- ['O', 'X']

	// Sort source policies according the the gcg's ordering
	Collections.sort(ps, new Comparator<PolicyBlocksPolicy>() {
	    @Override
	    public int compare(PolicyBlocksPolicy arg0, PolicyBlocksPolicy arg1) {
		State s0 = sampleState(arg0);
		State s1 = sampleState(arg1);

		for (int i = 0; i < ordering.size(); i++) {
		    Integer c0 = s0.getObjectsOfTrueClass(ordering.get(i))
			    .size();
		    Integer c1 = s1.getObjectsOfTrueClass(ordering.get(i))
			    .size();

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
	AbstractedPolicy finalAbs = naiveAbstractAll(hf,
		singletonList(ps.get(0))).get(0);
	Set<PolicyBlocksPolicy> originals = new HashSet<PolicyBlocksPolicy>(ops);
	finalAbs.originalPolicies = originals;
	for (int i = 1; i < ps.size(); i++) {
	    if (finalAbs.size() == 0) {
		return finalAbs;
	    }

	    // Through every policy
	    AbstractedPolicy curAbs = naiveAbstractAll(hf,
		    singletonList(ps.get(i))).get(0);
	    curAbs.originalPolicies = originals;

	    State s0 = sampleState(finalAbs);
	    State s1 = sampleState(curAbs);
	    Map<String, Integer> m0 = getObjectCounts(s0);
	    Map<String, Integer> m1 = getObjectCounts(s1);

	    while (true) {
		boolean flag = true;
		Set<String> objSet = new HashSet<String>();
		objSet.addAll(m0.keySet());
		objSet.addAll(m1.keySet());

		for (String obj : objSet) {
		    if (m0.get(obj) == null) {
			m0.put(obj, 0);
		    }
		    if (m1.get(obj) == null) {
			m1.put(obj, 0);
		    }

		    if (m0.get(obj) < 1 && m1.get(obj) < 1) {
		    } else if (m0.get(obj) > m1.get(obj)) {
			m0.put(obj, m0.get(obj) - 1);
			finalAbs = new AbstractedPolicy(hf, finalAbs,
				originals, m0);

			flag = false;
			break;
		    } else if (m1.get(obj) > m0.get(obj)) {
			m1.put(obj, m1.get(obj) - 1);
			curAbs = new AbstractedPolicy(hf, curAbs, originals, m1);

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

    /**
     * Merges the provided policies
     * 
     * @param abstractedPolicies
     *            - this list is modified as part of the recursion (becomes 1
     *            element after all recursion is done).
     * @return the final merged abstracted policy
     */
    public static AbstractedPolicy merge(
	    List<AbstractedPolicy> abstractedPolicies) {
	return merge(abstractedPolicies, false);
    }

    /**
     * Merges the provided policies
     * 
     * @param abstractedPolicies
     *            - this list is modified as part of the recursion (becomes 1
     *            element after all recursion is done).
     * @param isNaive
     *            - whether or not to consider the level of abstraction for
     *            validation; if this is true and abstractions are different,
     *            the merge will be empty
     * @return the final merged abstracted policy
     */
    public static AbstractedPolicy merge(
	    List<AbstractedPolicy> abstractedPolicies, boolean isNaive) {
	if (abstractedPolicies == null || abstractedPolicies.isEmpty()) {
	    throw new IllegalArgumentException(
		    "Cannot pass a null or empty list of abstracted policies to merge.");
	}

	List<AbstractedPolicy> newPolicies = new ArrayList<AbstractedPolicy>();
	newPolicies.addAll(abstractedPolicies);
	AbstractedPolicy merged = new AbstractedPolicy(newPolicies.get(0));

	for (int i = 1; i < newPolicies.size(); i++) {
	    merged = merged.mergeWith(newPolicies.get(i), isNaive);
	}

	return merged;
    }

    /**
     * Merges this with the provided policy
     * 
     * @param otherPolicy
     *            - policy to merge with
     * @return the merged policy
     */
    public AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy) {
	return mergeWith(otherPolicy, false);
    }

    /**
     * Merges this with the provided policy
     * 
     * @param otherPolicy
     *            - policy to merge with
     * @param isNaive
     *            - whether or not to consider the level of abstraction for
     *            validation; if this is true and abstractions are different,
     *            the merge will be empty
     * @return the merged policy
     */
    public AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy,
	    boolean isNaive) {
	if (!isNaive && !isSameAbstraction(otherPolicy)) {
	    throw new IllegalArgumentException(
		    "Not the same level of abstraction.");
	}

	AbstractedPolicy merged = new AbstractedPolicy();
	merged.hashFactory = this.hashFactory;
	merged.originalPolicies.addAll(this.originalPolicies);
	State withRespectTo = sampleState(otherPolicy).copy();

	for (Entry<StateHashTuple, GroundedAction> e : this.abstractedPolicy
		.entrySet()) {
	    GroundedAction ga;
	    if (this.hashFactory instanceof DiscreteStateHashFactory) {
		ga = otherPolicy.abstractedPolicy.get(e.getKey());
		if (e.getValue().equals(ga)) {
		    merged.abstractedPolicy.put(e.getKey(), e.getValue());
		}
	    } else {
		List<State> possibleStates = generatePossibleStates(
			e.getKey().s, withRespectTo);
		// This is done in the event that while two abstractions will
		// share
		// the GCG, they may have different object names (e.g. [p1, p2]
		// versus [p3, p2])
		// DiscreteStateHashFactory doesn't care about names, but this
		// is in
		// here just as a safety measure for other hashing methods.
		for (State possibleState : possibleStates) {
		    ga = otherPolicy.abstractedPolicy.get(this.hashFactory
			    .hashState(possibleState));
		    if (e.getValue().equals(ga)) {
			merged.abstractedPolicy.put(e.getKey(), e.getValue());
		    }
		}
	    }
	}

	return merged;
    }

    /**
     * Generates every possible arrangement of object classes in s1 with respect
     * to those in s2. Used in checking for equality in the merge function.
     * 
     * @param s1
     * @param s2
     * @return all possible arrangement of s1 with respect to s2
     */
    public static List<State> generatePossibleStates(State s1, State s2) {
	List<State> combs = new ArrayList<State>();
	boolean firstPass = true;

	for (String obClass : s1.getObjectClassesPresent()) {
	    List<ObjectInstance> oisOfClass = new ArrayList<ObjectInstance>();
	    oisOfClass.addAll(s2.getObjectsOfTrueClass(obClass));
	    List<List<ObjectInstance>> oisPerms = permutations(oisOfClass);
	    int origSize = combs.size();
	    combs = multiplyList(combs, oisPerms.size());

	    if (s1.getObjectsOfTrueClass(obClass).size() != s2
		    .getObjectsOfTrueClass(obClass).size()) {
		throw new IllegalArgumentException(
			"States are not on the same level of abstraction.");
	    }

	    int c = 0;
	    for (List<ObjectInstance> oisPerm : oisPerms) {
		if (firstPass) {
		    State newS = s1.copy();

		    int k = 0;
		    for (ObjectInstance oi : newS
			    .getObjectsOfTrueClass(obClass)) {
			oi.setName(oisPerm.get(k).getName());
			k++;
		    }

		    combs.add(newS);
		} else {
		    for (int i = 0; i < origSize; i++) {
			int index = (c * origSize) + i;
			State newS = combs.get(index).copy();

			int k = 0;
			for (ObjectInstance oi : newS
				.getObjectsOfTrueClass(obClass)) {
			    oi.setName(oisPerm.get(k).getName());
			    k++;
			}

			combs.set(index, newS);
		    }
		}

		c++;
	    }

	    if (firstPass) {
		firstPass = false;
	    }
	}

	return combs;
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
	    PolicyBlocksPolicy tempP = new PolicyBlocksPolicy(orig.getEpsilon());
	    tempP.policy = abs.abstractedPolicy;

	    // Abstracts the original policy with respect to the abstracted
	    // policy
	    AbstractedPolicy origAb = new AbstractedPolicy(hf, orig, tempP);
	    double stateSize = origAb.abstractedPolicy.size();
	    double stateMatch = 0.;

	    for (Entry<StateHashTuple, GroundedAction> e : abs.abstractedPolicy
		    .entrySet()) {
		if (e.getValue()
			.equals(origAb.abstractedPolicy.get(e.getKey()))) {
		    // Check for a state->action match
		    stateMatch += 1;
		}
	    }

	    totalMatch += stateMatch / stateSize;
	}

	return totalMatch;
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
	for (ObjectInstance oi : s.getAllObjects()) {
	    if (onames.contains(oi.getName())) {
		newS.addObject(oi);
	    }
	}

	return newS;
    }

    /**
     * Samples a state from the policy provided
     * 
     * @param p
     * @return a seemingly random state
     */
    public static State sampleState(PolicyBlocksPolicy p) {
	return sampleState(p.getPolicy());
    }

    /**
     * Samples a state from the policy provided
     * 
     * @param p
     * @return a seemingly random state
     */
    public static State sampleState(AbstractedPolicy p) {
	return sampleState(p.getPolicy());
    }

    /**
     * Samples a state from the policy provided
     * 
     * @param p
     * @return a seemingly random state
     */
    public static State sampleState(Map<StateHashTuple, GroundedAction> p) {
	return p.keySet().iterator().next().s;
    }

    /**
     * Gets the count of each object class in a state
     * 
     * @param s
     * @return mapping of object class -> count
     */
    public static Map<String, Integer> getObjectCounts(State s) {
	Map<String, Integer> cs = new HashMap<String, Integer>();
	List<List<ObjectInstance>> os = s.getAllObjectsByTrueClass();

	for (List<ObjectInstance> o : os) {
	    cs.put(o.get(0).getObjectClass().name, o.size());
	}

	return cs;
    }

    /**
     * Creates a list initialized to its indices
     * 
     * @param size
     * @return array of [0..size-1]
     */
    public static int[] range(int size) {
	int[] range = new int[size];
	for (int i = 0; i < size; i++) {
	    range[i] = i;
	}

	return range;
    }

    /**
     * Constructs and returns a list of one element
     * 
     * @param element
     * @return a list containing that element
     */
    public static <T> List<T> singletonList(T element) {
	List<T> singleton = new ArrayList<T>(1);
	singleton.add(element);

	return singleton;
    }

    /**
     * Gets the most common element from a list
     * 
     * @param l
     * @return most common element
     */
    public static <T> T mostCommonElement(List<T> l) {
	Map<T, Integer> weights = new HashMap<T, Integer>();

	for (T elem : l) {
	    weights.put(elem, weights.containsKey(elem) ? weights.get(elem) + 1
		    : 1);
	}

	T ret = null;
	int max = 0;
	for (Entry<T, Integer> e : weights.entrySet()) {
	    if (e.getValue() > max) {
		ret = e.getKey();
		max = e.getValue();
	    }
	}

	return ret;
    }

    /**
     * Creates a list of size * factor through item repetition
     * 
     * @param l
     * @param factor
     * @return the multiplied list
     */
    public static <T> List<T> multiplyList(List<T> l, int factor) {
	if (factor == 1) {
	    return l;
	} else if (factor < 1) {
	    throw new IllegalArgumentException("Invalid factor");
	}

	List<T> ret = new ArrayList<T>(l.size() * factor);

	for (int i = 0; i < factor; i++) {
	    ret.addAll(l);
	}

	return ret;
    }

    /**
     * Gets all permutations of a list
     * 
     * @param l
     * @return list of all permutations of l
     */
    public static <T> List<List<T>> permutations(List<T> l) {
	if (l.size() == 0) {
	    List<List<T>> result = new ArrayList<List<T>>();
	    result.add(new ArrayList<T>());
	    return result;
	}

	T first = l.remove(0);
	List<List<T>> ret = new ArrayList<List<T>>();
	List<List<T>> perms = permutations(l);

	for (List<T> smallerPerms : perms) {
	    for (int index = 0; index <= smallerPerms.size(); index++) {
		List<T> temp = new ArrayList<T>(smallerPerms);
		temp.add(index, first);
		ret.add(temp);
	    }
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
	if (set == null || set.size() == 0) {
	    throw new IllegalArgumentException(
		    "Cannot create subsets of an empty set.");
	} else if (alpha < 1 || alpha > beta || alpha > set.size()) {
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

    /**
     * Checks if the original policies are equal
     * 
     * @param other
     * @return whether or not the source policies are the same
     */
    public boolean isSameAbstraction(AbstractedPolicy other) {
	for (String s : this.gcg.keySet()) {
	    if (this.gcg.get(s) != other.gcg.get(s)) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Checks to see if the object combination is valid under the original
     * policy
     * 
     * @param ocomb
     * @return whether or not the abstraction is possible
     */
    public static boolean isProperAbstraction(State s, List<String> ocomb) {
	Map<String, Boolean> matches = new HashMap<String, Boolean>();
	for (String o : ocomb) {
	    matches.put(o, false);
	}

	for (ObjectInstance oi : s.getAllObjects()) {
	    for (String o : ocomb) {
		if (oi.getName().equals(o)) {
		    matches.put(o, true);
		}
	    }
	}

	for (boolean b : matches.values()) {
	    if (!b) {
		return false;
	    }
	}

	return true;
    }

    /**
     * Gets the abstracted policy
     * 
     * @return this.abstractedPolicy
     */
    public Map<StateHashTuple, GroundedAction> getPolicy() {
	return abstractedPolicy;
    }

    /**
     * Gets the GCG
     * 
     * @return this.gcg
     */
    public Map<String, Integer> getGCG() {
	return gcg;
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
    public StateHashFactory getHashFactory() {
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

    @Override
    public AbstractGroundedAction getAction(State s) {
	return abstractedPolicy.get(hashFactory.hashState(s));
    }

    @Override
    public List<ActionProb> getActionDistributionForState(State s) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isStochastic() {
	return false;
    }

    @Override
    public boolean isDefinedFor(State s) {
	return getAction(s) != null;
    }
}
