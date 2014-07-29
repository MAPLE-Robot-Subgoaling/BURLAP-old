package burlap.behavior.PolicyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.PolicyBlock.PolicyBlockDomain;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.GroundedAction;

/* THIS FILE HAD DEPENDENCIES ON AN OLD IMPLEMENTATION OF PolicyBlockPolicy.
 * Since PolicyBlockPolicy has be updated, the code in this file will not
 * work correctly.  It is currently maintained for possible use later and to
 * prevent any errors from old files that still use it.
 */

public class PolicyGenerator {

	//declaration of PolicyBlocks
	static PolicyBlockDomain environ;
	HashMap<List<State>, Policy> policies;
	String outputPath;
	HashMap<Collection<StateHashTuple>, Policy> initailSpace;
	HashMap<Collection<StateHashTuple>, PolicyBlockPolicy> mergedSpace;
	List<Policy> merged;
	
	public static void main(String args[]){
		
		PolicyGenerator generator = new PolicyGenerator("PolicyBlocks/");
		generator.generatePolicies("GW-", 4);			//generates 3 policies
		
		for(List<State> s: generator.policies.keySet()){
			System.out.println(s.get(0)+"************");
		}
		
		//generator.runMerge();							//strips the info needed, and calls merge()
		//generator.runMerge2();		//strips all 3 policies and performs a UnionMerge
		//generator.writePolicies();						//writes the values to a file
		generator.visualizePolicies();					//outputs the policies onto the screen
		
	}
	
	//creates a new policy block domain object
	public PolicyGenerator(String outputPath){
		environ = new PolicyBlockDomain();
		policies = new HashMap<List<State>, Policy>();
		initailSpace = environ.getHashPolicyMap();
		mergedSpace = new HashMap<Collection<StateHashTuple>, PolicyBlockPolicy>();
		merged = new ArrayList<Policy>();
		this.outputPath = outputPath;
	}

	
	//generates a hashmap of different policies used via Value Iteration Planner 
	public void generatePolicies(String name, int number){
		environ.computePolicy(name, number, outputPath);
		policies = environ.getPolicyMap();
	}
	
	//prints the policies
	public void printPolicies(){
		for(Policy p: policies.values()){
			System.out.println(p.toString());
		}
	}
	
	//prints the states
	public void printStates(){
		for(List<State> s: policies.keySet()){
			System.out.println("\t" + s.hashCode());
		}
	}
	
	public void runMerge2(){
		Iterator<?> it = initailSpace.entrySet().iterator();
		ArrayList initial = new ArrayList();
		
		while(it.hasNext()) //moves all policies into the "initial" ArrayList
		{
			Map.Entry<List<StateHashTuple>, Policy> pairs = (Map.Entry<List<StateHashTuple>, Policy>)it.next();
			List<Object> temp = new ArrayList<Object>();
			temp.add(pairs.getKey());
			temp.add(pairs.getValue());
			initial.add(temp); //policies are in the form of a List (first Object is the StateSequence, second is the Policy)
		}
		
		List finalPolicy = score(unionMerge(3, initial));
		
		
	}
	/*
	 * calls Merge()
	 * Strips out the policy object and associated state collection from it
	 * and passes it to merge.
	 * 
	 * This whole function needs to be restructured so that we are able to 
	 * generate multiple merges (similar to Trajectory Generator's unionSet)
	 */
	public void runMerge(){
		Iterator<?> it = initailSpace.entrySet().iterator();
		int limit = 0; //to stop pulling more than 2 policies
		
		List<StateHashTuple> stateSeq_A = null;
		Policy policy_A = null;
		
		List<StateHashTuple> stateSeq_B = null;
		Policy policy_B = null;
		
		
		//merges two regular policies (greedyQ)
		while(it.hasNext()){
			
			@SuppressWarnings("unchecked")
			Map.Entry<List<StateHashTuple>, Policy> pairs = (Map.Entry<List<StateHashTuple>, Policy>)it.next();
			
			if(limit == 1){
				
				//grabs the second policy and state sequence
				stateSeq_B = pairs.getKey();
				policy_B = pairs.getValue();
			}
			
			//grabs the first policy and state sequence
			stateSeq_A = pairs.getKey();
			policy_A = pairs.getValue();
			
			it.remove(); //avoid concurrent modification error
			limit++;
		}
		
		if(stateSeq_A == null || stateSeq_B == null || policy_A == null || policy_B == null) //quick check
			System.out.println("Did not assign Value Correctly");
		else{
			this.merge(stateSeq_A, stateSeq_B, policy_A, policy_B);
			System.out.println("GreedyQ - Merged items: " + mergedSpace.size());
		}
		
		//attempt to merge GreedyQ - the second one - with a PolicyBlockPolicy
		Iterator<?> temp = mergedSpace.entrySet().iterator();
		limit = 0;
		
		do{
			@SuppressWarnings("unchecked")
			Map.Entry<List<StateHashTuple>, Policy> pairs = (Map.Entry<List<StateHashTuple>, Policy>)temp.next();
			stateSeq_A = pairs.getKey();
			policy_A = pairs.getValue();
			//it.remove();
			
		}while(false); //stop after one iteration
		
		if(stateSeq_A == null || stateSeq_B == null || policy_A == null || policy_B == null) //quick check
			System.out.println("Did not assign Value Correctly");
		else{
			
			/*
			 * This is just to test merging between two different kinds of policies. It works, and we are able to merge,
			 * we just need to be able to merge successively. 
			 * 
			 * We need to re-write the loop to continously merge until we get a set of merged policies.
			 */
			
			this.merge(stateSeq_A, stateSeq_B, policy_A, policy_B);
			System.out.println("Hybrid - Merged items: " + mergedSpace.size());
		}
		
	}
	
