package domain.PolicyBlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.GroundedAction;

public class PolicyBlocksPolicyGenerator {

	static PolicyBlockDomain environ;
	HashMap<String, Policy> policies;
	String outputPath;
	Collection<StateHashTuple> stateSpace;
	
	public static void main(String args[]){
		PolicyBlocksPolicyGenerator generator = new PolicyBlocksPolicyGenerator("outputPolicyBlocks");
		generator.generatePolicies("gridWorld-", 3);
		generator.printPolicies();
		generator.visualizePolicies();
		
	}
	
	//creates a new policy block domain object
	public PolicyBlocksPolicyGenerator(String outputPath){
		environ = new PolicyBlockDomain();
		policies = new HashMap<String, Policy>();
		this.outputPath = outputPath;
	}
	
	//generates a hashmap of different policies used via Value Iteration Planner 
	public void generatePolicies(String name, int number){
		environ.computePolicy(name, number, outputPath);
		policies = environ.getPolicyMap();
		
	}
	
	public void printPolicies(){
		for(Policy p: policies.values()){
			System.out.println(p.toString());
		}
	}
	
	public Policy intersection(){
		Map<StateHashTuple, GroundedAction> intersection = new HashMap<StateHashTuple, GroundedAction>();
		
		for(StateHashTuple sh: stateSpace){
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
		
		return null;
	}
	
	public void merge(Policy a, Policy b){
		
	}
	
	
	public void visualizePolicies(){
		environ.visualize(outputPath);
	}
}
