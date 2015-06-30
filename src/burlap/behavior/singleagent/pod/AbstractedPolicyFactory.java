package burlap.behavior.singleagent.pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public abstract class AbstractedPolicyFactory<P extends Policy> {
    StateHashFactory hf;

    public abstract List<AbstractedPolicy<P>> abstractAll(List<P> policies);

    public abstract AbstractedPolicy<P> merge(
	    List<AbstractedPolicy<P>> abstractedPolicies);

    public abstract double scoreMerge(AbstractedPolicy<P> np);

    public abstract List<Entry<AbstractedPolicy<P>, Double>> powerMerge(
	    List<P> policies, int depth, int maxPol);

    public abstract void subtractAll(
	    List<Entry<AbstractedPolicy<PolicyBlocksPolicy>, Double>> merged);

    public abstract State sampleState(P policy);

    public abstract State sampleState(AbstractedPolicy<P> policy);

    public static Map<ObjectClass, Integer> greatestCommonGeneralization(
	    List<State> ss) {
	List<Map<ObjectClass, Integer>> mappings = new ArrayList<Map<ObjectClass, Integer>>(
		ss.size());
	Map<ObjectClass, List<Attribute>> attributes = new HashMap<ObjectClass, List<Attribute>>();
	Map<ObjectClass, Integer> gcg = new HashMap<ObjectClass, Integer>();
	int i = 0;

	// For each state, add the map of object class => count to mappings
	for (State s : ss) {
	    mappings.add(new HashMap<ObjectClass, Integer>());

	    // For each object, add the number of instances of each object class
	    for (ObjectInstance oi : s.getAllObjects()) {
		ObjectClass objClass = oi.getObjectClass();

		if (!mappings.get(i).containsKey(objClass)) {
		    mappings.get(i).put(objClass, 1);
		    List<Attribute> atts = oi.getObjectClass().attributeList;

		    // Perform loose type-checking to make sure the objects
		    // classes are correct
		    if (!attributes.containsKey(objClass)) {
			// Attributes of this class haven't been set yet
			attributes.put(objClass, atts);
		    } else {
			if (!attributes.get(objClass).equals(atts)) {
			    throw new IllegalArgumentException(
				    "Attributes belonging to the class "
					    + objClass.name + " don't match.");
			}
		    }
		} else {
		    mappings.get(i).put(objClass,
			    mappings.get(i).get(objClass) + 1);
		}
	    }

	    i++;
	}

	Map<ObjectClass, Integer> classCount = new HashMap<ObjectClass, Integer>();
	// Initialize the GCG
	for (Entry<ObjectClass, Integer> e : mappings.get(0).entrySet()) {
	    gcg.put(e.getKey(), e.getValue());
	    classCount.put(e.getKey(), 1);
	}
	mappings.remove(0);

	// Fill up the GCG with the greatest intersection between all states
	for (Map<ObjectClass, Integer> mapping : mappings) {
	    for (Entry<ObjectClass, Integer> e : mapping.entrySet()) {
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
	for (Entry<ObjectClass, Integer> e : classCount.entrySet()) {
	    if (gcg.containsKey(e.getKey()) && e.getValue() < ss.size()) {
		// If the object class does not exist for all states, remove it
		gcg.remove(e.getKey());
	    }
	}

	return gcg;
    }

    public static List<List<ObjectClass>> generateAllCombinations(State s,
	    Map<ObjectClass, Integer> gcg) {
	Map<ObjectClass, List<List<ObjectClass>>> combs = new HashMap<ObjectClass, List<List<ObjectClass>>>();

	for (Entry<ObjectClass, Integer> e : gcg.entrySet()) {
	    if (s.getFirstObjectOfClass(e.getKey().name) == null) {
		throw new IllegalArgumentException(
			"State provided does not match the GCG.");
	    }
	    List<ObjectClass> objClasses = new ArrayList<ObjectClass>();
	    for (ObjectInstance oi : s.getObjectsOfTrueClass(e.getKey().name)) {
		objClasses.add(oi.getObjectClass());
	    }

	    // Only want the kth subset
	    List<List<ObjectClass>> subsets = getSubsets(objClasses,
		    e.getValue(), e.getValue());
	    combs.put(e.getKey(), subsets);
	}

	List<List<ObjectClass>> classes = new ArrayList<List<ObjectClass>>();
	boolean firstPass = true;
	for (List<List<ObjectClass>> objClass : combs.values()) {
	    if (firstPass) {
		classes.addAll(objClass);
		firstPass = false;
		continue;
	    }

	    int originalSize = classes.size();
	    int c = 0;
	    int f = objClass.size();
	    classes = multiplyList(classes, f);

	    for (List<ObjectClass> objComb : objClass) {
		for (int i = c * originalSize; i < (c + 1) * originalSize; i++) {
		    List<ObjectClass> temp = classes.get(i);
		    temp.addAll(objComb);
		    classes.set(i, temp);
		}

		c++;
	    }
	}

	return classes;
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
     * Creates a state that only contains the objects in object names
     * 
     * @param s
     * @param onames
     * @return a new reduced state
     */
    public static State formState(State s, List<ObjectClass> classes) {
	State newS = new State();
	for (ObjectInstance oi : s.getAllObjects()) {
	    if (classes.contains(oi.getClass())) {
		newS.addObject(oi);
	    }
	}

	return newS;
    }

    /**
     * Gets the count of each object class in a state
     * 
     * @param s
     * @return mapping of object class -> count
     */
    public static Map<ObjectClass, Integer> getObjectCounts(State s) {
	Map<ObjectClass, Integer> cs = new HashMap<ObjectClass, Integer>();
	List<List<ObjectInstance>> os = s.getAllObjectsByTrueClass();

	for (List<ObjectInstance> o : os) {
	    cs.put(o.get(0).getObjectClass(), o.size());
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
    public static <T> List<List<T>> permutations(List<T> t) {
	List<T> l = new ArrayList<T>(t);
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
}