	public List score(ArrayList comb)
	{
		ArrayList rungCand = new ArrayList(); //List of the largest policy on each "rung" (that is: largest policy resulting from a pair-merging, largest from a triplet-merging, and so forth)
		
		for(int i = 0; i < comb.size(); i++) //loops through each "rung"
		{
			int maxScore = ((List<Object>)((List<Object>)((ArrayList)(comb.get(i))).get(0)).get(0)).size(); //sets the initial maximum score to the first policy
			int maxInd = 0; //sets the initial maximum score owner's index to 0
			for(int k = 1; k < ((ArrayList)(comb.get(i))).size(); k++) //loops through other policies on "rung" and updates score and index along the way
			{
				int tempScore = ((List<Object>)((List<Object>)((ArrayList)(comb.get(i))).get(k)).get(0)).size();
				if(tempScore > maxScore)
				{
					maxScore = tempScore;
					maxInd = k;
				}
			}
			
			rungCand.add((((List<Object>)((ArrayList)(comb.get(i))).get(maxInd)))); //copies largest policy to "rungCand"
		}
		
		int maxScore = ((List<Object>)((List<Object>) rungCand.get(0)).get(0)).size() * 2; //sets the initial maximum score to the first policy
		int maxInd = 0; //sets the initial maximum score owner's index to 0
		for(int i = 1; i < rungCand.size(); i++) //loops through each candidate and finds highest score (accounting for how many policies were merged to make the candidate)
		{
			int tempScore = ((List<Object>)((List<Object>) rungCand.get(i)).get(0)).size() * (i+2);
			if(tempScore > maxScore)
			{
				maxScore = tempScore;
				maxInd = i;
			}
		}
		
		List output = ((List<Object>)rungCand.get(maxInd)); //returns highest-scoring policy
		return output;
	}
	public ArrayList unionMerge(int max, ArrayList initial)
	{
		//ArrayList hiSc = new ArrayList(); //list of highest scores
		ArrayList comb = new ArrayList(); //list of lists of combined policies
		
		for(int i = 0; i < max-1; i++) //adds list to comb to store all combined policies
		{
			ArrayList temp = new ArrayList();
			comb.add(temp);
		}
		
		for(int pol1 = 0; pol1 < initial.size(); pol1++) //loops through each policy
		{
			for(int pol2 = pol1+1; pol2 < initial.size(); pol2++) //merges policy with another to produce all pairings
			{
				((List<Object>) comb.get(0)).add(merge((List<Object>)initial.get(pol1), (List<Object>)initial.get(pol2)));
			}
		}
		
		for(int a = 0; a < max-2; a++) //loops to merge through the maximum depth
		{
			int temp =  ((ArrayList)comb.get(a)).size();
			for(int b = 0; b < temp; b++) //loops through each policy in the current depth
			{
				 for(int c = 0; c < initial.size(); c++) //merges each initial policy with each policy in this depth
				 {
					 ((List<Object>) comb.get(a + 1)).add(merge((List<Object>)((ArrayList)comb.get(a)).get(b), (List<Object>)initial.get(c))); //saves policy to proper array list
				 }
			}
		}
		return comb;
	}
	
