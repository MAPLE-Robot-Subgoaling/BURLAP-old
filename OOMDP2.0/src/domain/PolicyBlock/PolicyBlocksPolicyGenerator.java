package domain.PolicyBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.GroundedAction;

public class PolicyBlocksPolicyGenerator {

	static PolicyBlockDomain environ;
	HashMap<List<State>, Policy> policies;
	String outputPath;
	HashMap<Collection<StateHashTuple>, Policy> stateSpace;
	List<Policy> merged;
	
	public static void main(String args[]){
		PolicyBlocksPolicyGenerator generator = new PolicyBlocksPolicyGenerator("outputPolicyBlocks");
		generator.generatePolicies("gridWorld-", 3);	//generates 3 policies
		generator.visualizePolicies();					//shows the generated policies
		generator.runMerge();							//strips the info needed, and calls merge()	
		
	}
	
	//creates a new policy block domain object
	public PolicyBlocksPolicyGenerator(String outputPath){
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
		Iterator<?> it = policies.entrySet().iterator();
		int limit = 0; //to stop pulling more than 2 policies
		
		List<State> stateSeq_A = null;
		Policy policy_A = null;
		
		List<State> stateSeq_B = null;
		Policy policy_B = null;
		
		while(it.hasNext()){
			
			@SuppressWarnings("unchecked")
			Map.Entry<List<State>, Policy> pairs = (Map.Entry<List<State>, Policy>)it.next();
			
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
		
		if(stateSeq_A == null && stateSeq_B == null && policy_A == null && policy_B == null) //quick check
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
	public void merge(List<State> stateSeqA, List<State> stateSeqB, Policy policyA, Policy policyB){
		Map<State, GroundedAction> intersection = new HashMap<State, GroundedAction>();
		
		
		if(stateSeqA.size() <= stateSeqB.size()){
			
			for(int i = 0; i < stateSeqA.size()-1; i++){
				State s = stateSeqA.get(i);
				for(int j = 0; j < stateSeqB.size()-1; j++){
					State p = stateSeqB.get(j);
					boolean common = true;
					
					/*
					 * gonna try something different here - in the next build
					 * more similar to the older implementation
					 */
					if(!(policyA.getAction(s).toString().equals(policyB.getAction(p).toString()))){
						common = false;
						break;
					}
					
					if(common)
						intersection.put(s,policyA.getAction(s));
				}
			}
			
		}else{
			
			for(int i = 0; i < stateSeqB.size()-1; i++){
				State s = stateSeqB.get(i);
				for(int j = 0; j < stateSeqA.size()-1; j++){
					State p = stateSeqA.get(j);
					boolean common = true;
					
					if(!(policyB.getAction(s).equals(policyA.getAction(p)))){
						common = false;
						break;
					}
					
					if(common)
						intersection.put(s,policyB.getAction(s));
				}
				
			}
			
		}
		
		Iterator<?> it = intersection.entrySet().iterator();
		
		while(it.hasNext()){
			
			//prints all the items associated with the hashmap
			//hashcode of the state + the GroundedAction attached to the state
			
			@SuppressWarnings("unchecked")
			Map.Entry<State, GroundedAction> pairs = (Map.Entry<State, GroundedAction>)it.next();
			System.out.println(pairs.getKey().hashCode() + "\t" + pairs.getValue().toString());
		}
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
		
		return null; //must return a policy object, in order to be called again.
	}
	
	//shows the policies generated by PolicyBlocksDomain
	public void visualizePolicies(){
		environ.visualize(outputPath);
	}
}
