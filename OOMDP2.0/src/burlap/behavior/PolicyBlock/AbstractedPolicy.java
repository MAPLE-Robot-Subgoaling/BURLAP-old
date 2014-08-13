package burlap.behavior.PolicyBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import domain.AbstractDomain.AbstractDomain;
import domain.fourroomsdomain.FourRooms;
import domain.taxiworld.TaxiWorldDomain;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.behavior.singleagent.options.Option;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedPolicy {
	private List<PolicyBlockPolicy> originalPolicies;
	public Map<StateHashTuple, GroundedAction> abstractedPolicy;
	public Map<StateHashTuple, List<QValue>> absPol;
	public State newState;
	public List<ObjectInstance> droppedAttr;
	public List<ObjectInstance> droppedObj;
	
	public static long[] runTaxiLearning(PolicyBlockPolicy policy, StateHashFactory hf, int[][] passPos, int episodes, String filepath) throws IOException {
		return runTaxiLearning(policy, hf,  new ArrayList<Option>(), passPos, episodes, filepath);
	}
	
	public static long[] runTaxiLearning(PolicyBlockPolicy policy, StateHashFactory hf, Option o, int[][] passPos, int episodes, String filepath) throws IOException {
		List<Option> os = new ArrayList<Option>();
		os.add(o);
		return runTaxiLearning(policy, hf, os, passPos, episodes, filepath);
	}
	
	public static long[] runTaxiLearning(PolicyBlockPolicy policy, StateHashFactory hf, List<Option> os, int[][] passPos, int episodes, String filepath) throws IOException {
		TaxiWorldDomain.MAXPASS = passPos.length;
		QLearning Q = new QLearning(TaxiWorldDomain.DOMAIN, TaxiWorldDomain.rf, TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf, TaxiWorldDomain.GAMMA, TaxiWorldDomain.LEARNINGRATE, Integer.MAX_VALUE);
		//TaxiWorldDomain td = new TaxiWorldDomain();
		//td.generateDomain();
		State s = TaxiWorldDomain.getCleanState();
        Q.setLearningPolicy(policy);
        policy.setPlanner(Q);
        
        for (Option o: os) {
			Q.addNonDomainReferencedAction(o);
		}
        
        File f = new File(filepath);
        BufferedWriter b = new BufferedWriter(new FileWriter(f));
        b.write("Episode,Steps\n");
        long[] cumulArr = new long[episodes];
        long cumul = 0;
        
        for (int i = 0; i < episodes; i++) {
            TaxiWorldDomain.setAgent(s, 4, 5);
            TaxiWorldDomain.setGoal(s, 3, 5);
            
            for (int j = 1; j <= TaxiWorldDomain.MAXPASS; j++) {
            	TaxiWorldDomain.setPassenger(s, j, passPos[j - 1][0], passPos[j - 1][1]);
            }

            EpisodeAnalysis analyzer = new EpisodeAnalysis();
            analyzer = Q.runLearningEpisodeFrom(s);
            cumul += analyzer.numTimeSteps();
            b.write((i+1) + "," + cumul + "\n");
            cumulArr[i] = cumul;
        }
        b.close();
        
        return cumulArr;
	}
	
	public static PolicyBlockOption generateRandomOption(StateHashFactory hf, List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
		Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
		Random rand = new Random();
		int count = 0;
		int max = rand.nextInt((stateSpace.size()));
		
		// Samples from the state space
		for (StateHashTuple s: stateSpace) {
			if (count >= max) {
				break;
			}
			policy.put(s, new GroundedAction(actionSpace.get(rand.nextInt(actionSpace.size())), ""));
			count++;
		}
		
		System.out.println(count);
		System.out.println(stateSpace.size());
		return new PolicyBlockOption(hf, actionSpace, policy);
	}
	
	/**
	 * This method generates an option to have the agent take a cyclic path in the TaxiWorldDomain.
	 * Acts as a sanity check to make sure that options wont cycle forever
	 * @param hf
	 * @param actionSpace
	 * @param stateSpace
	 * @return an option representing a cyclic path
	 */
	public static PolicyBlockOption generateCyclicOption(StateHashFactory hf, List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
		Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
		Action n = null;
		Action s = null;
		Action e = null;
		Action w = null;
		for (Action a: actionSpace) {
			if (a.getName().equals(TaxiWorldDomain.ACTIONNORTH)) {
				n = a;
			} else if (a.getName().equals(TaxiWorldDomain.ACTIONSOUTH)) {
				s = a;
			} else if (a.getName().equals(TaxiWorldDomain.ACTIONEAST)) {
				e = a;
			} else if (a.getName().equals(TaxiWorldDomain.ACTIONWEST)) {
				w = a;
			}
		}
		
		if (n == null || s == null || e == null || w == null) {
			throw new NullPointerException("Action space doesn't define the correct actions for this domain.");
		}
		// (1, 5) -> e; (2, 5) -> n; (2, 6) -> w; (1,6) -> s
		for (StateHashTuple st: stateSpace) {
			int x = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT).getDiscValForAttribute(TaxiWorldDomain.ATTX);
			int y = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT).getDiscValForAttribute(TaxiWorldDomain.ATTY);
			if (x == 1 && y == 5) {
				policy.put(st, new GroundedAction(e, ""));
			} else if (x == 2 && y == 5) {
				policy.put(st, new GroundedAction(n, ""));
			} else if (x == 2 && y == 6) {
				policy.put(st, new GroundedAction(w, ""));
			} else if (x == 1 && y == 6) {
				policy.put(st, new GroundedAction(s, ""));
			}
		}
		
		return new PolicyBlockOption(hf, actionSpace, policy);
	}
	
	public static void main(String args[]) throws IOException {
		/*
		String path = "C:/Users/Allison/Desktop/";
		TaxiWorldDomain.MAXPASS = 3;
		
		new TaxiWorldDomain().generateDomain();
        DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
        hf.setAttributesForClass(TaxiWorldDomain.CLASSAGENT,
        		TaxiWorldDomain.DOMAIN.getObjectClass(TaxiWorldDomain.CLASSAGENT).attributeList);
		double epsilon = 0.3;
		int episodes = 5000;
		long startTime = System.currentTimeMillis();
		
		// int[][] first = {{1, 2}, {3, 2}};
		// int[][] second = {{1, 4}, {5, 4}};
		// int[][] merged = {{13, 7}, {7, 7}};
		// List<Integer[]> open = TaxiWorldDomain.getOpenSpots();
		// for (Integer[] o: open) {
		// 	 System.out.println(o[0] + ": " + o[1]);
		// }
		int[][] first = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
		int[][] second = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
		int[][] merged = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
		System.out.print("First: { ");
		for (int i = 0; i < first.length; i++) {
			System.out.print("("+first[i][0]+", "+first[i][1]+") ");
		}
		System.out.println("}");
		System.out.print("Second: { ");
		for (int i = 0; i < second.length; i++) {
			System.out.print("("+second[i][0]+", "+second[i][1]+") ");
		}
		System.out.println("}");
		System.out.print("Merged: { ");
		for (int i = 0; i < merged.length; i++) {
			System.out.print("("+merged[i][0]+", "+merged[i][1]+") ");
		}
		System.out.println("}");
		
		System.out.println("Starting first source.");
		PolicyBlockPolicy newPolicy1 = new PolicyBlockPolicy(epsilon);
		System.out.println(runTaxiLearning(newPolicy1, hf, first, episodes, path + "one.csv")[episodes - 1]);
		System.out.println("Done with first source.\n");
		
		System.out.println("Starting second source.");
		PolicyBlockPolicy newPolicy2 = new PolicyBlockPolicy(epsilon);
		System.out.println(runTaxiLearning(newPolicy2, hf, second, episodes, path + "two.csv")[episodes - 1]);
		System.out.println("Done with second source.\n");
        
        ArrayList<PolicyBlockPolicy> pis = new ArrayList<PolicyBlockPolicy>();
        pis.add(newPolicy1);
        pis.add(newPolicy2);
        List<AbstractedPolicy> absPolicies = unionMerge(pis, pis.size());
		for (AbstractedPolicy ap: absPolicies) {
			System.out.println(ap.abstractedPolicy.size());
		}
		
		System.out.println("Starting merged.");
		PolicyBlockOption pbp = new PolicyBlockOption(hf, TaxiWorldDomain.DOMAIN.getActions(), absPolicies.get(2).abstractedPolicy);
		PolicyBlockPolicy newPolicy3 = new PolicyBlockPolicy(epsilon);
		System.out.println(runTaxiLearning(newPolicy3, hf, pbp, merged, episodes, path + "Merged Option.csv")[episodes - 1]);
		System.out.println("Done with merged.\n");
		
		System.out.println("Starting Q-learning.");
		PolicyBlockPolicy newPolicy4 = new PolicyBlockPolicy(epsilon);
		System.out.println(runTaxiLearning(newPolicy4, hf, merged, episodes, path + "Q-Learning.csv")[episodes - 1]);
		System.out.println("Done with Q-learning.\n");
		
		System.out.println("Starting Single-source options.");
		PolicyBlockOption pop1 = new PolicyBlockOption(hf, TaxiWorldDomain.DOMAIN.getActions(), absPolicies.get(0).abstractedPolicy);
		PolicyBlockOption pop2 = new PolicyBlockOption(hf, TaxiWorldDomain.DOMAIN.getActions(), absPolicies.get(1).abstractedPolicy);
		List<Option> ps = new ArrayList<Option>();
		ps.add(pop1);
		ps.add(pop2);
		PolicyBlockPolicy newPolicy5 = new PolicyBlockPolicy(epsilon);
		System.out.println(runTaxiLearning(newPolicy5, hf, ps, merged, episodes, path + "Flat Policy Option.csv")[episodes - 1]);
		System.out.println("Done with Single-source options.\n");
		
		System.out.println("Starting random.");
		PolicyBlockOption rando = generateRandomOption(hf, TaxiWorldDomain.DOMAIN.getActions(), newPolicy1.policy.keySet());
        PolicyBlockPolicy newPolicy6 = new PolicyBlockPolicy(epsilon);
        System.out.println(runTaxiLearning(newPolicy6, hf, rando, merged, episodes, path + "Random Option.csv")[episodes - 1]);
        System.out.println("Done with random.\n");
        
        System.out.println("Experiment finished. Took a total of " + ((System.currentTimeMillis() - startTime) / 60000.0) + " minutes.");
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
		
        for (int i=0;i<2000;i++) {
        	ea = FourRooms.Q.runLearningEpisodeFrom(ss2);
        }
        
		PolicyBlockPolicy p1 = new PolicyBlockPolicy((QLearning)FourRooms.Q,0);
		((QLearning)FourRooms.Q).setLearningPolicy(p1);
        EpisodeAnalysis ea1 = new EpisodeAnalysis();
		
        for (int i=0;i<100;i++) {
        	ea1 = FourRooms.Q.runLearningEpisodeFrom(ss2);
        }
        
		PolicyBlockPolicy p2 = new PolicyBlockPolicy((QLearning)FourRooms.Q,0);
		((QLearning)FourRooms.Q).setLearningPolicy(p2);
        EpisodeAnalysis ea2 = new EpisodeAnalysis();
		
        for (int i=0;i<2;i++) {
        	ea2 = FourRooms.Q.runLearningEpisodeFrom(ss2);
        }
        
        List<PolicyBlockPolicy> pp = new ArrayList<PolicyBlockPolicy>();
        pp.add(p1);
        pp.add(p2);
        
		AbstractedPolicy ap = new AbstractedPolicy(((QLearning)FourRooms.Q).getHashingFactory(),p,pp);
		System.out.println(ap.abstractedPolicy.size());
	}

	public AbstractedPolicy() {
		abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
		originalPolicies = new ArrayList<PolicyBlockPolicy>();
		absPol = new HashMap<StateHashTuple, List<QValue>>();
	}
	
	public AbstractedPolicy(AbstractedPolicy p) {
		this();
		this.abstractedPolicy.putAll(p.abstractedPolicy);
		this.originalPolicies.addAll(p.originalPolicies);
		this.absPol.putAll(p.absPol);
	}


	/**
	 * Accepts a single initial policy and a set of other initial policies,
	 * abstracts a single abstract policy (the abstraction of the first initial policy relative to the others)
	 * @param sh - StateHashFactory is used to go from State - > StateHashTuple after abstraction
	 * @param initialPolicy
	 * @param policyList
	 */
	public AbstractedPolicy(StateHashFactory sh,PolicyBlockPolicy initialPolicy,List<PolicyBlockPolicy> policyList) {	
		State s = findLimitingStateOverall(initialPolicy,policyList);
		
		Map<String,Integer> lciMap = leastCommonIntersectionState(s,getInitialState(initialPolicy));

		ArrayList<ArrayList<ObjectInstance>> listFromMapping =  getListFromMapping(lciMap,s);		
		
		ArrayList<ArrayList<ObjectInstance>> listOfCombinations = generateCombinations(listFromMapping);
		
		abstractedPolicy = score(sh,getInitialState(initialPolicy),initialPolicy,listOfCombinations);
	}
	
	/**
	 * Scores all possible abstractions and finds the abstraction with the lowest error
	 * @param original
	 * @param la
	 * @param objList
	 * @return
	 */
	public static Map<StateHashTuple,GroundedAction> score(StateHashFactory sh,State reducedState,PolicyBlockPolicy p,ArrayList<ArrayList<ObjectInstance>> objList) {		
		List<Map<StateHashTuple,GroundedAction>> policyList = makeNewPolicies(sh,objList,reducedState,p);
		ArrayList<GroundedAction> actionSequence = new ArrayList<GroundedAction>();
		ArrayList<ArrayList<GroundedAction>> actionList = new ArrayList<ArrayList<GroundedAction>>();
		Map<Integer,Double> mapOfDiff = new HashMap<Integer,Double>();
		ArrayList<GroundedAction> originalActions = new ArrayList<GroundedAction>();
		
		int i = 0;
		double lowest = 0;
		
		Boolean flag = true;
		
		//creates action sequences of all abstractions
		for (Map<StateHashTuple,GroundedAction> map : policyList) {
			actionSequence = new ArrayList<GroundedAction>();
			for (Map.Entry<StateHashTuple,GroundedAction> entry : map.entrySet()) {
				actionSequence.add(entry.getValue());
			}	
			actionList.add(actionSequence);
		}

		//generate original action sequence
		for (Map.Entry<StateHashTuple,GroundedAction> entry : p.policy.entrySet()) {
			originalActions.add(entry.getValue());
		}
		
		//find difference between original action sequence and abstractions
		for (List<GroundedAction> gaList : actionList) {
			mapOfDiff.put(i,findDifference(originalActions,gaList));
			i += 1;
		}
		
		//finds abstraction with lowest error
		for (Map.Entry<Integer,Double> entry : mapOfDiff.entrySet()) {
			if (flag) {
				lowest = entry.getValue();
				i = entry.getKey();
			} else {
				if (entry.getValue() < lowest) {
					lowest = entry.getValue();
					i = entry.getKey();
				}
			}
		}
		
		return policyList.get(i);
	}

	/**
	 * Finds the correct action that corresponds to the given state.
	 * If multiple states are identical, Q values are averaged.
	 * Otherwise the Q values remain the same.
	 * @param toCheck - state to find action for
	 * @param stateList - all lowered states from original policy
	 * @param oldPolicy - original policy
	 * @return action with the highest Q value
	 */
	public static AbstractGroundedAction findCorrectAction(State toCheck,List<State> stateList,PolicyBlockPolicy oldPolicy) {
		List<State> stateMatches = new ArrayList<State>();
		ArrayList<ArrayList<QValue>> qList = new ArrayList<ArrayList<QValue>>();
		
		//checks for matching among states
		//**there will always be 1+ match, because
		//baseState is in stateList
		for (State s : stateList) {
			if (toCheck.getCompleteStateDescription().equals(s.getCompleteStateDescription())) {
				stateMatches.add(s);
			}
		}		
		
		//makes list of all Q values that correspond to stateMatches
		for (State s : stateMatches) {
			QLearningStateNode oi = ((QLearning)oldPolicy.qplanner).getStateNode(((QLearning)oldPolicy.qplanner).stateHash(s));
			qList.add((ArrayList<QValue>)oi.qEntry);
		}
		
		List<QValue> av = findAverages(stateMatches.get(0),qList);
		
		return findHighestQValue(av);	
	}
	
	public static AbstractGroundedAction findHighestQValue(List<QValue> qList) {
		QValue highest = qList.get(0);
		
		for (QValue q : qList) {
			if (q.q > highest.q) {
				highest = q;
			}
		}

		return highest.a;
	}
	
	/**
	 * Finds averages of Q values in a given list. 
	 * @param s
	 * @param toAverage
	 * @return
	 */
	public static List<QValue> findAverages(State s,ArrayList<ArrayList<QValue>> toAverage) {
		List<QValue> averageQ = new ArrayList<QValue>();
		int i = 0;
		int j = 0;
		double sum = 0;
		double average = 0;

		while (i < toAverage.get(0).size()) {
			sum = 0;
			for (ArrayList<QValue> qList : toAverage) {
				sum += qList.get(i).q;
				j += 1;
			}
			average = sum / j;
			j = 0;
			QValue newQ = new QValue(s,toAverage.get(0).get(i).a,average);
			
			averageQ.add(newQ);
			i += 1;	
		}
		
		return averageQ;
	}
	
	/**
	 * Uses makeNewPolicy()
	 * @param sh
	 * @param listOfCombinations - combinations of possible abstractions (objects to be turned into states)
	 * @param reducedState
	 * @param oldPolicy
	 * @return
	 */
	public static List<Map<StateHashTuple,GroundedAction>> makeNewPolicies(StateHashFactory sh,ArrayList<ArrayList<ObjectInstance>> listOfCombinations,State reducedState,PolicyBlockPolicy oldPolicy) {
		List<Map<StateHashTuple,GroundedAction>> newMap = new ArrayList<Map<StateHashTuple,GroundedAction>>();
		State s = null;
		
		for (ArrayList<ObjectInstance> o : listOfCombinations) {
			s = new State();
			
			for (ObjectInstance oo : o) {
				s.addObject(oo);
			}
			
			newMap.add(makeNewPolicy(sh,s,oldPolicy));
		}
		
		return newMap;
	}
	
	/**
	 * Returns the a state stored in the policy's hashmap.
	 * @param initialPolicy
	 * @return
	 */
	public static State getInitialState(PolicyBlockPolicy initialPolicy) {
		State initialState = null;
		for (Map.Entry<StateHashTuple, GroundedAction> entry : initialPolicy.policy.entrySet()) {
			initialState = entry.getKey().s;
		}	
		
		return initialState;
	}
	
	/**
	 * Finds the state that corresponds to the Least Common Intersection of a list of policies
	 * @param initialPolicy
	 * @param policyList
	 * @return
	 */
	public static State findLimitingStateOverall(PolicyBlockPolicy initialPolicy,List<PolicyBlockPolicy> policyList) {
		Boolean flag = true;
		State s = null;
		State s1 = null;
		State s2 = null;
		State initialState = null;
		
		for (PolicyBlockPolicy pol : policyList) {
			for (Map.Entry<StateHashTuple, GroundedAction> entry : pol.policy.entrySet()) {
				s1 = entry.getKey().s;

				if (flag) {
					s = s1;
					flag = false;
				}
			}
			s2 = s;

			s = findLimitingState(generateLCIMappingState(s1),generateLCIMappingState(s2),s1,s2);
		}
		
		for (Map.Entry<StateHashTuple, GroundedAction> entry : initialPolicy.policy.entrySet()) {
			initialState = entry.getKey().s;
		}
		
		s = findLimitingState(generateLCIMappingState(initialState),generateLCIMappingState(s),initialState,s);
		
		return s;
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
		Boolean flag = true;
		ArrayList<ObjectInstance> ordered = new ArrayList<ObjectInstance>(unordered.size());
		List<String> nameList = new ArrayList<String>();
		
		for (ObjectInstance o : unordered) {
			nameList.add(o.getName());
		}
		
		Collections.sort(nameList);
		
		for (String s : nameList) {
			for (ObjectInstance obj : unordered) {
				if (s.equals(obj.getName()) && flag) {
					ordered.add(obj);
					flag = false;
				}
			}
			flag = true;
		}
		
		return ordered;
	}
	
	/**
	 * Generates all combinations given a multidimensional arraylist of objects.
	 * **Does not generate combinations with duplicate objects (assumes all objects have distinct names)
	 * If 2 objects need to be added, they will exist twice within sets.
	 * @param sets
	 * @return
	 */
	public static ArrayList<ArrayList<ObjectInstance>> generateCombinations(ArrayList<ArrayList<ObjectInstance>> sets) {
	    ArrayList<ArrayList<ObjectInstance>> output = new ArrayList<ArrayList<ObjectInstance>>();
	    ArrayList<ObjectInstance> toOrder;
		int solutions = 1;
	    int j = 0;
	    
		for(int i = 0; i < sets.size(); i++) {
	    	solutions *= sets.get(i).size();
	    }
	    
		for(int i = 0; i < solutions; i++) {
	        j = 1;  
	        toOrder = new ArrayList<ObjectInstance>();
	        
	        for(ArrayList<ObjectInstance> set : sets) {
	        	toOrder.add(set.get((i/j)%set.size()));
	            j *= set.size();
	        }
	       
	        if (!output.contains(orderOI(toOrder)) && !hasDuplicates(toOrder)) {
	        	output.add(orderOI(toOrder));
	        }
	    }
	    return output;
	}
	
	/**
	 * Checks if there are duplicate objects within the list
	 * @param toCheck
	 * @return true, if there are duplicates. otherwise, false
	 */
	public static Boolean hasDuplicates(ArrayList<ObjectInstance> toCheck) {
		int count = 0;
		
		for (ObjectInstance o : toCheck) {
			for (ObjectInstance oo : toCheck) {
				if(oo.equals(o)) {
					count += 1;
				}
			}
			if (count > 1) {
				return true;
			}
			count = 0;
		}
		return false;
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
				domainCount.put(o.getObjectClass().name, domainCount.get(o.getObjectClass().name)+1);
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
					domain2Count.put(entry.getKey(),domain2Count.get(entry.getKey()));
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
	 * Lowers policy to baseState level.
	 * Also finds correct action of each state.
	 * @param sh
	 * @param baseState - Least Common Intersection
	 * @param oldPolicy - policy that is being abstracted
	 * @return
	 */
	public static HashMap<StateHashTuple,GroundedAction> makeNewPolicy(StateHashFactory sh,State baseState,PolicyBlockPolicy oldPolicy) {
		HashMap<StateHashTuple,GroundedAction> newPolicy = new HashMap<StateHashTuple,GroundedAction>();
		State s = null;
		State hashedState = null;
		List<State> stateList = new ArrayList<State>();
		
		//generates states that match the baseState's objects, with the policy's states' attributes
		for (Map.Entry<StateHashTuple,GroundedAction> entry : oldPolicy.policy.entrySet()) {
			s = new State();
			
			hashedState = entry.getKey().s;
			
			for (ObjectInstance o : baseState.getAllObjects()) {
				s.addObject(o);
			}

			for (ObjectInstance hashO : hashedState.getAllObjects()) {
				for (ObjectInstance o : s.getAllObjects()) {
					if (hashO.getName().equals(o.getName())) {
						for (Attribute a : hashO.getObjectClass().attributeList) {
							s.getObject(o.getName()).setValue(a.name, hashO.getDiscValForAttribute(a.name));
						}
					}
				}
			}
			
			stateList.add(s.copy());
		}
		
		for (State st : stateList) {
			newPolicy.put(sh.hashState(st), (GroundedAction)findCorrectAction(st,stateList,oldPolicy));
		}
		
		return newPolicy;
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
			List<QValue> qs = otherPolicy.absPol.get(e.getKey());
			if (a != null && a.equals(e.getValue())) {
				merged.abstractedPolicy.put(e.getKey(), e.getValue());
				
				List<QValue> qs2 = absPol.get(e.getKey());
				List<QValue> maxQs = new ArrayList<QValue>();
				for (QValue q : qs) {
					for (QValue q2 : qs2) {
						if (q.a.actionName().equals(q2.a.actionName())) {
							QValue newQ = new QValue(q.s, q.a, Math.max(q.q, q2.q));
							maxQs.add(newQ);
						}
					}
				}
				
				merged.absPol.put(e.getKey(), maxQs);
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
			throw new IllegalArgumentException("Need a depth >= 2 and <= " + list.size());
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
			newP.absPol.putAll(p.qpolicy);
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