	/*
	 * attempts to merge the two states
	 * checks to see with state sequence is shorter, cycles through state by state.
	 * The algorithm may need to be altered/fixed
	 */
	public void merge(List<StateHashTuple> stateSeqA, List<StateHashTuple> stateSeqB, Policy policyA, Policy policyB){
		Map<StateHashTuple, GroundedAction> intersection = new HashMap<StateHashTuple, GroundedAction>();
		List<StateHashTuple> stateList = new ArrayList<StateHashTuple>();
		
		System.out.println("\t\tPerforming Merging!!\n");
		
		if(stateSeqA.size() <= stateSeqB.size()){

			for(int i = 0; i < stateSeqA.size(); i++){
				StateHashTuple s = stateSeqA.get(i);
				for(int j = 0; j < stateSeqB.size(); j++){
					StateHashTuple p = stateSeqB.get(j);
					
					if(s.equals(p) && (policyA.getAction(s.s).equals(policyB.getAction(p.s)))){
						if(s.s.hashCode() != p.s.hashCode()){
							System.out.println("S:"+s.s.hashCode() + "::P:" +p.s.hashCode());
							System.out.println("S-A:" + policyA.getAction(s.s) + "::P-A:" + policyB.getAction(p.s));
						}
						//intersection.put(stateSeqA.get(i),policyA.getAction(s.s));
						stateList.add(s);
						
						
					}		
				}
			}
			
		}else{
			
			for(int i = 0; i < stateSeqB.size(); i++){
				StateHashTuple s = stateSeqB.get(i);
				for(int j = 0; j < stateSeqA.size(); j++){
					StateHashTuple p = stateSeqA.get(j);
					
					if(s.equals(p) && (policyA.getAction(p.s).equals(policyB.getAction(s.s)))){
						
						if(s.s.hashCode() != p.s.hashCode()){
							System.out.println("S:"+s.s.hashCode() + "::P:" +p.s.hashCode());
							System.out.println("S-A:" + policyA.getAction(s.s) + "::P-A:" + policyB.getAction(p.s));
						}
						//intersection.put(stateSeqB.get(i),policyB.getAction(s.s));
						stateList.add(s);
						
						
					}		
				}
			}
		}
		
		//result of the merging objects
		PolicyBlockPolicy result = new PolicyBlockPolicy(.99);
		//result.setStateSpace((HashMap<StateHashTuple, GroundedAction>)intersection);
		
		
		/*
		 *So what happens is that Java doesn't allow for duplicate keys. So if the keys turn out to have the same
		 *stateSpace but different associated actions, Java doesn't care, it get's overwritten. That's why for now
		 *only one merged policy shows up. 
		 */
		mergedSpace.put(stateList, result);
		
	}
	
	//basically the same 'merge' method as above, but the parameters and what it returns are compatible
	public List<Object> merge(List<Object> List1, List<Object> List2){
		List<StateHashTuple> stateSeqA = (List<StateHashTuple>)List1.get(0);
		List<StateHashTuple> stateSeqB = (List<StateHashTuple>)List2.get(0);
		Policy policyA = (Policy)List1.get(1);
		Policy policyB = (Policy)List2.get(1);
		Map<StateHashTuple, GroundedAction> intersection = new HashMap<StateHashTuple, GroundedAction>();
		List<StateHashTuple> stateList = new ArrayList<StateHashTuple>();
		List<Object> output = new ArrayList<Object>();

		System.out.println("\n\tInitial State Space: " + stateSeqA.size() + ":" + stateSeqB.size() + "\n");
		
		if(stateSeqA.size() <= stateSeqB.size()){

			for(int i = 0; i < stateSeqA.size(); i++){
				StateHashTuple s = stateSeqA.get(i);
				for(int j = 0; j < stateSeqB.size(); j++){
					StateHashTuple p = stateSeqB.get(j);
					
					if(s.equals(p) && (policyA.getAction(s.s).equals(policyB.getAction(p.s)))){
						
						//System.out.println("\t::"+ s.s.hashCode() + ":" + p.s.hashCode());

						if(s.s.hashCode() != p.s.hashCode()){
							System.out.println("S:"+s.s.hashCode() + "::P:" +p.s.hashCode());
							System.out.println("S-A:" + policyA.getAction(s.s) + "::P-A:" + policyB.getAction(p.s));
						}
						
						//intersection.put(stateSeqA.get(i),policyA.getAction(s.s));
						stateList.add(s);
						
						
					}		
				}
			}
			
		}else{
			
			for(int i = 0; i < stateSeqB.size(); i++){
				StateHashTuple s = stateSeqB.get(i);
				for(int j = 0; j < stateSeqA.size(); j++){
					StateHashTuple p = stateSeqA.get(j);
					
					if(s.equals(p) && (policyA.getAction(p.s).equals(policyB.getAction(s.s)))){
						
						//System.out.println("\t::"+ s.s.hashCode() + ":" + p.s.hashCode())
						if(s.s.hashCode() != p.s.hashCode()){
							System.out.println("S:"+s.s.hashCode() + "::P:" +p.s.hashCode());
							System.out.println("S-A:" + policyA.getAction(s.s) + "::P-A:" + policyB.getAction(p.s));
						}
						//intersection.put(stateSeqB.get(i),policyB.getAction(s.s));
						stateList.add(s);
						
						
					}		
				}
			}
		}
		
		//result of the merging objects
		PolicyBlockPolicy result = new PolicyBlockPolicy(.99);
		//result.setStateSpace((HashMap<StateHashTuple, GroundedAction>)intersection);
	
		
		//System.out.println("\tFinal-StateSpace: " + result.stateSpace.size());
		
		output.add(stateList);
		output.add(result);
		
		/*
		 *So what happens is that Java doesn't allow for duplicate keys. So if the keys turn out to have the same
		 *stateSpace but different associated actions, Java doesn't care, it get's overwritten. That's why for now
		 *only one merged policy shows up. 
		 */
		//mergedSpace.put(stateList, result);
		return output;
		
	}
	
	public void writePolicies(){
		int i = 0;
		
		for(PolicyBlockPolicy p: mergedSpace.values()){
			environ.writePolicy(p, outputPath + "merged-" + i);
			i++;
		}
	}
	
	//shows the policies generated by PolicyBlocksDomain
	public void visualizePolicies(){
		environ.visualize(outputPath);
	}
}
