package burlap.behavior.singleagent.pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public abstract class AbstractedPolicyFactory<P extends Policy> {
    StateHashFactory hf;

    public abstract List<AbstractedPolicy> abstractAll(
	    List<P> policies);

    public abstract AbstractedPolicy merge(
	    List<AbstractedPolicy> abstractedPolicies);

    public abstract Map<AbstractedPolicy, Double> powerMerge(
	    List<P> policies, int depth, int maxPol);

    public static Map<String, Integer> greatestCommonGeneralization(
	    List<State> ss) {
	List<Map<String, Integer>> mappings = new ArrayList<Map<String, Integer>>(
		ss.size());
	Map<String, List<Attribute>> attributes = new HashMap<String, List<Attribute>>();
	Map<String, Integer> gcg = new HashMap<String, Integer>();
	int i = 0;

	// For each state, add the map of object class => count to mappings
	for (State s : ss) {
	    mappings.add(new HashMap<String, Integer>());

	    // For each object, add the number of instances of each object class
	    for (ObjectInstance oi : s.getAllObjects()) {
		String className = oi.getTrueClassName();

		if (!mappings.get(i).containsKey(className)) {
		    mappings.get(i).put(className, 1);
		    List<Attribute> atts = oi.getObjectClass().attributeList;

		    // Perform loose type-checking to make sure the objects
		    // classes are correct
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
		// If the object class does not exist for all states, remove it
		gcg.remove(e.getKey());
	    }
	}

	return gcg;
    }

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
     * Implements the NEXKSB algorithm. Gets all k-subsets of the set provided. Implementation provided here: http://stackoverflow.com/a/15603638
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
