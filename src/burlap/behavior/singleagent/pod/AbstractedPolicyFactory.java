package burlap.behavior.singleagent.pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;

/**
 * This class acts within the abstract factory pattern to facilitate easy access
 * to generator methods seen in both Portable Option Discovery (POD) [1] and
 * PolicyBlocks [2].
 * 
 * <p>
 * 1. Nicholay Topin, Nicholas Haltmeyer, Shawn Squire, John Winder, Marie
 * desJardins, James MacGlashan, "Portable option discovery for automated
 * learning transfer in object-oriented Markov Decision Processes,"
 * <i>Proceedings of the International Joint Conference on Artificial
 * Intelligence (IJCAI-15)</i>, Buenos Aires, Argentina, July 2015. <br>
 * 2. Pickett, Marc, and Andrew G. Barto.
 * "Policyblocks: An algorithm for creating useful macro-actions in reinforcement learning."
 * <i>ICML</i>. Vol. 2. 2002.
 * </p>
 * 
 * @author Nicholas Haltmeyer
 * 
 * @param <P extends burlap.behavior.singleagent.Policy>
 *        The policy for which the extending sub-classes abstract from
 */
public abstract class AbstractedPolicyFactory<P extends Policy> {
    /**
     * The object used to properly transform state objects into state hash
     * tuples.
     */
    protected StateHashFactory hf;

    /**
     * Abstracts each provided policy relative to another.
     * 
     * @param policies
     *            The policies to abstract relative to.
     * @return The newly abstracted policies
     */
    public abstract List<AbstractedPolicy<P>> abstractAll(List<P> policies);

    /**
     * Merges the provided policies into a single policy.
     * 
     * @param abstractedPolicies
     *            The policies to be abstracted
     * @return The newly merged policy.
     */
    public abstract AbstractedPolicy<P> merge(
	    List<AbstractedPolicy<P>> abstractedPolicies);

    /**
     * Scores a merged policy relative to its original policies.
     * 
     * @param np
     *            The newly formed merged policy
     * @return A real number score
     */
    public abstract double scoreMerge(AbstractedPolicy<P> np);

    /**
     * Performs the power merge as described in PolicyBlocks [2].
     * 
     * @param policies
     *            The policies to merge
     * @param depth
     *            The depth of which to generate subsets (2 to doubles, 3 for
     *            triples, etc.)
     * @param maxPol
     *            The number of policies to generate
     * @return A sorted list of maxPol policies, along with their score.
     */
    public abstract List<Entry<AbstractedPolicy<P>, Double>> powerMerge(
	    List<P> policies, int depth, int maxPol);

    /**
     * Subtracts redundant references within each policy. May do nothing.
     * 
     * @param merged
     *            The policies formed as a result of powerMerge
     */
    public abstract void subtractAll(
	    List<Entry<AbstractedPolicy<P>, Double>> merged);

    /**
     * Samples a state from the given policy. May not be supported for all
     * policy types.
     * 
     * @param policy
     *            The policy to sample from
     * @return A seemingly random state from the policy
     */
    public abstract State sampleState(P policy);

    /**
     * Samples a state from the given policy. May not be supported for all
     * policy types.
     * 
     * @param policy
     *            The policy to sample from
     * @return A seemingly random state from the policy
     */
    public abstract State sampleState(AbstractedPolicy<P> policy);

