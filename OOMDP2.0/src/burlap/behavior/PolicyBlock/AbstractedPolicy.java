package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import domain.taxiworld.TaxiWorldDomain;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedPolicy {
	private Map<StateHashTuple, GroundedAction> abstractedPolicy;
	private List<PolicyBlockPolicy> originalPolicies;
	public State newState;
	public List<ObjectInstance> droppedAttr;
	public List<ObjectInstance> droppedObj;
	
	public static void main(String args[]){
		TaxiWorldDomain.MAXPASS = 1;
		
		TaxiWorldDomain td1 = new TaxiWorldDomain();
		td1.generateDomain();
		State s1 = TaxiWorldDomain.getCleanState();
        int[][] passPos1 = {
                {1, 1}
        };
		PolicyBlockPolicy newPolicy1 = new PolicyBlockPolicy((QLearning) TaxiWorldDomain.Q, 0.9);
        ((QLearning) TaxiWorldDomain.Q).setLearningPolicy(newPolicy1);
        for (int i = 0; i < 1000; i++) {
            TaxiWorldDomain.setAgent(s1, 4, 5);
            TaxiWorldDomain.setGoal(s1, 4, 5);
            
            for (int j = 1; j <= TaxiWorldDomain.MAXPASS; j++) {
            	TaxiWorldDomain.setPassenger(s1, j, passPos1[j - 1][0], passPos1[j - 1][1]);
            }

            TaxiWorldDomain.analyzer = new EpisodeAnalysis();
            //System.out.print("Episode " + i + ": ");
            TaxiWorldDomain.analyzer = TaxiWorldDomain.Q.runLearningEpisodeFrom(s1);
            if (i % 1000 == 0) {
            	System.out.println("1: " + i);
            }
            //System.out.println("\tSteps: " + TaxiWorldDomain.analyzer.numTimeSteps());
        }
		
		TaxiWorldDomain td2 = new TaxiWorldDomain();
		td2.generateDomain();
		State s2 = TaxiWorldDomain.getCleanState();
        int[][] passPos2 = {
                {1, 3}
        };
		PolicyBlockPolicy newPolicy2 = new PolicyBlockPolicy((QLearning) TaxiWorldDomain.Q, 0.9);
        ((QLearning) TaxiWorldDomain.Q).setLearningPolicy(newPolicy2);
        for (int i = 0; i < 1000; i++) {
            TaxiWorldDomain.setAgent(s2, 4, 5);
            TaxiWorldDomain.setGoal(s2, 4, 5);
            
            for (int j = 1; j <= TaxiWorldDomain.MAXPASS; j++) {
            	TaxiWorldDomain.setPassenger(s2, j, passPos2[j - 1][0], passPos2[j - 1][1]);
            }

            TaxiWorldDomain.analyzer = new EpisodeAnalysis();
            //System.out.print("Episode " + i + ": ");
            TaxiWorldDomain.analyzer = TaxiWorldDomain.Q.runLearningEpisodeFrom(s2);
            if (i % 1000 == 0) {
            	System.out.println("2: " + i);
            }
            //System.out.println("\tSteps: " + TaxiWorldDomain.analyzer.numTimeSteps());
        }
        
        ArrayList<PolicyBlockPolicy> pis = new ArrayList<PolicyBlockPolicy>();
        pis.add(newPolicy1);
        pis.add(newPolicy2);
		for (AbstractedPolicy ap: unionMerge(pis, pis.size())) {
			System.out.println(ap.abstractedPolicy.size());
			System.out.println("*************\n");
		}        
	}
	
	public AbstractedPolicy() {
		abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
		originalPolicies = new ArrayList<PolicyBlockPolicy>();
	}
	
	// TODO Once abstraction is implemented, have all of the dropped objects copied as well
	public AbstractedPolicy(AbstractedPolicy p) {
		this();
		this.abstractedPolicy.putAll(p.abstractedPolicy);
		this.originalPolicies.addAll(p.originalPolicies);
	}

	/**
	 * Abstracts away any attributes/objects that don't affect the original policy.
	 * **Assumes x and y attributes are named "x" and "y", respectively.
	 * @param d
	 * @param s
	 * @param p
	 * @param la
	 * @param map Used to verify that coordinates that would be in a wall aren't checked
	 */
	public AbstractedPolicy(Domain d,State s,PolicyBlockPolicy p,LearningAgent la,int [][] map) {		
		EpisodeAnalysis ea = new EpisodeAnalysis();
		
		List<ObjectInstance> allObj = s.getAllObjects();
		droppedAttr  = new ArrayList<ObjectInstance>();
		droppedObj = new ArrayList<ObjectInstance>();
		
		int numObjLeft = s.getAllObjects().size();
		int numAttrDropped = 0;
		int numMatch = 0;
		int originalActions = la.getLastLearningEpisode().actionSequence.size();
		int original = 0;
		int x = 0;
		int y = 0;
		
		newState = new State();
		
		Boolean flag = false;
		
		while (numObjLeft > 0) {
			ObjectInstance o = s.getAllObjects().get(numObjLeft - 1);
			numObjLeft -= 1;
			
			for (Attribute a : o.getObjectClass().attributeList) {
				original = o.getDiscValForAttribute(a.name);
				
				//loops through every possibility of the attribute, checking for changes in policy length
				//must check for x and y that possibilities are not in walls
				for (String str : a.discValues) {
					o.setValue(a.name,str);
					if (a.name == "x") {
						y = o.getDiscValForAttribute("y");
						if (map[Integer.parseInt(str)][y] != 1) {
							for(int i=0;i < 1000;i++) {
								ea = la.runLearningEpisodeFrom(s);
							}
						}
					} else if (a.name == "y") {
						x = o.getDiscValForAttribute("x");
						if (map[x][Integer.parseInt(str)] != 1) {
							for(int i=0;i < 1000;i++) {
								ea = la.runLearningEpisodeFrom(s);	
							}
						}
					} else {
						for(int i=0;i < 2000;i++) {
							ea = la.runLearningEpisodeFrom(s);	
						}	
						//System.out.println(o.getName() + " " + ea.actionSequence);
					}
					
					//if size of policy changes, no need to continue checking possibilities
					if (originalActions != ea.actionSequence.size()) {
						numMatch = 0;
						break;
					} else {
						numMatch += 1;
					}

				}
				
				//if the policy doesn't change for every value of the attribute, the attribute is dropped
				if (numMatch == a.discValues.size()) {
					//System.out.println("Dropping attribute " + a.name + " from object " + o.getName());
					droppedAttr.add(removeAttribute(d,a,o));
					numAttrDropped += 1;
				}
				numMatch = 0;
				o.setValue(a.name,original);
			}
		
			//if all attributes in an object are dropped, the object is dropped
			if (numAttrDropped == o.getObjectClass().numAttributes()) {
				allObj.remove(o);
				droppedObj.add(o);
			}
			numAttrDropped = 0;
		}
		
		//recreates state without dropped attributes/objects
		for (ObjectInstance finalObj : allObj) {
			for (ObjectInstance dropped : droppedAttr) {
				if (finalObj.getName().equals(dropped.getName())) {
					newState.addObject(dropped);
					flag = true;
				}
			}
			
			if (!flag) {
				newState.addObject(finalObj);
			}
			
			flag = false;
		}
				
	}
	
	public boolean isSameAbstraction(AbstractedPolicy other) {
		return this.originalPolicies.equals(other.originalPolicies);
	}
	
	public AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy) {
		if (!isSameAbstraction(otherPolicy))
			throw new IllegalArgumentException("Not the same level of abstraction.");

		AbstractedPolicy merged = new AbstractedPolicy();
		merged.originalPolicies.addAll(this.originalPolicies);
		
		for (Entry<StateHashTuple, GroundedAction> e : abstractedPolicy.entrySet()) {
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
	 * @param abstractedPolicies - this list is modified as part of the recursion (becomes 1 element after all recursion is done).
	 * @return
	 */
	public static AbstractedPolicy merge(List<AbstractedPolicy> abstractedPolicies) {
		if (abstractedPolicies == null || abstractedPolicies.isEmpty())
			throw new IllegalArgumentException("Cannot pass a null or empty list of abstracted policies to merge.");
		
		ArrayList<AbstractedPolicy> newPolicies = new ArrayList<AbstractedPolicy>();
		newPolicies.addAll(abstractedPolicies);
		AbstractedPolicy merged = new AbstractedPolicy(newPolicies.get(0));
		
		for (int i = 1; i < newPolicies.size(); i++) {
			merged = merged.mergeWith(newPolicies.get(i));
		}
		
		return merged;
	}

	/**
	 * Generates the powerset to a certain depth, excluding the empty set
	 * @param list
	 * @param depth
	 * @return
	 */
	private static <T> List<List<T>> powerset(Collection<T> list, int depth) {
		if (depth < 1 || depth > list.size()) {
			throw new IllegalArgumentException("Need a depth >= 1 and <= " + list.size());
		}
		List<List<T>> ps = new ArrayList<List<T>>();
		ps.add(new ArrayList<T>());   // add the empty set
		
		// for every item in the original list
		for (T item : list) {
			List<List<T>> newPs = new ArrayList<List<T>>();
			
			for (List<T> subset : ps) {
				// copy all of the current powerset's subsets
				newPs.add(subset);
				
				// plus the subsets appended with the current item
				List<T> newSubset = new ArrayList<T>(subset);
				newSubset.add(item);
				if (newSubset.size() <= depth) {
					newPs.add(newSubset);
				}
			}
 
			// powerset is now powerset of list.subList(0, list.indexOf(item)+1)
			ps = newPs;
		}
	  
		ps.remove(0);
		return ps;
	}

	/**
	 * Abstraction happens on a per-group basis of policies to merge.
	 * @param policies
	 * @param depth
	 * @return
	 */
	public static List<AbstractedPolicy> unionMerge(List<PolicyBlockPolicy> policies, int depth) {
		ArrayList<AbstractedPolicy> mergedPolicies = new ArrayList<AbstractedPolicy>();
		
		for (List<PolicyBlockPolicy> ps: powerset(policies, depth)) {
			mergedPolicies.add(merge(abstractAll(ps)));
		}
		
		return mergedPolicies;
	}
	
	/**
	 * Removes attribute from an ObjectInstance
	 * @param d
	 * @param toRemove
	 * @param o
	 * @return New ObjectInstance with same properties, minus the specified attribute.
	 */
	public static ObjectInstance removeAttribute(Domain d,Attribute toRemove,ObjectInstance o) {
		if (o.getObjectClass().hasAttribute(toRemove)) {
	
			ObjectClass newObjectClass = new ObjectClass(d,o.getObjectClass().name);
			
			for (Attribute aa: o.getObjectClass().attributeList) {
				if (!toRemove.equals(aa)) {
					newObjectClass.addAttribute(aa);
				}
			}

			ObjectInstance newOI = new ObjectInstance(newObjectClass,o.getName());
			
			for (Attribute a : o.getObjectClass().attributeList) {
				if (!toRemove.equals(a)) {
					newOI.setValue(a.name,o.getStringValForAttribute(a.name));
				}
			}
			
			return newOI;
		} else {	
			return null;
		}
	}
	
	// TODO Implement an abstracting method that takes grounded policies and abstracts
	// them to the most abstract level with respect to all grounded policies.
	/**
	 * Right now, this method assumes that all of the policies are in the same domain and makes a simple copy of the policy
	 * @param policies
	 * @return
	 */
	public static ArrayList<AbstractedPolicy> abstractAll(List<PolicyBlockPolicy> policies) {
		ArrayList<AbstractedPolicy> abstractedPolicies = new ArrayList<AbstractedPolicy>();
		
		for (PolicyBlockPolicy p: policies) {
			AbstractedPolicy newP = new AbstractedPolicy();
			newP.originalPolicies.addAll(policies);
			newP.abstractedPolicy.putAll(p.policy);
			abstractedPolicies.add(newP);
		}
		
		return abstractedPolicies;
	}
	
	@Override
	/**
	 * TODO make this method use string builder
	 */
	public String toString() {
		String out = "";
		for (Entry<StateHashTuple, GroundedAction> e: abstractedPolicy.entrySet()) {
			out += e.getKey().hashCode() + ": " + e.getValue() + "\n";
		}
		
		return out;
	}
}