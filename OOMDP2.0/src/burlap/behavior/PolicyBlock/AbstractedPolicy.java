package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.List;
import domain.fourroomsdomain.FourRooms;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class AbstractedPolicy {

	public PolicyBlockPolicy abstractedPolicy;
	public State newState;
	public List<ObjectInstance> droppedAttr;
	public List<ObjectInstance> droppedObj;
	
	public static void main(String args[]){	

        FourRooms fr = new FourRooms();        
        Domain d1 = fr.generateDomain();
        State s1 = FourRooms.getCleanState();
        
        FourRooms.setAgent(s1, 1, 1);
        FourRooms.setGoal(s1, 11, 11);
		
        //note: epsilon value of 0
        PolicyBlockPolicy newPolicy1 = new PolicyBlockPolicy((QLearning)FourRooms.Q,0);
        
        ((QLearning) FourRooms.Q).setLearningPolicy(newPolicy1);
        EpisodeAnalysis ea = new EpisodeAnalysis();

        for(int i=0;i < 2000;i++) {
			ea = FourRooms.Q.runLearningEpisodeFrom(s1);
		}
        
        System.out.println("Original state:");
        System.out.println(s1.getCompleteStateDescription());
        System.out.println("--------");
        AbstractedPolicy ap = new AbstractedPolicy(d1,s1,newPolicy1,FourRooms.Q,FourRooms.MAP);
        System.out.println("State after abstraction:");
        System.out.println(ap.newState.getCompleteStateDescription());
        
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
	public static ArrayList<AbstractedPolicy> abstractAll(ArrayList<PolicyBlockPolicy> groundedPolicies) {
		return new ArrayList<AbstractedPolicy>();
	}
	
	
}