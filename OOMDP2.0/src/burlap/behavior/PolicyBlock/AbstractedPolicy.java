package burlap.behavior.PolicyBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import domain.taxiworld.TaxiWorldDomain;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.behavior.singleagent.options.Option;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedPolicy {
    private Set<PolicyBlockPolicy> originalPolicies;
    public Map<StateHashTuple, GroundedAction> abstractedPolicy;

    public static long[] runTaxiLearning(PolicyBlockPolicy policy,
	    StateHashFactory hf, int[][] passPos, int episodes, String filepath)
	    throws IOException {
	return runTaxiLearning(policy, hf, passPos, new ArrayList<Option>(),
		episodes, filepath);
    }

    public static long[] runTaxiLearning(PolicyBlockPolicy policy,
	    StateHashFactory hf, int[][] passPos, Option o, int episodes,
	    String filepath) throws IOException {
	List<Option> os = new ArrayList<Option>();
	os.add(o);
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath);
    }

    public static long[] runTaxiLearning(PolicyBlockPolicy policy,
	    StateHashFactory hf, int[][] passPos, List<? extends Option> os,
	    int episodes, String filepath) throws IOException {
	TaxiWorldDomain.MAXPASS = passPos.length;
	QLearning Q = new QLearning(TaxiWorldDomain.DOMAIN, TaxiWorldDomain.rf,
		TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		TaxiWorldDomain.GAMMA, TaxiWorldDomain.LEARNINGRATE,
		Integer.MAX_VALUE);

	State s = TaxiWorldDomain.getCleanState();
	Q.setLearningPolicy(policy);
	policy.setPlanner(Q);

	for (Option o : os) {
	    Q.addNonDomainReferencedAction(o);
	}

	File f = new File(filepath);
	BufferedWriter b = new BufferedWriter(new FileWriter(f));
	b.write("Episode,Steps\n");
	long[] cumulArr = new long[episodes];
	long cumul = 0;

	for (int i = 0; i < episodes; i++) {
	    TaxiWorldDomain.setAgent(s, 4, 5);
	    TaxiWorldDomain.setGoal(3, 5);

	    for (int j = 1; j <= TaxiWorldDomain.MAXPASS; j++) {
		TaxiWorldDomain.setPassenger(s, j, passPos[j - 1][0],
			passPos[j - 1][1]);
	    }

	    EpisodeAnalysis analyzer = new EpisodeAnalysis();
	    analyzer = Q.runLearningEpisodeFrom(s);
	    cumul += analyzer.numTimeSteps();
	    b.write((i + 1) + "," + cumul + "\n");
	    cumulArr[i] = cumul;
	}
	b.close();

	return cumulArr;
    }

    /**
     * Generates a random options defined over a random chunk of the state space
     * 
     * @param hf
     * @param actionSpace
     * @param stateSpace
     * @return a AbstractedOption randomly initialized
     */
    public static AbstractedOption generateRandomOption(StateHashFactory hf,
	    List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
	Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
	Random rand = new Random();
	int count = 0;
	int max = rand.nextInt((stateSpace.size()));

	// Samples from the state space
	for (StateHashTuple s : stateSpace) {
	    if (count >= max) {
		break;
	    }
	    policy.put(
		    s,
		    new GroundedAction(actionSpace.get(rand.nextInt(actionSpace
			    .size())), ""));
	    count++;
	}

	System.out.println(count);
	System.out.println(stateSpace.size());
	return new AbstractedOption(hf, policy);
    }

    /**
     * This method generates an option to have the agent take a cyclic path in
     * the TaxiWorldDomain. Acts as a sanity check to make sure that options
     * wont cycle forever
     * 
     * @param hf
     * @param actionSpace
     * @param stateSpace
     * @return an option representing a cyclic path
     */
    public static AbstractedOption generateCyclicOption(StateHashFactory hf,
	    List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
	Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
	Action n = null;
	Action s = null;
	Action e = null;
	Action w = null;
	for (Action a : actionSpace) {
	    if (a.getName().equals(TaxiWorldDomain.ACTIONNORTH)) {
		n = a;
	    } else if (a.getName().equals(TaxiWorldDomain.ACTIONSOUTH)) {
		s = a;
	    } else if (a.getName().equals(TaxiWorldDomain.ACTIONEAST)) {
		e = a;
	    } else if (a.getName().equals(TaxiWorldDomain.ACTIONWEST)) {
		w = a;
	    }
	}

	if (n == null || s == null || e == null || w == null) {
	    throw new NullPointerException(
		    "Action space doesn't define the correct actions for this domain.");
	}
	// (1, 5) -> e; (2, 5) -> n; (2, 6) -> w; (1,6) -> s
	for (StateHashTuple st : stateSpace) {
	    int x = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT)
		    .getDiscValForAttribute(TaxiWorldDomain.ATTX);
	    int y = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT)
		    .getDiscValForAttribute(TaxiWorldDomain.ATTY);
	    if (x == 1 && y == 5) {
		policy.put(st, new GroundedAction(e, ""));
	    } else if (x == 2 && y == 5) {
		policy.put(st, new GroundedAction(n, ""));
	    } else if (x == 2 && y == 6) {
		policy.put(st, new GroundedAction(w, ""));
	    } else if (x == 1 && y == 6) {
		policy.put(st, new GroundedAction(s, ""));
	    }
	}

	return new AbstractedOption(hf, policy);
    }

    public static void main(String args[]) throws IOException {
	String path = "C:/Users/Allison/Desktop/";
	TaxiWorldDomain.MAXPASS = 3;
	new TaxiWorldDomain().generateDomain();
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(
		TaxiWorldDomain.CLASSAGENT,
		TaxiWorldDomain.DOMAIN
			.getObjectClass(TaxiWorldDomain.CLASSAGENT).attributeList);
	double epsilon = 0.8;
	int episodes = 1000;
	long startTime = System.currentTimeMillis();
	Random rand = new Random();
	int c = 1;
	int max = TaxiWorldDomain.MAXPASS;
	// Offset must always be one, or there will be value errors with
	// ATTCARRY
	// MAXPASS must never be set higher than max, ATTCARRY will have issues
	// as well
	// If MAXPASS must be set higher, the domain must be regenerated
	int offset = 1;

	TaxiWorldDomain.MAXPASS = rand.nextInt(max) + offset;
	int[][] ps1 = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy1 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy1, hf, ps1, episodes, path
		+ "one.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = rand.nextInt(max) + offset;
	int[][] ps2 = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy2 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy2, hf, ps2, episodes, path
		+ "two.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = rand.nextInt(max) + offset;
	int[][] ps3 = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy3 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy3, hf, ps3, episodes, path
		+ "three.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = rand.nextInt(max) + offset;
	int[][] ps4 = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy4 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy4, hf, ps4, episodes, path
		+ "four.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = rand.nextInt(max) + offset;
	int[][] ps5 = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy5 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy5, hf, ps5, episodes, path
		+ "five.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	ArrayList<PolicyBlockPolicy> toMerge = new ArrayList<PolicyBlockPolicy>();
	toMerge.add(policy1);
	toMerge.add(policy2);
	toMerge.add(policy3);
	toMerge.add(policy4);
	toMerge.add(policy5);

	long uTime = System.currentTimeMillis();
	int depth = 3;
	System.out.println("Starting union merge with depth " + depth + ".");
	List<AbstractedPolicy> merged = unionMerge(hf, toMerge, depth);
	System.out
		.println("Finished union merge; took "
			+ ((System.currentTimeMillis() - uTime) / 60000.0)
			+ " minutes");
	List<AbstractedPolicy> toRemove = new ArrayList<AbstractedPolicy>();
	for (AbstractedPolicy a : merged) {
	    // Temporary measure to prevent scoreUnionMerge from breaking if
	    // there the abstracted policy is empty (happens if the goal objects
	    // are different)
	    if (a.size() == 0) {
		toRemove.add(a);
	    }
	}
	merged.removeAll(toRemove);

	long mTime = System.currentTimeMillis();
	System.out.println("Starting scoring merges.");
	List<Entry<Double, AbstractedPolicy>> scored = scoreUnionMerge(hf,
		merged);
	AbstractedPolicy best1 = scored.get(0).getValue();
	AbstractedPolicy best2 = scored.get(1).getValue();
	System.out.println(scored.size() + " options generated.");
	System.out
		.println("Finished scoring merges; took "
			+ ((System.currentTimeMillis() - mTime) / 60000.0)
			+ " minutes");
	System.out.println("Final policy of size: " + best1.size());
	System.out.println(scored.get(0).getKey());
	System.out.println("Final policy of size: " + best2.size());
	System.out.println(scored.get(1).getKey());
	System.out.println(best1.abstractedPolicy.keySet().iterator().next().s);
	System.out.println(best2.abstractedPolicy.keySet().iterator().next().s);

	List<AbstractedOption> ops = new ArrayList<AbstractedOption>();
	ops.add(new AbstractedOption(hf, best1.abstractedPolicy));
	// ops.add(new AbstractedOption(hf, best2.abstractedPolicy));

	TaxiWorldDomain.MAXPASS = 1;
	new TaxiWorldDomain().generateDomain();
	int[][] ps6 = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy6 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy6, hf, ps6, ops, episodes,
		path + "six.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;
	// TODO

	PolicyBlockPolicy policy7 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy7, hf, ps6, episodes, path
		+ "seven.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);

	System.out.println("Experiment finished. Took a total of "
		+ ((System.currentTimeMillis() - startTime) / 60000.0)
		+ " minutes.");
    }

    public AbstractedPolicy() {
	abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
	originalPolicies = new HashSet<PolicyBlockPolicy>();
    }

    public AbstractedPolicy(AbstractedPolicy p) {
	this();
	this.abstractedPolicy.putAll(p.abstractedPolicy);
	this.originalPolicies.addAll(p.originalPolicies);
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
    public AbstractedPolicy(StateHashFactory sh, PolicyBlockPolicy p,
	    List<PolicyBlockPolicy> ps) {
	State s = findLimitingStateOverall(p, ps);

	Map<String, Integer> lciMap = leastCommonIntersectionState(s,
		getInitialState(p));

	ArrayList<ArrayList<ObjectInstance>> listFromMapping = getListFromMapping(
		lciMap, s);

	ArrayList<ArrayList<ObjectInstance>> listOfCombinations = generateCombinations(listFromMapping);

	abstractedPolicy = scoreAbstraction(sh, getInitialState(p), p,
		listOfCombinations);
	ps.add(p);
	originalPolicies = new HashSet<PolicyBlockPolicy>();
	originalPolicies.addAll(ps);
    }

    /**
     * Scores all possible abstractions and finds the abstraction with the
     * lowest error
     * 
     * @param original
     * @param la
     * @param objList
     * @return
     */
    public static Map<StateHashTuple, GroundedAction> scoreAbstraction(
	    StateHashFactory sh, State reducedState, PolicyBlockPolicy p,
	    ArrayList<ArrayList<ObjectInstance>> objList) {
	List<Map<StateHashTuple, GroundedAction>> policyList = makeNewPolicies(
		sh, objList, reducedState, p);
	ArrayList<GroundedAction> actionSequence = new ArrayList<GroundedAction>();
	ArrayList<ArrayList<GroundedAction>> actionList = new ArrayList<ArrayList<GroundedAction>>();
	Map<Integer, Double> mapOfDiff = new HashMap<Integer, Double>();
	ArrayList<GroundedAction> originalActions = new ArrayList<GroundedAction>();

	int i = 0;
	double lowest = 0;

	Boolean flag = true;
	// TODO
	// this method is assuming that entrySet() preserves sorting, which it
	// doesn't
	// creates action sequences of all abstractions
	for (Map<StateHashTuple, GroundedAction> map : policyList) {
	    actionSequence = new ArrayList<GroundedAction>();
	    for (Entry<StateHashTuple, GroundedAction> entry : map.entrySet()) {
		actionSequence.add(entry.getValue());
	    }
	    actionList.add(actionSequence);
	}

	// generate original action sequence
	for (Entry<StateHashTuple, GroundedAction> entry : p.policy.entrySet()) {
	    originalActions.add(entry.getValue());
	}

	/*
	 * TODO Convert to array list
	 */
	// find difference between original action sequence and abstractions
	for (List<GroundedAction> gaList : actionList) {
	    mapOfDiff.put(i, findDifference(originalActions, gaList));
	    i += 1;
	}

	// finds abstraction with lowest error
	for (Map.Entry<Integer, Double> entry : mapOfDiff.entrySet()) {
	    if (flag) {
		lowest = entry.getValue();
		i = entry.getKey();
	    } else {
		if (entry.getValue() < lowest) {
		    lowest = entry.getValue();
		    i = entry.getKey();
		}
	    }
	}

	return policyList.get(i);
    }

    /**
     * Finds the correct action that corresponds to the given state. If multiple
     * states are identical, Q values are averaged. Otherwise the Q values
     * remain the same.
     * 
     * @param toCheck
     *            - state to find action for
     * @param stateList
     *            - all lowered states from original policy
     * @param oldPolicy
     *            - original policy
     * @return action with the highest Q value
     */
    public static AbstractGroundedAction findCorrectAction(State toCheck,
	    List<State> stateList, PolicyBlockPolicy oldPolicy) {
	List<State> stateMatches = new ArrayList<State>();
	ArrayList<ArrayList<QValue>> qList = new ArrayList<ArrayList<QValue>>();

	// checks for matching among states
	// **there will always be 1+ match, because
	// baseState is in stateList
	for (State s : stateList) {
	    if (toCheck.equals(s)) {
		stateMatches.add(s);
	    }
	}

	// makes list of all Q values that correspond to stateMatches
	for (State s : stateMatches) {
	    QLearningStateNode oi = ((QLearning) oldPolicy.qplanner)
		    .getStateNode(((QLearning) oldPolicy.qplanner).stateHash(s));
	    qList.add((ArrayList<QValue>) oi.qEntry);
	}

	List<QValue> av = findAverages(stateMatches.get(0), qList);

	return findHighestQValue(av);
    }

    public static AbstractGroundedAction findHighestQValue(List<QValue> qList) {
	QValue highest = qList.get(0);

	for (QValue q : qList) {
	    if (q.q > highest.q) {
		highest = q;
	    }
	}

	return highest.a;
    }

    /**
     * Finds averages of Q values in a given list.
     * 
     * @param s
     * @param toAverage
     * @return
     */
    public static List<QValue> findAverages(State s,
	    ArrayList<ArrayList<QValue>> toAverage) {
	List<QValue> averageQ = new ArrayList<QValue>();
	int i = 0;
	int j = 0;
	double sum = 0;
	double average = 0;

	while (i < toAverage.get(0).size()) {
	    sum = 0;
	    for (ArrayList<QValue> qList : toAverage) {
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
     * Uses makeNewPolicy()
     * 
     * @param sh
     * @param listOfCombinations
     *            - combinations of possible abstractions (objects to be turned
     *            into states)
     * @param reducedState
     * @param oldPolicy
     * @return
     */
    public static List<Map<StateHashTuple, GroundedAction>> makeNewPolicies(
	    StateHashFactory sh,
	    ArrayList<ArrayList<ObjectInstance>> listOfCombinations,
	    State reducedState, PolicyBlockPolicy oldPolicy) {
	List<Map<StateHashTuple, GroundedAction>> newMap = new ArrayList<Map<StateHashTuple, GroundedAction>>();
	State s = null;

	for (ArrayList<ObjectInstance> o : listOfCombinations) {
	    s = new State();

	    for (ObjectInstance oo : o) {
		s.addObject(oo);
	    }

	    newMap.add(makeNewPolicy(sh, s, oldPolicy));
	}

	return newMap;
    }

    /**
     * Returns the a state stored in the policy's hashmap.
     * 
     * @param initialPolicy
     * @return
     */
    public static State getInitialState(PolicyBlockPolicy initialPolicy) {
	State initialState = null;
	for (Map.Entry<StateHashTuple, GroundedAction> entry : initialPolicy.policy
		.entrySet()) {
	    initialState = entry.getKey().s;
	}

	return initialState;
    }

    /**
     * Finds the state that corresponds to the Least Common Intersection of a
     * list of policies
     * 
     * @param initialPolicy
     * @param ps
     * @return
     */
    public static State findLimitingStateOverall(
	    PolicyBlockPolicy initialPolicy, List<PolicyBlockPolicy> ps) {
	if (initialPolicy.size() == 0) {
	    throw new IllegalArgumentException("Initial policy is empty.");
	}
	Boolean flag = true;
	State s = null;
	State s1 = null;
	State s2 = null;
	State initialState = null;

	for (PolicyBlockPolicy pol : ps) {
	    if (pol.size() == 0) {
		throw new IllegalArgumentException("Policy is empty: " + pol);
	    }
	    for (Map.Entry<StateHashTuple, GroundedAction> entry : pol.policy
		    .entrySet()) {
		s1 = entry.getKey().s;

		if (flag) {
		    s = s1;
		    flag = false;
		}
	    }
	    s2 = s;

	    s = findLimitingState(generateLCIMappingState(s1),
		    generateLCIMappingState(s2), s1, s2);
	}

	for (Entry<StateHashTuple, GroundedAction> entry : initialPolicy.policy
		.entrySet()) {
	    initialState = entry.getKey().s;
	}

	s = findLimitingState(generateLCIMappingState(initialState),
		generateLCIMappingState(s), initialState, s);

	return s;
    }

    /**
     * Finds quantitative difference between two ArrayLists Used for calculating
     * error
     * 
     * @param actionSequence
     * @param actionSequence2
     * @return
     */
    public static double findDifference(List<GroundedAction> actionSequence,
	    List<GroundedAction> actionSequence2) {
	double error = 0;
	int i = 0;

	while (i < actionSequence.size() && i < actionSequence2.size()) {
	    if (!actionSequence.get(i).equals(actionSequence2.get(i))) {
		error++;
	    }
	    i++;
	}

	if ((i >= actionSequence.size() && i < actionSequence2.size())) {
	    error += actionSequence2.size() - i;
	} else if (i >= actionSequence2.size() && i < actionSequence.size()) {
	    error += actionSequence.size() - i;
	}

	return error / actionSequence.size();
    }

    /**
     * Makes state given a list of ObjectInstances
     * 
     * @param objList
     * @return
     */
    public static State makeStateWith(
	    ArrayList<ArrayList<ObjectInstance>> objList) {
	State s = new State();

	for (ArrayList<ObjectInstance> oList : objList) {
	    for (ObjectInstance o : oList) {
		s.addObject(o);
	    }
	}

	return s;
    }

    /**
     * Orders list of ObjectInstance into alphabetical order
     * 
     * @param unordered
     * @return
     */
    public static ArrayList<ObjectInstance> orderOI(
	    ArrayList<ObjectInstance> unordered) {
	Boolean flag = true;
	ArrayList<ObjectInstance> ordered = new ArrayList<ObjectInstance>(
		unordered.size());
	List<String> nameList = new ArrayList<String>();

	for (ObjectInstance o : unordered) {
	    nameList.add(o.getName());
	}

	Collections.sort(nameList);

	for (String s : nameList) {
	    for (ObjectInstance obj : unordered) {
		if (s.equals(obj.getName()) && flag) {
		    ordered.add(obj);
		    flag = false;
		}
	    }
	    flag = true;
	}

	return ordered;
    }

    /**
     * Generates all combinations given a multidimensional ArrayList of objects.
     * **Does not generate combinations with duplicate objects (assumes all
     * objects have distinct names) If 2 objects need to be added, they will
     * exist twice within sets.
     * 
     * @param sets
     * @return
     */
    public static ArrayList<ArrayList<ObjectInstance>> generateCombinations(
	    ArrayList<ArrayList<ObjectInstance>> sets) {
	ArrayList<ArrayList<ObjectInstance>> output = new ArrayList<ArrayList<ObjectInstance>>();
	ArrayList<ObjectInstance> toOrder;
	int solutions = 1;
	int j = 0;

	for (int i = 0; i < sets.size(); i++) {
	    solutions *= sets.get(i).size();
	}

	for (int i = 0; i < solutions; i++) {
	    j = 1;
	    toOrder = new ArrayList<ObjectInstance>();

	    for (ArrayList<ObjectInstance> set : sets) {
		toOrder.add(set.get((i / j) % set.size()));
		j *= set.size();
	    }

	    if (!output.contains(orderOI(toOrder)) && !hasDuplicates(toOrder)) {
		output.add(orderOI(toOrder));
	    }
	}
	return output;
    }

    /**
     * Checks if there are duplicate objects within the list
     * 
     * @param toCheck
     * @return true, if there are duplicates. otherwise, false
     */
    public static Boolean hasDuplicates(ArrayList<ObjectInstance> toCheck) {
	int count = 0;

	for (ObjectInstance o : toCheck) {
	    for (ObjectInstance oo : toCheck) {
		if (oo.equals(o)) {
		    count += 1;
		}
	    }
	    if (count > 1) {
		return true;
	    }
	    count = 0;
	}
	return false;
    }

    /**
     * Finds list that corresponds to the mapping provided. If there are more
     * than one of a type of object, duplicates that list as many times as there
     * are that object.
     * 
     * @param map
     * @param s
     * @return
     */
    public static ArrayList<ArrayList<ObjectInstance>> getListFromMapping(
	    Map<String, Integer> map, State s) {
	ArrayList<ArrayList<ObjectInstance>> objList = new ArrayList<ArrayList<ObjectInstance>>();
	ArrayList<ArrayList<ObjectInstance>> toAdd = new ArrayList<ArrayList<ObjectInstance>>();
	int i = 1;

	for (Map.Entry<String, Integer> entry : map.entrySet()) {
	    objList.add(getListOfObject(s, entry.getKey()));
	}

	for (ArrayList<ObjectInstance> o : objList) {
	    while (o.size() > i) {
		toAdd.add(o);
		i += 1;
	    }
	    i = 1;
	}

	for (ArrayList<ObjectInstance> o : toAdd) {
	    objList.add(o);
	}

	return objList;
    }

    /**
     * Generates list of one type of object in a domain.
     * 
     * @param s
     * @param name
     * @return
     */
    public static ArrayList<ObjectInstance> getListOfObject(State s, String name) {

	ArrayList<ObjectInstance> objList = new ArrayList<ObjectInstance>();

	for (ObjectInstance o : s.getAllObjects()) {
	    if (o.getObjectClass().name.equals(name)) {
		objList.add(o);
	    }
	}

	return objList;
    }

    /**
     * Generates the map to be used by leastCommonIntersectionState()
     * 
     * @param s
     *            - State to be mapped
     * @return Map that includes the names of the objects and their frequency
     */
    public static Map<String, Integer> generateLCIMappingState(State s) {
	Map<String, Integer> domainCount = new HashMap<String, Integer>();

	for (ObjectInstance o : s.getAllObjects()) {
	    if (domainCount.containsKey(o.getObjectClass().name)) {
		domainCount.put(o.getObjectClass().name,
			domainCount.get(o.getObjectClass().name) + 1);
	    } else {
		domainCount.put(o.getObjectClass().name, 1);
	    }
	}

	return domainCount;
    }

    /**
     * Generates the Least Common Intersection of two states.
     * 
     * @param a
     * @param b
     * @return Map of ObjectClass name and frequency
     */
    public static Map<String, Integer> leastCommonIntersectionState(State a,
	    State b) {

	Map<String, Integer> domain1Count = generateLCIMappingState(a);
	Map<String, Integer> domain2Count = generateLCIMappingState(b);
	ArrayList<String> toRemove = new ArrayList<String>();

	for (Map.Entry<String, Integer> entry : domain1Count.entrySet()) {
	    if (domain2Count.containsKey(entry.getKey())) {
		if (entry.getValue() < domain2Count.get(entry.getKey())) {
		    domain2Count.put(entry.getKey(),
			    domain2Count.get(entry.getKey()));
		    // System.out.println(domain2Count.get(entry.getKey()) + " "
		    // + entry.getKey());
		}
	    } else {
		domain2Count.remove(entry.getKey());
	    }
	}

	for (Map.Entry<String, Integer> entry : domain2Count.entrySet()) {
	    if (!domain1Count.containsKey(entry.getKey())) {
		toRemove.add(entry.getKey());
	    }
	}

	for (String s : toRemove) {
	    domain2Count.remove(s);
	}

	return domain2Count;
    }

    /**
     * Lowers policy to baseState level. Also finds correct action of each
     * state.
     * 
     * @param sh
     * @param baseState
     *            - Least Common Intersection
     * @param oldPolicy
     *            - policy that is being abstracted
     * @return
     */
    public static HashMap<StateHashTuple, GroundedAction> makeNewPolicy(
	    StateHashFactory sh, State baseState, PolicyBlockPolicy oldPolicy) {
	HashMap<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();
	State s = null;
	State hashedState = null;
	List<State> stateList = new ArrayList<State>();

	// generates states that match the baseState's objects, with the
	// policy's states' attributes
	for (Map.Entry<StateHashTuple, GroundedAction> entry : oldPolicy.policy
		.entrySet()) {
	    s = new State();

	    hashedState = entry.getKey().s;

	    for (ObjectInstance o : baseState.getAllObjects()) {
		s.addObject(o);
	    }

	    for (ObjectInstance hashO : hashedState.getAllObjects()) {
		for (ObjectInstance o : s.getAllObjects()) {
		    if (hashO.getName().equals(o.getName())) {
			for (Attribute a : hashO.getObjectClass().attributeList) {
			    s.getObject(o.getName()).setValue(a.name,
				    hashO.getDiscValForAttribute(a.name));
			}
		    }
		}
	    }

	    stateList.add(s.copy());
	}

	for (State st : stateList) {
	    newPolicy
		    .put(sh.hashState(st),
			    (GroundedAction) findCorrectAction(st, stateList,
				    oldPolicy));
	}

	return newPolicy;
    }

    /**
     * Finds the state that refers to the least common intersection of 2 states.
     * 
     * @param d1
     *            Map of state in (ObjectClass name, frequency) form.
     * @param d2
     * @param s1
     * @param s2
     * @return State that limits the intersection
     */
    public static State findLimitingState(Map<String, Integer> d1,
	    Map<String, Integer> d2, State s1, State s2) {
	for (Entry<String, Integer> entry1 : d1.entrySet()) {
	    if (!d2.containsKey(entry1.getKey())
		    || d2.containsKey(entry1.getKey())
		    && d2.get(entry1.getKey()) < entry1.getValue()) {
		return s2;
	    }
	}
	return s1;
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
	    List<PolicyBlockPolicy> policies) {
	if (policies.size() < 2) {
	    throw new IllegalArgumentException("Need at least 2 policies.");
	}

	List<AbstractedPolicy> abstractedPolicies = new ArrayList<AbstractedPolicy>();
	for (int i = 0; i < policies.size(); i++) {
	    List<PolicyBlockPolicy> newPolicies = new ArrayList<PolicyBlockPolicy>();
	    newPolicies.addAll(policies);

	    PolicyBlockPolicy temp = newPolicies.remove(i);
	    abstractedPolicies.add(new AbstractedPolicy(hf, temp, newPolicies));
	    newPolicies.add(temp);
	}

	return abstractedPolicies;
    }

    public boolean isSameAbstraction(AbstractedPolicy other) {
	return this.getOriginalPolicies().equals(other.getOriginalPolicies());
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
	    if (a != null && a.equals(e.getValue())) {
		merged.abstractedPolicy.put(e.getKey(), e.getValue());
	    }
	}

	return merged;
    }

    /**
     * This method assumes that the order of merging is commutative.
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
     * Abstraction happens on a per-group basis of policies to merge.
     * 
     * @param policies
     *            - the set of policies to be merged
     * @param depth
     *            - the upper bound of k
     * @return performs the merge operation on all k-subsets of the list of
     *         policies
     */
    public static List<AbstractedPolicy> unionMerge(StateHashFactory hf,
	    List<PolicyBlockPolicy> policies, int depth) {
	List<AbstractedPolicy> mergedPolicies = new ArrayList<AbstractedPolicy>();
	for (List<PolicyBlockPolicy> ps : getSubsets(policies, 2, depth)) {
	    mergedPolicies.add(merge(abstractAll(hf, ps)));
	}

	return mergedPolicies;
    }

    /**
     * Grounds each abstracted policy to its set of original policies to score
     * the error in abstraction Error is measured as number of states with the
     * same action / total number of states. The first element has the greatest
     * score.
     * 
     * @param hf
     *            - the hashing factory
     * @param merged
     *            - the list of merged abstracted policies
     * @return the list of all abstracted policies with relative scores
     */
    public static List<Entry<Double, AbstractedPolicy>> scoreUnionMerge(
	    StateHashFactory hf, List<AbstractedPolicy> merged) {
	List<Entry<Double, AbstractedPolicy>> ret = new ArrayList<Entry<Double, AbstractedPolicy>>();

	for (AbstractedPolicy abs : merged) {
	    double totalMatch = 0.;

	    for (PolicyBlockPolicy orig : abs.getOriginalPolicies()) {
		List<PolicyBlockPolicy> temp = new ArrayList<PolicyBlockPolicy>();
		PolicyBlockPolicy tempP = new PolicyBlockPolicy(
			orig.getEpsilon());
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

	    ret.add(new AbstractMap.SimpleEntry<Double, AbstractedPolicy>(
		    totalMatch, abs));
	}

	Collections.sort(ret,
		new Comparator<Entry<Double, AbstractedPolicy>>() {
		    @Override
		    public int compare(Entry<Double, AbstractedPolicy> arg0,
			    Entry<Double, AbstractedPolicy> arg1) {
			if (arg0.getKey() > arg1.getKey()) {
			    return -1;
			} else if (arg0.getKey() < arg1.getKey()) {
			    return 1;
			}

			return 0;
		    }

		});

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
	int size = set.size();
	int[] range = new int[size];
	for (int i = 0; i < size; i++) {
	    range[i] = i;
	}

	for (int i = alpha; i <= beta; i++) {
	    int[][] combs = nexksb(range, i);
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
    private static long binomial(int n, int k) {
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
     * Gets the set of original policies to avoid changing them by mistake
     * 
     * @return set of original policies
     */
    public Set<PolicyBlockPolicy> getOriginalPolicies() {
	return originalPolicies;
    }

    /**
     * Gets the size of the state space for which the policy is defined
     * 
     * @return the size of the policy
     */
    public int size() {
	return abstractedPolicy.size();
    }

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