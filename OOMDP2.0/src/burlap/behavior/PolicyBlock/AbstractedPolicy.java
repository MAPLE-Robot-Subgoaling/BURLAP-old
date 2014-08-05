package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import domain.AbstractDomain.AbstractDomain;
import domain.fourroomsdomain.FourRooms;
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
		/*TaxiWorldDomain.MAXPASS = 1;
		
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
*/
		FourRooms fr = new FourRooms();
		Domain d = fr.generateDomain();

		AbstractDomain ad1 = new AbstractDomain();
		Domain finalDomain2 = ad1.generateDomain(d);
		
		State ss1 = FourRooms.getCleanState();
		State ss2 = FourRooms.getCleanState();

		ObjectInstance agent1 = ss1.getObjectsOfTrueClass("agent").get(0);
		ObjectInstance agent2 = ss2.getObjectsOfTrueClass("agent").get(0);

		ObjectInstance goal1 = ss1.getObjectsOfTrueClass("goal").get(0);
		ObjectInstance goal2 = ss2.getObjectsOfTrueClass("goal").get(0);
		
		agent1.setValue("x", 1);
		agent1.setValue("y", 1);
		
		agent2.setValue("x", 1);
		agent2.setValue("y", 1);
		
		goal1.setValue("x", 11);
		goal1.setValue("y",11);
		
		goal2.setValue("x", 11);
		goal2.setValue("y", 11);
		
		ObjectInstance block1 = new ObjectInstance(finalDomain2.getObjectClass("random"),"random"+0);
		block1.setValue("x", 5);
		block1.setValue("y", 2);
		
		ObjectInstance block2 = new ObjectInstance(finalDomain2.getObjectClass("random"),"random"+1);
		block2.setValue("x", 7);
		block2.setValue("y", 4);
		
		ObjectInstance block3 = new ObjectInstance(finalDomain2.getObjectClass("random"),"random"+2);
		block3.setValue("x", 3);
		block3.setValue("y", 3);

		ss2.addObject(block1);
		ss2.addObject(block2);
		
		ss1.addObject(block3);
		ss1.addObject(block1);
		ss1.addObject(block2);
		
		PolicyBlockPolicy p = new PolicyBlockPolicy((QLearning)FourRooms.Q,0);
		((QLearning)FourRooms.Q).setLearningPolicy(p);
        EpisodeAnalysis ea = new EpisodeAnalysis();
		ea = FourRooms.Q.runLearningEpisodeFrom(ss2);
		
		abstractPolicy(ea.actionSequence,FourRooms.Q,ss1,ss2);
	}
	
	public AbstractedPolicy() {
		abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
		originalPolicies = new ArrayList<PolicyBlockPolicy>();
	}
	
	public AbstractedPolicy(AbstractedPolicy p) {
		this();
		this.abstractedPolicy.putAll(p.abstractedPolicy);
		this.originalPolicies.addAll(p.originalPolicies);
	}

	public static PolicyBlockPolicy abstractPolicy(List<GroundedAction> actionSequence,LearningAgent la,State s1,State s2) {
		Map<String,Integer> lciMap = leastCommonIntersectionState(s1,s2);
		 
		State s = findLimitingState(generateLCIMappingState(s1),generateLCIMappingState(s2),s1,s2);
		 
		ArrayList<ArrayList<ObjectInstance>> listFromMapping =  getListFromMapping(lciMap,s);

		ArrayList<ArrayList<ObjectInstance>> listOfCombinations = findCombinations(listFromMapping,0,new ArrayList<ObjectInstance>(), new ArrayList<ArrayList<ObjectInstance>>());
		
		return score(actionSequence,la,listOfCombinations);
	}
	
	/**
	 * Scores all possible abstractions and finds the abstraction with the lowest error
	 * @param original
	 * @param la
	 * @param objList
	 * @return
	 */
	public static PolicyBlockPolicy score(List<GroundedAction> original,LearningAgent la,ArrayList<ArrayList<ObjectInstance>> objList) {
		PolicyBlockPolicy newPolicy = null;
		PolicyBlockPolicy toReturn = newPolicy;
		double error = 0;
		double oldError = 0;
		Boolean flag = true;
		
		for (ArrayList<ObjectInstance> ol : objList) {
			State st = new State();

			for (ObjectInstance o : ol) {
				st.addObject(o);
			}
			
			newPolicy = new PolicyBlockPolicy((QLearning)la,0);
			((QLearning)la).setLearningPolicy(newPolicy);
	        EpisodeAnalysis ea = new EpisodeAnalysis();
			ea = la.runLearningEpisodeFrom(st);
			
			error = findDifference(original,ea.actionSequence);

			if (error < oldError) {
				toReturn = newPolicy;
				oldError = error;
				flag = false;
			}

		}
		
		if (flag) {
			toReturn = newPolicy;
		}
		
		return toReturn;
	}
	
	/**
	 * Finds quantitative difference between two ArrayLists
	 * Used for calculating error
	 * @param actionSequence
	 * @param actionSequence2
	 * @return
	 */
	public static double findDifference(List<GroundedAction> actionSequence,List<GroundedAction> actionSequence2) {
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
		
		return error/actionSequence.size();
	}
	
	/**
	 * Makes state given a list of ObjectInstances
	 * @param objList
	 * @return
	 */
	public static State makeStateWith(ArrayList<ArrayList<ObjectInstance>> objList) {
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
	 * @param unordered
	 * @return
	 */
	public static ArrayList<ObjectInstance> orderOI (ArrayList<ObjectInstance> unordered) {
		ArrayList<ObjectInstance> ordered = new ArrayList<ObjectInstance>(unordered.size());
		List<String> nameList = new ArrayList<String>();
		
		for (ObjectInstance o : unordered) {
			nameList.add(o.getName());
		}
		
		Collections.sort(nameList);
		
		for (String s : nameList) {
			for (ObjectInstance obj : unordered) {
				if (s.equals(obj.getName())) {
					ordered.add(obj);
				}
			}
		}
		
		return ordered;
	}
	
	/**
	 * Finds all combinations of multidimensional ArrayList
	 * Used for generating all possibilities of objects to drop
	 * @param objList
	 * @param index
	 * @param output
	 * @param finalOutput
	 * @return
	 */
	public static ArrayList<ArrayList<ObjectInstance>> findCombinations(ArrayList<ArrayList<ObjectInstance>> objList, int index, ArrayList<ObjectInstance> output,ArrayList<ArrayList<ObjectInstance>> finalOutput){
		if(index == objList.size()){		
			ArrayList<ObjectInstance> orderedOutput = new ArrayList<ObjectInstance>(orderOI(output));
			
			if (!finalOutput.contains(orderedOutput) && orderedOutput.size() == objList.size()) {
				finalOutput.add(orderedOutput);
			}

		}
	    else{
	        for(int i=0 ; i<objList.get(index).size(); i++){
	        	if (!output.contains(objList.get(index).get(i))) {
	        		output.add(objList.get(index).get(i));
	        	}
	        	
	            findCombinations(objList,index+1,output,finalOutput);
	            //output.remove(output.size() - 1);  
	        }
	    }
		
		return finalOutput;
	}
	
	/**
	 * Finds list that corresponds to the mapping provided. If there are more than one of a type of object,
	 * duplicates that list as many times as there are that object.
	 * @param map
	 * @param s
	 * @return
	 */
	public static ArrayList<ArrayList<ObjectInstance>> getListFromMapping(Map<String,Integer> map,State s) {
		ArrayList<ArrayList<ObjectInstance>> objList = new ArrayList<ArrayList<ObjectInstance>>();
		ArrayList<ArrayList<ObjectInstance>> toAdd = new ArrayList<ArrayList<ObjectInstance>>();
		int i = 1;
		
		for (Map.Entry<String,Integer> entry : map.entrySet()) {
			objList.add(getListOfObject(s,entry.getKey()));
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
	 * @param s
	 * @param name
	 * @return
	 */
	public static ArrayList<ObjectInstance> getListOfObject(State s,String name) {
		
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
	 * @param s - State to be mapped
	 * @return  Map that includes the names of the objects and their frequency
	 */
	public static Map<String,Integer> generateLCIMappingState(State s) {
		Map<String,Integer> domainCount = new HashMap<String,Integer>();
		
		for(ObjectInstance o : s.getAllObjects()) {
			if (domainCount.containsKey(o.getObjectClass().name)) {
				domainCount.replace(o.getObjectClass().name, domainCount.get(o.getObjectClass().name)+1);
			} else {
				domainCount.put(o.getObjectClass().name,1);
			}	
		}
		
		return domainCount;
	}
	
	/**
	 * Generates the Least Common Intersection of two states.
	 * @param a
	 * @param b
	 * @return Map of ObjectClass name and frequency
	 */
	public static Map<String,Integer> leastCommonIntersectionState(State a,State b){
		
		Map<String,Integer> domain1Count = generateLCIMappingState(a);
		Map<String,Integer> domain2Count = generateLCIMappingState(b);
		ArrayList<String> toRemove = new ArrayList<String>();
		
		for (Map.Entry<String,Integer> entry : domain1Count.entrySet()) {
			if (domain2Count.containsKey(entry.getKey())) {
				if (entry.getValue() < domain2Count.get(entry.getKey())) {
					domain2Count.replace(entry.getKey(),domain2Count.get(entry.getKey()));
					//System.out.println(domain2Count.get(entry.getKey()) + " " + entry.getKey());
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
	 * Finds the state that refers to the least common intersection of 2 states.
	 * @param d1 Map of state in (ObjectClass name, frequency) form.
	 * @param d2
	 * @param s1
	 * @param s2
	 * @return State that limits the intersection
	 */
	public static State findLimitingState(Map<String,Integer> d1,Map<String,Integer> d2,State s1,State s2) {
		
		for (Map.Entry<String, Integer> entry1 : d1.entrySet()) {
			if (!d2.containsKey(entry1.getKey()) || d2.containsKey(entry1.getKey()) && d2.get(entry1.getKey()) < entry1.getValue()) {
				return s2;
			} 
		}
		return s1;
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