    /**
     * Creates the greatest common generalization between states. That is, the
     * intersection of object classes and their attribute schemas.
     * 
     * @param ss
     *            The states to intersect
     * @return A mapping of object classes (with potentially reduced attributes)
     *         to counts
     */
    public static Map<ObjectClass, Integer> greatestCommonGeneralization(
	    List<State> ss) {
	Map<ObjectClass, Integer> gcg = new HashMap<ObjectClass, Integer>();
	Map<String, List<Attribute>> atts = new HashMap<String, List<Attribute>>();
	Map<String, Integer> classCounts = new HashMap<String, Integer>();

	for (State s : ss) {
	    List<String> classesSeen = new ArrayList<String>();
	    for (List<ObjectInstance> objs : s.getAllObjectsByTrueClass()) {
		String curClass = objs.get(0).getObjectClass().name;
		classesSeen.add(curClass);

		if (!classCounts.containsKey(curClass)) {
		    classCounts.put(curClass, objs.size());
		} else {
		    if (classCounts.get(curClass) > objs.size()) {
			classCounts.put(curClass, objs.size());
		    }
		}

		for (ObjectInstance obj : objs) {
		    if (atts.containsKey(curClass)) {
			// Find the intersection between the currently stored
			// list of attributes and the most recently seen,
			// for the current object class
			List<Attribute> curAtts = atts.get(curClass);
			Set<Attribute> inter = new HashSet<Attribute>(curAtts);
			inter.retainAll(obj.getObjectClass().attributeList);
			List<Attribute> newAtts = new ArrayList<Attribute>(
				inter);

			atts.put(curClass, newAtts);
		    } else {
			// Initialize the counts
			atts.put(curClass, obj.getObjectClass().attributeList);
		    }
		}
	    }

	    for (String gcgClass : atts.keySet()) {
		if (!classesSeen.contains(gcgClass)) {
		    // Remove any object classes that don't exist in this state
		    atts.remove(gcgClass);
		    classCounts.remove(gcgClass);
		}
	    }
	}

	for (String classCount : classCounts.keySet()) {
	    // Construct the final structure
	    ObjectClass finalClass = new ObjectClass(null, classCount);
	    finalClass.setAttributes(atts.get(classCount));

	    gcg.put(finalClass, classCounts.get(classCount));
	}

	return gcg;
    }

