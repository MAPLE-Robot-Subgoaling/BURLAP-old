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

public class PolicyGenerator {

	//declaration of PolicyBlocks
	static PolicyBlockDomain environ;
	HashMap<List<State>, Policy> policies;
	String outputPath;
	HashMap<Collection<StateHashTuple>, Policy> stateSpace;
	List<Policy> merged;
	
	public static void main(String args[]){
		PolicyGenerator generator = new PolicyGenerator("PolicyBlocks/");
		generator.generatePolicies("GW-", 3);			//generates 3 policies
		generator.runMerge();							//strips the info needed, and calls merge()	
	}
	
	//creates a new policy block domain object
	public PolicyGenerator(String outputPath){
		environ = new PolicyBlockDomain();
		policies = new HashMap<List<State>, Policy>();
		stateSpace = environ.getHashPolicyMap();
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
	
	/*
	 * calls Merge()
	 * Strips out the policy object and associated state collection from it
	 * and passes it to merge.
	 */
	public void runMerge(){
		Iterator<?> it = stateSpace.entrySet().iterator();
		int limit = 0; //to stop pulling more than 2 policies
		
		List<StateHashTuple> stateSeq_A = null;
		Policy policy_A = null;
		
		List<StateHashTuple> stateSeq_B = null;
		Policy policy_B = null;
		
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
		}
	}
	
	/*
	 * attempts to merge the two states
	 * checks to see with state sequence is shorter, cycles through state by state.
	 * The algorithm may need to be altered/fixed
	 */
	public void merge(List<StateHashTuple> stateSeqA, List<StateHashTuple> stateSeqB, Policy policyA, Policy policyB){
		Map<StateHashTuple, GroundedAction> intersection = new HashMap<StateHashTuple, GroundedAction>();
		
		StateHashTuple firstState;
		int count = 0;
		
		if(stateSeqA.size() <= stateSeqB.size()){
			firstState = stateSeqA.get(0);
			
			for(int i = 0; i < stateSeqA.size(); i++){
				StateHashTuple s = stateSeqA.get(i);
				for(int j = 0; j < stateSeqB.size(); j++){
					StateHashTuple p = stateSeqB.get(j);
					
					//Fixed Here
					if(s.equals(p) && (policyA.getAction(s.s).equals(policyB.getAction(p.s)))){
						intersection.put(stateSeqB.get(i),policyB.getAction(s.s));
						count++;
					}		
				}
			}
			
		}else{
			
			firstState = stateSeqB.get(0);
			
			for(int i = 0; i < stateSeqB.size(); i++){
				StateHashTuple s = stateSeqB.get(i);
				for(int j = 0; j < stateSeqA.size(); j++){
					StateHashTuple p = stateSeqA.get(j);
					
					//Fixed
					if(s.equals(p) && (policyA.getAction(p.s).equals(policyB.getAction(s.s)))){
						intersection.put(stateSeqB.get(i),policyB.getAction(s.s));
						count++;
					}		
				}
			}
		}
		
		//result of the merging objects
		PolicyBlockPolicy result = new PolicyBlockPolicy((HashMap<StateHashTuple, GroundedAction>)intersection);
		
		System.out.println("Total Succesful Merges: " + count);
		
		environ.showPolicy(firstState, result, this.outputPath, 3);
	}
	
	//James' Algorithm (currently unchecked, not functional)
	public Policy intersection(Collection<StateHashTuple> hashStateSpace){
		Map<StateHashTuple, GroundedAction> intersection = new HashMap<StateHashTuple, GroundedAction>();
		
		for(StateHashTuple sh: hashStateSpace){
			boolean common = true;
			GroundedAction last = null;
			
			for(Policy p:policies.values()){
				if(last == null)
					last = p.getAction(sh.s);
				else{
					if(!(last.equals(p.getAction(sh.s)))){
						common = false;
						break;
					}
				}
			}
			
			if(common)
				intersection.put(sh, last);	
		}
		
		
		PolicyBlockPolicy result = new PolicyBlockPolicy((HashMap<StateHashTuple, GroundedAction>)intersection);
		environ.showPolicy(null, result, this.outputPath, 3);
		return result;				//must return a policy object, in order to be called again.
	}
	
	//shows the policies generated by PolicyBlocksDomain
	public void visualizePolicies(){
		environ.visualize(outputPath);
	}
}
