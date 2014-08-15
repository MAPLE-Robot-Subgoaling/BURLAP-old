package burlap.behavior.PolicyBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.QLearningStateNode;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class AbstractedPolicy {
	private Set<PolicyBlockPolicy> originalPolicies;
	public Map<StateHashTuple, GroundedAction> abstractedPolicy;

	public static long[] runTaxiLearning(PolicyBlockPolicy policy,
			StateHashFactory hf, int[][] passPos, int episodes, String filepath)
			throws IOException {
		return runTaxiLearning(policy, hf, new ArrayList<Option>(), passPos,
				episodes, filepath);
	}

	public static long[] runTaxiLearning(PolicyBlockPolicy policy,
			StateHashFactory hf, Option o, int[][] passPos, int episodes,
			String filepath) throws IOException {
		List<Option> os = new ArrayList<Option>();
		os.add(o);
		return runTaxiLearning(policy, hf, os, passPos, episodes, filepath);
	}

	public static long[] runTaxiLearning(PolicyBlockPolicy policy,
			StateHashFactory hf, List<Option> os, int[][] passPos,
			int episodes, String filepath) throws IOException {
		TaxiWorldDomain.MAXPASS = passPos.length;
		QLearning Q = new QLearning(TaxiWorldDomain.DOMAIN, TaxiWorldDomain.rf,
				TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
				TaxiWorldDomain.GAMMA, TaxiWorldDomain.LEARNINGRATE,
				Integer.MAX_VALUE);
		// TaxiWorldDomain td = new TaxiWorldDomain();
		// td.generateDomain();
		State s = TaxiWorldDomain.getCleanState();
		Q.setLearningPolicy(policy);
		policy.setPlanner(Q);

		for (Option o : os) {
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
				TaxiWorldDomain.setPassenger(s, j, passPos[j - 1][0],
						passPos[j - 1][1]);
			}

			EpisodeAnalysis analyzer = new EpisodeAnalysis();
			analyzer = Q.runLearningEpisodeFrom(s);
			cumul += analyzer.numTimeSteps();
			b.write((i + 1) + "," + cumul + "\n");
			cumulArr[i] = cumul;
		}
		b.close();

		return cumulArr;
	}

	public static PolicyBlockOption generateRandomOption(StateHashFactory hf,
			List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
		Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
		Random rand = new Random();
		int count = 0;
		int max = rand.nextInt((stateSpace.size()));

		// Samples from the state space
		for (StateHashTuple s : stateSpace) {
			if (count >= max) {
				break;
			}
			policy.put(
					s,
					new GroundedAction(actionSpace.get(rand.nextInt(actionSpace
							.size())), ""));
			count++;
		}

		System.out.println(count);
		System.out.println(stateSpace.size());
		return new PolicyBlockOption(hf, policy, actionSpace);
	}

	/**
	 * This method generates an option to have the agent take a cyclic path in
	 * the TaxiWorldDomain. Acts as a sanity check to make sure that options
	 * wont cycle forever
	 * 
	 * @param hf
	 * @param actionSpace
	 * @param stateSpace
	 * @return an option representing a cyclic path
	 */
	public static PolicyBlockOption generateCyclicOption(StateHashFactory hf,
			List<Action> actionSpace, Set<StateHashTuple> stateSpace) {
		Map<StateHashTuple, GroundedAction> policy = new HashMap<StateHashTuple, GroundedAction>();
		Action n = null;
		Action s = null;
		Action e = null;
		Action w = null;
		for (Action a : actionSpace) {
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
			throw new NullPointerException(
					"Action space doesn't define the correct actions for this domain.");
		}
		// (1, 5) -> e; (2, 5) -> n; (2, 6) -> w; (1,6) -> s
		for (StateHashTuple st : stateSpace) {
			int x = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT)
					.getDiscValForAttribute(TaxiWorldDomain.ATTX);
			int y = st.s.getFirstObjectOfClass(TaxiWorldDomain.CLASSAGENT)
					.getDiscValForAttribute(TaxiWorldDomain.ATTY);
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

		return new PolicyBlockOption(hf, policy, actionSpace);
	}

	public static void main(String args[]) throws IOException {
		/*
		 * String path = "C:/Users/Allison/Desktop/"; TaxiWorldDomain.MAXPASS =
		 * 2;
		 * 
		 * new TaxiWorldDomain().generateDomain(); DiscreteStateHashFactory hf =
		 * new DiscreteStateHashFactory();
		 * hf.setAttributesForClass(TaxiWorldDomain.CLASSAGENT,
		 * TaxiWorldDomain.DOMAIN
		 * .getObjectClass(TaxiWorldDomain.CLASSAGENT).attributeList); double
		 * epsilon = 0.8; int episodes = 1000; long startTime =
		 * System.currentTimeMillis();
		 */
		// int[][] first = {{1, 2}, {3, 2}};
		// int[][] second = {{1, 4}, {5, 4}};
		// int[][] merged = {{13, 7}, {7, 7}};
		// List<Integer[]> open = TaxiWorldDomain.getOpenSpots();
		// for (Integer[] o: open) {
		// System.out.println(o[0] + ": " + o[1]);
		// }
		/*
		 * int[][] first =
		 * TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS); int[][]
		 * second = TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
		 * int[][] merged =
		 * TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
		 * System.out.print("First: { "); for (int i = 0; i < first.length; i++)
		 * { System.out.print("("+first[i][0]+", "+first[i][1]+") "); }
		 * System.out.println("}"); System.out.print("Second: { "); for (int i =
		 * 0; i < second.length; i++) {
		 * System.out.print("("+second[i][0]+", "+second[i][1]+") "); }
		 * System.out.println("}"); System.out.print("Merged: { "); for (int i =
		 * 0; i < merged.length; i++) {
		 * System.out.print("("+merged[i][0]+", "+merged[i][1]+") "); }
		 * System.out.println("}");
		 * 
		 * System.out.println("Starting first source."); PolicyBlockPolicy
		 * newPolicy1 = new PolicyBlockPolicy(epsilon);
		 * System.out.println(runTaxiLearning(newPolicy1, hf, first, episodes,
		 * path + "one.csv")[episodes - 1]);
		 * System.out.println("Done with first source.\n");
		 * 
		 * System.out.println("Starting second source."); PolicyBlockPolicy
		 * newPolicy2 = new PolicyBlockPolicy(epsilon);
		 * System.out.println(runTaxiLearning(newPolicy2, hf, second, episodes,
		 * path + "two.csv")[episodes - 1]);
		 * System.out.println("Done with second source.\n");
		 * 
		 * ArrayList<PolicyBlockPolicy> pis = new
		 * ArrayList<PolicyBlockPolicy>(); pis.add(newPolicy1);
		 * pis.add(newPolicy2); List<AbstractedPolicy> absPolicies =
		 * unionMerge(pis, hf, pis.size()); for (AbstractedPolicy ap:
		 * absPolicies) { System.out.println(ap.abstractedPolicy.size()); }
		 * 
		 * System.out.println("Starting merged."); PolicyBlockOption pbp = new
		 * PolicyBlockOption(hf, TaxiWorldDomain.DOMAIN.getActions(),
		 * absPolicies.get(2).abstractedPolicy); PolicyBlockPolicy newPolicy3 =
		 * new PolicyBlockPolicy(epsilon);
		 * System.out.println(runTaxiLearning(newPolicy3, hf, pbp, merged,
		 * episodes, path + "Merged Option.csv")[episodes - 1]);
		 * System.out.println("Done with merged.\n");
		 * 
		 * System.out.println("Starting Q-learning."); PolicyBlockPolicy
		 * newPolicy4 = new PolicyBlockPolicy(epsilon);
		 * System.out.println(runTaxiLearning(newPolicy4, hf, merged, episodes,
		 * path + "Q-Learning.csv")[episodes - 1]);
		 * System.out.println("Done with Q-learning.\n");
		 * 
		 * System.out.println("Starting Single-source options.");
		 * PolicyBlockOption pop1 = new PolicyBlockOption(hf,
		 * TaxiWorldDomain.DOMAIN.getActions(),
		 * absPolicies.get(0).abstractedPolicy); PolicyBlockOption pop2 = new
		 * PolicyBlockOption(hf, TaxiWorldDomain.DOMAIN.getActions(),
		 * absPolicies.get(1).abstractedPolicy); List<Option> ps = new
		 * ArrayList<Option>(); ps.add(pop1); ps.add(pop2); PolicyBlockPolicy
		 * newPolicy5 = new PolicyBlockPolicy(epsilon);
		 * System.out.println(runTaxiLearning(newPolicy5, hf, ps, merged,
		 * episodes, path + "Flat Policy Option.csv")[episodes - 1]);
		 * System.out.println("Done with Single-source options.\n");
		 * 
		 * System.out.println("Starting random."); PolicyBlockOption rando =
		 * generateRandomOption(hf, TaxiWorldDomain.DOMAIN.getActions(),
		 * newPolicy1.policy.keySet()); PolicyBlockPolicy newPolicy6 = new
		 * PolicyBlockPolicy(epsilon);
		 * System.out.println(runTaxiLearning(newPolicy6, hf, rando, merged,
		 * episodes, path + "Random Option.csv")[episodes - 1]);
		 * System.out.println("Done with random.\n");
		 * 
		 * System.out.println("Experiment finished. Took a total of " +
		 * ((System.currentTimeMillis() - startTime) / 60000.0) + " minutes.");
		 */

		// Need to have a method to balloon each policy using reachability to
		// assure it's defined
		// TODO maybe put the considerations for null mappings into the merging,
		// to prevent extra computation
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
		goal1.setValue("y", 11);
		goal2.setValue("x", 11);
		goal2.setValue("y", 11);
		ObjectInstance block1 = new ObjectInstance(
				finalDomain2.getObjectClass("random"), "random" + 0);
		block1.setValue("x", 5);
		block1.setValue("y", 2);
		ObjectInstance block2 = new ObjectInstance(
				finalDomain2.getObjectClass("random"), "random" + 1);
		block2.setValue("x", 7);
		block2.setValue("y", 4);
		ObjectInstance block3 = new ObjectInstance(
				finalDomain2.getObjectClass("random"), "random" + 2);
		block3.setValue("x", 3);
		block3.setValue("y", 3);
		ss2.addObject(block1);
		ss2.addObject(block2);
		ss1.addObject(block3);
		ss1.addObject(block1);
		ss1.addObject(block2);

		PolicyBlockPolicy p1 = new PolicyBlockPolicy((QLearning) FourRooms.Q,
				0.8);
		((QLearning) FourRooms.Q).setLearningPolicy(p1);
		for (int i = 0; i < 1; i++) {
			FourRooms.Q.runLearningEpisodeFrom(ss1);
		}
		PolicyBlockPolicy p2 = new PolicyBlockPolicy((QLearning) FourRooms.Q,
				0.8);
		((QLearning) FourRooms.Q).setLearningPolicy(p2);
		for (int i = 0; i < 1; i++) {
			FourRooms.Q.runLearningEpisodeFrom(ss2);
		}
		PolicyBlockPolicy p3 = new PolicyBlockPolicy((QLearning) FourRooms.Q,
				0.8);
		((QLearning) FourRooms.Q).setLearningPolicy(p3);
		for (int i = 0; i < 1; i++) {
			FourRooms.Q.runLearningEpisodeFrom(ss2);
		}

		// TODO
		ArrayList<PolicyBlockPolicy> pl1 = new ArrayList<PolicyBlockPolicy>();
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		pl1.add(p1);
		pl1.add(p2);
		pl1.add(p3);
		StateHashFactory hf = ((OOMDPPlanner) FourRooms.Q).getHashingFactory();
		List<AbstractedPolicy> as = unionMerge(hf, pl1, pl1.size());
		for (AbstractedPolicy a : as) {
			System.out.println(a.getOriginalPolicies().size());
			System.out.println(a.size());
		}
		System.out.println();

		AbstractedPolicy best = scoreUnionMerge(hf, as);
		System.out.println();
		System.out.println(best);
		System.out.println(best.size());
	}

	public AbstractedPolicy() {
		abstractedPolicy = new HashMap<StateHashTuple, GroundedAction>();
		originalPolicies = new HashSet<PolicyBlockPolicy>();
	}

	public AbstractedPolicy(AbstractedPolicy p) {
		this();
		this.abstractedPolicy.putAll(p.abstractedPolicy);
		this.originalPolicies.addAll(p.originalPolicies);
	}

	/**
	 * Accepts a single initial policy and a set of other initial policies,
	 * abstracts a single abstract policy (the abstraction of the first initial
	 * policy relative to the others)
	 * 
	 * @param sh
	 *            - StateHashFactory is used to go from State - > StateHashTuple
	 *            after abstraction
	 * @param initialPolicy
	 * @param policyList
	 */
	public AbstractedPolicy(StateHashFactory sh, PolicyBlockPolicy p,
			List<PolicyBlockPolicy> ps) {
		State s = findLimitingStateOverall(p, ps);

		Map<String, Integer> lciMap = leastCommonIntersectionState(s,
				getInitialState(p));

		ArrayList<ArrayList<ObjectInstance>> listFromMapping = getListFromMapping(
				lciMap, s);

		ArrayList<ArrayList<ObjectInstance>> listOfCombinations = generateCombinations(listFromMapping);

		abstractedPolicy = scoreAbstraction(sh, getInitialState(p), p,
				listOfCombinations);
		ps.add(p);
		originalPolicies = new HashSet<PolicyBlockPolicy>();
		originalPolicies.addAll(ps);
	}

	/**
	 * Scores all possible abstractions and finds the abstraction with the
	 * lowest error
	 * 
	 * @param original
	 * @param la
	 * @param objList
	 * @return
	 */
	public static Map<StateHashTuple, GroundedAction> scoreAbstraction(
			StateHashFactory sh, State reducedState, PolicyBlockPolicy p,
			ArrayList<ArrayList<ObjectInstance>> objList) {
		List<Map<StateHashTuple, GroundedAction>> policyList = makeNewPolicies(
				sh, objList, reducedState, p);
		ArrayList<GroundedAction> actionSequence = new ArrayList<GroundedAction>();
		ArrayList<ArrayList<GroundedAction>> actionList = new ArrayList<ArrayList<GroundedAction>>();
		Map<Integer, Double> mapOfDiff = new HashMap<Integer, Double>();
		ArrayList<GroundedAction> originalActions = new ArrayList<GroundedAction>();

		int i = 0;
		double lowest = 0;

		Boolean flag = true;
		// TODO
		// this method is assuming that entrySet() preserves sorting, which it
		// doesn't
		// creates action sequences of all abstractions
		for (Map<StateHashTuple, GroundedAction> map : policyList) {
			actionSequence = new ArrayList<GroundedAction>();
			for (Map.Entry<StateHashTuple, GroundedAction> entry : map
					.entrySet()) {
				actionSequence.add(entry.getValue());
			}
			actionList.add(actionSequence);
		}

		// generate original action sequence
		for (Map.Entry<StateHashTuple, GroundedAction> entry : p.policy
				.entrySet()) {
			originalActions.add(entry.getValue());
		}

		/*
		 * Convert to array list
		 */
		// find difference between original action sequence and abstractions
		for (List<GroundedAction> gaList : actionList) {
			mapOfDiff.put(i, findDifference(originalActions, gaList));
			i += 1;
		}

		// finds abstraction with lowest error
		for (Map.Entry<Integer, Double> entry : mapOfDiff.entrySet()) {
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
	 * Finds the correct action that corresponds to the given state. If multiple
	 * states are identical, Q values are averaged. Otherwise the Q values
	 * remain the same.
	 * 
	 * @param toCheck
	 *            - state to find action for
	 * @param stateList
	 *            - all lowered states from original policy
	 * @param oldPolicy
	 *            - original policy
	 * @return action with the highest Q value
	 */
	public static AbstractGroundedAction findCorrectAction(State toCheck,
			List<State> stateList, PolicyBlockPolicy oldPolicy) {
		List<State> stateMatches = new ArrayList<State>();
		ArrayList<ArrayList<QValue>> qList = new ArrayList<ArrayList<QValue>>();

		// checks for matching among states
		// **there will always be 1+ match, because
		// baseState is in stateList
		for (State s : stateList) {
			if (toCheck.getCompleteStateDescription().equals(
					s.getCompleteStateDescription())) {
				stateMatches.add(s);
			}
		}

		// makes list of all Q values that correspond to stateMatches
		for (State s : stateMatches) {
			QLearningStateNode oi = ((QLearning) oldPolicy.qplanner)
					.getStateNode(((QLearning) oldPolicy.qplanner).stateHash(s));
			qList.add((ArrayList<QValue>) oi.qEntry);
		}

		List<QValue> av = findAverages(stateMatches.get(0), qList);

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
	 * 
	 * @param s
	 * @param toAverage
	 * @return
	 */
	public static List<QValue> findAverages(State s,
			ArrayList<ArrayList<QValue>> toAverage) {
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
			QValue newQ = new QValue(s, toAverage.get(0).get(i).a, average);

			averageQ.add(newQ);
			i += 1;
		}

		return averageQ;
	}

	/**
	 * Uses makeNewPolicy()
	 * 
	 * @param sh
	 * @param listOfCombinations
	 *            - combinations of possible abstractions (objects to be turned
	 *            into states)
	 * @param reducedState
	 * @param oldPolicy
	 * @return
	 */
	public static List<Map<StateHashTuple, GroundedAction>> makeNewPolicies(
			StateHashFactory sh,
			ArrayList<ArrayList<ObjectInstance>> listOfCombinations,
			State reducedState, PolicyBlockPolicy oldPolicy) {
		List<Map<StateHashTuple, GroundedAction>> newMap = new ArrayList<Map<StateHashTuple, GroundedAction>>();
		State s = null;

		for (ArrayList<ObjectInstance> o : listOfCombinations) {
			s = new State();

			for (ObjectInstance oo : o) {
				s.addObject(oo);
			}

			newMap.add(makeNewPolicy(sh, s, oldPolicy));
		}

		return newMap;
	}

	/**
	 * Returns the a state stored in the policy's hashmap.
	 * 
	 * @param initialPolicy
	 * @return
	 */
	public static State getInitialState(PolicyBlockPolicy initialPolicy) {
		State initialState = null;
		for (Map.Entry<StateHashTuple, GroundedAction> entry : initialPolicy.policy
				.entrySet()) {
			initialState = entry.getKey().s;
		}

		return initialState;
	}

	/**
	 * Finds the state that corresponds to the Least Common Intersection of a
	 * list of policies
	 * 
	 * @param initialPolicy
	 * @param ps
	 * @return
	 */
	public static State findLimitingStateOverall(
			PolicyBlockPolicy initialPolicy, List<PolicyBlockPolicy> ps) {
		Boolean flag = true;
		State s = null;
		State s1 = null;
		State s2 = null;
		State initialState = null;

		for (PolicyBlockPolicy pol : ps) {
			for (Map.Entry<StateHashTuple, GroundedAction> entry : pol.policy
					.entrySet()) {
				s1 = entry.getKey().s;

				if (flag) {
					s = s1;
					flag = false;
				}
			}
			s2 = s;

			s = findLimitingState(generateLCIMappingState(s1),
					generateLCIMappingState(s2), s1, s2);
		}

		for (Map.Entry<StateHashTuple, GroundedAction> entry : initialPolicy.policy
				.entrySet()) {
			initialState = entry.getKey().s;
		}

		s = findLimitingState(generateLCIMappingState(initialState),
				generateLCIMappingState(s), initialState, s);

		return s;
	}

	/**
	 * Finds quantitative difference between two ArrayLists Used for calculating
	 * error
	 * 
	 * @param actionSequence
	 * @param actionSequence2
	 * @return
	 */
	public static double findDifference(List<GroundedAction> actionSequence,
			List<GroundedAction> actionSequence2) {
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

		return error / actionSequence.size();
	}

	/**
	 * Makes state given a list of ObjectInstances
	 * 
	 * @param objList
	 * @return
	 */
	public static State makeStateWith(
			ArrayList<ArrayList<ObjectInstance>> objList) {
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
	 * 
	 * @param unordered
	 * @return
	 */
	public static ArrayList<ObjectInstance> orderOI(
			ArrayList<ObjectInstance> unordered) {
		Boolean flag = true;
		ArrayList<ObjectInstance> ordered = new ArrayList<ObjectInstance>(
				unordered.size());
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
	 * Generates all combinations given a multidimensional ArrayList of objects.
	 * **Does not generate combinations with duplicate objects (assumes all
	 * objects have distinct names) If 2 objects need to be added, they will
	 * exist twice within sets.
	 * 
	 * @param sets
	 * @return
	 */
	public static ArrayList<ArrayList<ObjectInstance>> generateCombinations(
			ArrayList<ArrayList<ObjectInstance>> sets) {
		ArrayList<ArrayList<ObjectInstance>> output = new ArrayList<ArrayList<ObjectInstance>>();
		ArrayList<ObjectInstance> toOrder;
		int solutions = 1;
		int j = 0;

		for (int i = 0; i < sets.size(); i++) {
			solutions *= sets.get(i).size();
		}

		for (int i = 0; i < solutions; i++) {
			j = 1;
			toOrder = new ArrayList<ObjectInstance>();

			for (ArrayList<ObjectInstance> set : sets) {
				toOrder.add(set.get((i / j) % set.size()));
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
	 * 
	 * @param toCheck
	 * @return true, if there are duplicates. otherwise, false
	 */
	public static Boolean hasDuplicates(ArrayList<ObjectInstance> toCheck) {
		int count = 0;

		for (ObjectInstance o : toCheck) {
			for (ObjectInstance oo : toCheck) {
				if (oo.equals(o)) {
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
	 * Finds list that corresponds to the mapping provided. If there are more
	 * than one of a type of object, duplicates that list as many times as there
	 * are that object.
	 * 
	 * @param map
	 * @param s
	 * @return
	 */
	public static ArrayList<ArrayList<ObjectInstance>> getListFromMapping(
			Map<String, Integer> map, State s) {
		ArrayList<ArrayList<ObjectInstance>> objList = new ArrayList<ArrayList<ObjectInstance>>();
		ArrayList<ArrayList<ObjectInstance>> toAdd = new ArrayList<ArrayList<ObjectInstance>>();
		int i = 1;

		for (Map.Entry<String, Integer> entry : map.entrySet()) {
			objList.add(getListOfObject(s, entry.getKey()));
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
	 * 
	 * @param s
	 * @param name
	 * @return
	 */
	public static ArrayList<ObjectInstance> getListOfObject(State s, String name) {

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
	 * 
	 * @param s
	 *            - State to be mapped
	 * @return Map that includes the names of the objects and their frequency
	 */
	public static Map<String, Integer> generateLCIMappingState(State s) {
		Map<String, Integer> domainCount = new HashMap<String, Integer>();

		for (ObjectInstance o : s.getAllObjects()) {
			if (domainCount.containsKey(o.getObjectClass().name)) {
				domainCount.put(o.getObjectClass().name,
						domainCount.get(o.getObjectClass().name) + 1);
			} else {
				domainCount.put(o.getObjectClass().name, 1);
			}
		}

		return domainCount;
	}

	/**
	 * Generates the Least Common Intersection of two states.
	 * 
	 * @param a
	 * @param b
	 * @return Map of ObjectClass name and frequency
	 */
	public static Map<String, Integer> leastCommonIntersectionState(State a,
			State b) {

		Map<String, Integer> domain1Count = generateLCIMappingState(a);
		Map<String, Integer> domain2Count = generateLCIMappingState(b);
		ArrayList<String> toRemove = new ArrayList<String>();

		for (Map.Entry<String, Integer> entry : domain1Count.entrySet()) {
			if (domain2Count.containsKey(entry.getKey())) {
				if (entry.getValue() < domain2Count.get(entry.getKey())) {
					domain2Count.put(entry.getKey(),
							domain2Count.get(entry.getKey()));
					// System.out.println(domain2Count.get(entry.getKey()) + " "
					// + entry.getKey());
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
	 * Lowers policy to baseState level. Also finds correct action of each
	 * state.
	 * 
	 * @param sh
	 * @param baseState
	 *            - Least Common Intersection
	 * @param oldPolicy
	 *            - policy that is being abstracted
	 * @return
	 */
	public static HashMap<StateHashTuple, GroundedAction> makeNewPolicy(
			StateHashFactory sh, State baseState, PolicyBlockPolicy oldPolicy) {
		HashMap<StateHashTuple, GroundedAction> newPolicy = new HashMap<StateHashTuple, GroundedAction>();
		State s = null;
		State hashedState = null;
		List<State> stateList = new ArrayList<State>();

		// generates states that match the baseState's objects, with the
		// policy's states' attributes
		for (Map.Entry<StateHashTuple, GroundedAction> entry : oldPolicy.policy
				.entrySet()) {
			s = new State();

			hashedState = entry.getKey().s;

			for (ObjectInstance o : baseState.getAllObjects()) {
				s.addObject(o);
			}

			for (ObjectInstance hashO : hashedState.getAllObjects()) {
				for (ObjectInstance o : s.getAllObjects()) {
					if (hashO.getName().equals(o.getName())) {
						for (Attribute a : hashO.getObjectClass().attributeList) {
							s.getObject(o.getName()).setValue(a.name,
									hashO.getDiscValForAttribute(a.name));
						}
					}
				}
			}

			stateList.add(s.copy());
		}

		for (State st : stateList) {
			newPolicy
					.put(sh.hashState(st),
							(GroundedAction) findCorrectAction(st, stateList,
									oldPolicy));
		}

		return newPolicy;
	}

	/**
	 * Finds the state that refers to the least common intersection of 2 states.
	 * 
	 * @param d1
	 *            Map of state in (ObjectClass name, frequency) form.
	 * @param d2
	 * @param s1
	 * @param s2
	 * @return State that limits the intersection
	 */
	public static State findLimitingState(Map<String, Integer> d1,
			Map<String, Integer> d2, State s1, State s2) {
		for (Map.Entry<String, Integer> entry1 : d1.entrySet()) {
			if (!d2.containsKey(entry1.getKey())
					|| d2.containsKey(entry1.getKey())
					&& d2.get(entry1.getKey()) < entry1.getValue()) {
				return s2;
			}
		}
		return s1;
	}

	/**
	 * Abstracts each policy with respect to each other
	 * 
	 * @param policies
	 *            - the list of policies to be abstracted with respect to
	 *            another
	 * @return the list of abstracted policies
	 */
	public static List<AbstractedPolicy> abstractAll(StateHashFactory hf,
			List<PolicyBlockPolicy> policies) {
		if (policies.size() < 2) {
			throw new IllegalArgumentException("Need at least 2 policies.");
		}

		List<AbstractedPolicy> abstractedPolicies = new ArrayList<AbstractedPolicy>();
		for (int i = 0; i < policies.size(); i++) {
			List<PolicyBlockPolicy> newPolicies = new ArrayList<PolicyBlockPolicy>();
			newPolicies.addAll(policies);

			PolicyBlockPolicy temp = newPolicies.remove(i);
			abstractedPolicies.add(new AbstractedPolicy(hf, temp, newPolicies));
			newPolicies.add(temp);
		}

		return abstractedPolicies;
	}

	public boolean isSameAbstraction(AbstractedPolicy other) {
		return this.getOriginalPolicies().equals(other.getOriginalPolicies());
	}

	public AbstractedPolicy mergeWith(AbstractedPolicy otherPolicy) {
		if (!isSameAbstraction(otherPolicy)) {
			throw new IllegalArgumentException(
					"Not the same level of abstraction.");
		}

		AbstractedPolicy merged = new AbstractedPolicy();
		merged.originalPolicies.addAll(this.originalPolicies);

		for (Entry<StateHashTuple, GroundedAction> e : abstractedPolicy
				.entrySet()) {
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
	 * 
	 * @param abstractedPolicies
	 *            - this list is modified as part of the recursion (becomes 1
	 *            element after all recursion is done).
	 * @return
	 */
	public static AbstractedPolicy merge(
			List<AbstractedPolicy> abstractedPolicies) {
		if (abstractedPolicies == null || abstractedPolicies.isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot pass a null or empty list of abstracted policies to merge.");
		}

		List<AbstractedPolicy> newPolicies = new ArrayList<AbstractedPolicy>();
		newPolicies.addAll(abstractedPolicies);
		AbstractedPolicy merged = new AbstractedPolicy(newPolicies.get(0));

		for (int i = 1; i < newPolicies.size(); i++) {
			merged = merged.mergeWith(newPolicies.get(i));
		}

		return merged;
	}

	/**
	 * Abstraction happens on a per-group basis of policies to merge.
	 * 
	 * @param policies
	 *            - the set of policies to be merged
	 * @param depth
	 *            - the upper bound of k
	 * @return performs the merge operation on all k-subsets of the list of
	 *         policies
	 */
	public static List<AbstractedPolicy> unionMerge(StateHashFactory hf,
			List<PolicyBlockPolicy> policies, int depth) {
		List<AbstractedPolicy> mergedPolicies = new ArrayList<AbstractedPolicy>();

		for (List<PolicyBlockPolicy> ps : getSubsets(policies, 2, depth)) {
			mergedPolicies.add(merge(abstractAll(hf, ps)));
		}

		return mergedPolicies;
	}

	/**
	 * Grounds each abstracted policy to its set of original policies to score
	 * the error in abstraction Error is measured as number of states with the
	 * same action / total number of states
	 * 
	 * @param hf
	 *            - the hashing factory
	 * @param merged
	 *            - the list of merged abstracted policies
	 * @return the best scoring abstracted policy
	 */
	public static AbstractedPolicy scoreUnionMerge(StateHashFactory hf,
			List<AbstractedPolicy> merged) {
		AbstractedPolicy ret = null;
		double max = 0.;
		for (AbstractedPolicy abs : merged) {
			double totalMatch = 0.;

			for (PolicyBlockPolicy orig : abs.getOriginalPolicies()) {
				List<PolicyBlockPolicy> temp = new ArrayList<PolicyBlockPolicy>();
				PolicyBlockPolicy tempP = new PolicyBlockPolicy(
						orig.getEpsilon());
				tempP.policy = abs.abstractedPolicy;
				temp.add(tempP);

				AbstractedPolicy origAb = new AbstractedPolicy(hf, orig, temp);
				double stateSize = origAb.abstractedPolicy.size();
				double stateMatch = 0.;

				// TODO
				// Is the check just for where the states exists in both, or for
				// when the states exist and have the same action in both?
				// working with same action for now
				// What to do in the event of a tie?
				for (Entry<StateHashTuple, GroundedAction> e : abs.abstractedPolicy
						.entrySet()) {
					if (origAb.abstractedPolicy.containsKey(e.getKey())
							&& origAb.abstractedPolicy.get(e.getKey()).equals(
									e.getValue())) {
						// Check for state match and then action match
						stateMatch += 1;
					}
				}
				totalMatch += stateMatch / stateSize;
			}
			System.out.println(abs.getOriginalPolicies());
			System.out.println(totalMatch + " / "
					+ abs.getOriginalPolicies().size());
			if (totalMatch > max) {
				ret = abs;
				max = totalMatch;
			}
		}

		return ret;
	}

	/**
	 * Generates all k-subsets from alpha to beta Runs O(Sum from k=alpha to
	 * beta of (n!)/(k! * (n-k)!))
	 * 
	 * @param set
	 *            - the set to be permuted
	 * @param alpha
	 *            - lower bound of k
	 * @param beta
	 *            - upper bound of k
	 * @return a list of all k-subsets from alpha to beta
	 */
	public static <T> List<List<T>> getSubsets(List<T> set, int alpha, int beta) {
		if (alpha < 1 || alpha > beta || alpha > set.size()) {
			throw new IllegalArgumentException("Invalid minimum depth size: "
					+ alpha + ".");
		} else if (beta > set.size() || beta < 1) {
			throw new IllegalArgumentException("Invalid maximum depth size: "
					+ beta + ".");
		}

		List<List<T>> ret = new ArrayList<List<T>>();
		int size = set.size();
		int[] range = new int[size];
		for (int i = 0; i < size; i++) {
			range[i] = i;
		}

		for (int i = alpha; i <= beta; i++) {
			int[][] combs = nexksb(range, i);
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
	 * 
	 * @param k
	 *            - size of subset tuples
	 * @param set
	 *            - the set to be generated from
	 * @return all subsets of size k
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
	 * Performs the binomial coefficient function C(n, k)
	 * 
	 * @param n
	 * @param k
	 * @return (n!)/(k! * (n - k)!)
	 */
	private static long binomial(int n, int k) {
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
	 * Gets the set of original policies to avoid changing them by mistake
	 * 
	 * @return set of original policies
	 */
	public Set<PolicyBlockPolicy> getOriginalPolicies() {
		return originalPolicies;
	}

	/**
	 * Gets the size of the state space for which the policy is defined
	 * 
	 * @return the size of the policy
	 */
	public int size() {
		return abstractedPolicy.size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (Entry<StateHashTuple, GroundedAction> e : abstractedPolicy
				.entrySet()) {
			builder.append(e.getKey().hashCode() + ": " + e.getValue() + "\n");
		}

		return builder.toString();
	}
}