    /**
     * Generates all combinations of objects valid under the current state and
     * the GCG.
     * 
     * @param gcg
     *            The greatest common generalization
     * @param s
     *            The state the reduce according to the GCG and then explode
     *            according to the number of combinations
     * @return A list of combinations of object classes
     */
    public static List<List<ObjectClass>> generateAllCombinations(
	    Map<ObjectClass, Integer> gcg, State s) {
	Map<ObjectClass, List<List<ObjectClass>>> combs = new HashMap<ObjectClass, List<List<ObjectClass>>>();

	for (Entry<ObjectClass, Integer> e : gcg.entrySet()) {
	    if (s.getFirstObjectOfClass(e.getKey().name) == null) {
		throw new IllegalArgumentException(
			"State provided does not match the GCG.");
	    }

	    List<ObjectClass> objClasses = new ArrayList<ObjectClass>();
	    for (int i = 0; i < s.getObjectsOfTrueClass(e.getKey().name).size(); i++) {
		objClasses.add(e.getKey().copy(null));
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
     * to those in s2. Used in checking for equality in the merge function. Note
     * that this method will not intersect the attributes belonging to each
     * object class. That is, it assumes all object instances belong to the same
     * class schema if the names are equal. This can be performed by matching
     * the returned states according to the GCG.
     * 
     * @param s1
     * @param s2
     * @return All possible arrangement of s1 with respect to s2
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

	    firstPass = false;
	}

	return combs;
    }

    /**
     * Creates a state that only contains the objects provided.
     * 
     * @param s
     *            The state to reduce
     * @param classes
     *            The object classes to consider
     * @return A newly reduced state
     */
    public static State formState(State s, List<ObjectClass> classes) {
	// The use of a temporary object class here is pretty hacky (the fields
	// needed are private). Hopefully will be able to fix in BURLAP 2.0.
	class TempObjectInstance extends ObjectInstance {
	    public TempObjectInstance(ObjectInstance oi) {
		super(oi);
	    }

	    public TempObjectInstance(ObjectClass oc, String name) {
		super(oc, name);
	    }

	    public ObjectInstance formObjectInstance(ObjectClass oc) {
		TempObjectInstance ret = new TempObjectInstance(oc, name);
		List<Value> newValues = new ArrayList<Value>(ret.values.size());

		for (String valName : ret.obClass.attributeIndex.keySet()) {
		    newValues.add(getValueForAttribute(valName));
		}

		ret.values = newValues;
		return (ObjectInstance) ret;
	    }
	}

	State newS = new State();
	List<String> classNames = new ArrayList<String>(classes.size());
	Map<String, Integer> classMap = new HashMap<String, Integer>();

	int k = 0;
	for (ObjectClass oc : classes) {
	    classNames.add(oc.name);
	    classMap.put(oc.name, k);
	    k++;
	}

	for (ObjectInstance oi : s.getAllObjects()) {
	    if (classNames.contains(oi.getTrueClassName())) {
		// Remove any attributes that don't exist within the gcg
		TempObjectInstance toi = new TempObjectInstance(oi);
		ObjectInstance newOI = toi.formObjectInstance(classes
			.get(classMap.get(oi.getTrueClassName())));
		newS.addObject(newOI);
	    }
	}

	return newS;
    }

    /**
     * Gets the count of each object class in a state. Note that this method
     * selects the object class to index by in an observably random fashion.
     * That is, no checking is performed to make sure all object classes of the
     * same name have identical schemas.
     * 
     * @param s
     *            The state to count
     * @return A mapping of object classes to counts
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
     * Creates a list initialized to its indices.
     * 
     * @param size
     *            The number of elements to generate
     * @return Array of [0 .. size-1]
     */
    public static int[] range(int size) {
	int[] range = new int[size];
	for (int i = 0; i < size; i++) {
	    range[i] = i;
	}

	return range;
    }

    /**
     * Constructs and returns a list of one element.
     * 
     * @param element
     *            The element to enclose
     * @return A list containing that element
     */
    public static <T> List<T> singletonList(T element) {
	List<T> singleton = new ArrayList<T>(1);
	singleton.add(element);

	return singleton;
    }

    /**
     * Gets the most common element from a list. If there exists a tie, a random
     * number is rolled.
     * 
     * @param l
     *            The list in question
     * @return The most common element
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
     * Creates a list of size * factor through item repetition. Identical to the
     * Python operation.
     * 
     * @param l
     *            The list in question
     * @param factor
     *            The factor of multiplication
     * @return The multiplied list
     */
    public static <T> List<T> multiplyList(List<T> l, int factor) {
	if (factor == 1) {
	    return l;
	} else if (factor < 1) {
	    throw new IllegalArgumentException("Invalid factor.");
	}

	List<T> ret = new ArrayList<T>(l.size() * factor);

	for (int i = 0; i < factor; i++) {
	    ret.addAll(l);
	}

	return ret;
    }

    /**
     * Gets all permutations of a list.
     * 
     * @param l
     *            The list in question
     * @return List of all permutations of l
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
     * Generates all k-subsets from alpha to beta.
     * 
     * @param set
     *            the set to be permuted
     * @param alpha
     *            lower bound of k
     * @param beta
     *            upper bound of k
     * @return A list of all k-subsets from alpha to beta
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
     * Source: http://stackoverflow.com/questions/4504974/
     * 
     * @param k
     *            size of subset tuples
     * @param set
     *            the set to be generated from
     * @return All subsets of size k
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
     * Performs the binomial coefficient function C(n, k). Source:
     * http://stackoverflow.com/questions/4504974/
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
     * policy.
     * 
     * @param oComb
     * @return Whether or not the abstraction is possible
     */
    public static boolean isProperAbstraction(State s, List<ObjectClass> oComb) {
	Map<String, Boolean> matches = new HashMap<String, Boolean>();
	for (ObjectClass o : oComb) {
	    matches.put(o.name, false);
	}

	for (ObjectInstance oi : s.getAllObjects()) {
	    for (ObjectClass oc : oComb) {
		if (oi.getName().equals(oc)) {
		    if (oi.getObjectClass().attributeMap.keySet().equals(
			    oc.attributeMap.keySet())) {
			matches.put(oc.name, true);
		    }
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
