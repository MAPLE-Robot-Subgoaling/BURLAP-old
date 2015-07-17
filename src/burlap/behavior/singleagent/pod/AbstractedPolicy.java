package burlap.behavior.singleagent.pod;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.ObjectClass;

/**
 * This class acts as a top-level abstract structure for the policy abstraction
 * procedure used in Portable Option Discovery (POD) [1].
 * 
 * <p>
 * 1. Nicholay Topin, Nicholas Haltmeyer, Shawn Squire, John Winder, Marie
 * desJardins, James MacGlashan, “Portable option discovery for automated
 * learning transfer in object-oriented Markov Decision Processes,”
 * <i>Proceedings of the International Joint Conference on Artificial
 * Intelligence (IJCAI-15)</i>, Buenos Aires, Argentina, July 2015.
 * </p>
 * 
 * @author Nicholas Haltmeyer
 * 
 * @param <P extends burlap.behavior.singleagent.Policy>
 *        The policy for which the extending sub-classes abstract from
 */
public abstract class AbstractedPolicy<P extends Policy> {
    /**
     * The policy factory object called on for static operations used by POD.
     */
    protected AbstractedPolicyFactory<P> apf;
    /**
     * The object used to properly transform state objects into state hash
     * tuples.
     */
    protected StateHashFactory hf;
    /**
     * The greatest common generalization -- an intersection of objects and
     * their attributes between states.
     */
    protected Map<ObjectClass, Integer> gcg;
    /**
     * The set of original policies leveraged for abstraction.
     */
    protected Set<P> originalPolicies;
    /**
     * The newly generated abstracted policy.
     */
    protected P abstractedPolicy;

    /**
     * Generates and scores the top n abstracted policy candidates
     * 
     * @param ip
     *            The initial policy
     * @param oiCombs
     *            The specific combination of objects to abstract relative to
     * @param n
     *            The number of candidates allowed for selection
     * @return A list containing pairs of abstracted policies and their score
     */
    public abstract List<Entry<P, Double>> generatePolicyCandidates(P ip,
	    List<List<ObjectClass>> oiCombs, int n);

    /**
     * Scores a newly formed abstracted policy with respect to an initial policy
     * 
     * @param ip
     *            The initial policy
     * @param np
     *            The newly abstracted policy
     * @return A real number score of similarity -- will be scaled differently
     *         depending on criteria of abstraction
     */
    public abstract double scoreAbstraction(P ip, P np);

    /**
     * Merges the current abstracted policy with the one provided. No
     * abstraction should be performed during this step.
     * 
     * @param otherPolicy
     *            The policy to merge with
     * @return The newly merged policy
     */
    public abstract AbstractedPolicy<P> mergeWith(
	    AbstractedPolicy<P> otherPolicy);

    /**
     * Determines whether the current policy is on the same level of abstraction
     * as another
     * 
     * @param otherPolicy
     *            The policy to check with respect to
     * @return T/F
     */
    public abstract boolean isSameAbstraction(AbstractedPolicy<P> otherPolicy);

    /**
     * Fetches the true policy representation
     * 
     * @return {@link #abstractedPolicy}
     */
    public abstract Policy getPolicy();

    /**
     * Fetches the greatest common generalization
     * 
     * @return {@link #gcg}
     */
    public Map<ObjectClass, Integer> getGCG() {
	return gcg;
    }

    /**
     * Fetches the set of original policies
     * 
     * @return {@link #originalPolicies}
     */
    public Set<P> getOriginalPolicies() {
	return originalPolicies;
    }

    /**
     * Fetches the hash factory associated with this policy
     * 
     * @return {@link #hf}
     */
    public StateHashFactory getHashFactory() {
	return hf;
    }

    /**
     * Fetches the size of the policy -- typically the number of entries within
     * a table.
     * 
     * @return {@link #abstractedPolicy.size()}
     */
    public abstract int size();
}
