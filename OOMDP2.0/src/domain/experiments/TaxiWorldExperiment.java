package domain.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import domain.taxiworld.TaxiWorldDomain;
import burlap.behavior.PolicyBlock.AbstractedOption;
import burlap.behavior.PolicyBlock.AbstractedPolicy;
import burlap.behavior.PolicyBlock.PolicyBlockPolicy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class TaxiWorldExperiment {
    public static long[] runTaxiLearning(PolicyBlockPolicy policy,
	    StateHashFactory hf, int[][] passPos, int episodes, String filepath)
	    throws IOException {
	return runTaxiLearning(policy, hf, passPos, new ArrayList<Option>(),
		episodes, filepath);
    }

    public static long[] runTaxiLearning(PolicyBlockPolicy policy,
	    StateHashFactory hf, int[][] passPos, Option o, int episodes,
	    String filepath) throws IOException {
	List<Option> os = new ArrayList<Option>();
	os.add(o);
	return runTaxiLearning(policy, hf, passPos, os, episodes, filepath);
    }

    public static long[] runTaxiLearning(PolicyBlockPolicy policy,
	    StateHashFactory hf, int[][] passPos, List<? extends Option> os,
	    int episodes, String filepath) throws IOException {
	TaxiWorldDomain.MAXPASS = passPos.length;
	QLearning Q = new QLearning(TaxiWorldDomain.DOMAIN, TaxiWorldDomain.rf,
		TaxiWorldDomain.tf, TaxiWorldDomain.DISCOUNTFACTOR, hf,
		TaxiWorldDomain.GAMMA, TaxiWorldDomain.LEARNINGRATE,
		Integer.MAX_VALUE);

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
	    TaxiWorldDomain.setGoal(3, 5);

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

    /**
     * Generates a random options defined over a random chunk of the state space
     * 
     * @param hf
     * @param actionSpace
     * @param stateSpace
     * @return a AbstractedOption randomly initialized
     */
    public static AbstractedOption generateRandomOption(StateHashFactory hf,
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
	return new AbstractedOption(hf, policy, "random");
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
    public static AbstractedOption generateCyclicOption(StateHashFactory hf,
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

	return new AbstractedOption(hf, policy, "cyclic");
    }

    public static void main(String args[]) throws IOException {
	String path = "C:/Users/Allison/Desktop/";
	TaxiWorldDomain.MAXPASS = 3;
	new TaxiWorldDomain().generateDomain();
	DiscreteStateHashFactory hf = new DiscreteStateHashFactory();
	hf.setAttributesForClass(
		TaxiWorldDomain.CLASSAGENT,
		TaxiWorldDomain.DOMAIN
			.getObjectClass(TaxiWorldDomain.CLASSAGENT).attributeList);
	double epsilon = 0.1;
	int episodes = 10;
	long startTime = System.currentTimeMillis();
	Random rand = new Random();
	int c = 1;
	int max = TaxiWorldDomain.MAXPASS;
	// Offset must always be one, or there will be value errors with
	// ATTCARRY
	// MAXPASS must never be set higher than max, ATTCARRY will have issues
	// as well
	// If MAXPASS must be set higher, the domain must be regenerated
	int offset = 1;

	TaxiWorldDomain.MAXPASS = 2;//rand.nextInt(max) + offset;
	int[][] ps1 = new int[][] {{11, 10}, {15, 10}};//TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy1 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy1, hf, ps1, episodes, path
		+ "one.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = 1;//rand.nextInt(max) + offset;
	int[][] ps2 = new int[][] {{5, 9}};//TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy2 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy2, hf, ps2, episodes, path
		+ "two.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = 3;//rand.nextInt(max) + offset;
	int[][] ps3 = new int[][] {{15, 4}, {7, 10}, {3, 8}};//TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy3 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy3, hf, ps3, episodes, path
		+ "three.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = 1;//rand.nextInt(max) + offset;
	int[][] ps4 = new int[][] {{9, 1}};//TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy4 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy4, hf, ps4, episodes, path
		+ "four.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;

	TaxiWorldDomain.MAXPASS = 3;//rand.nextInt(max) + offset;
	int[][] ps5 = new int[][] {{4, 6}, {1, 1}, {4, 3}};// TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy5 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy5, hf, ps5, episodes, path
		+ "five.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;
	ArrayList<PolicyBlockPolicy> toMerge = new ArrayList<PolicyBlockPolicy>();
	toMerge.add(policy1);
	toMerge.add(policy2);
	toMerge.add(policy3);
	toMerge.add(policy4);
	toMerge.add(policy5);

	long uTime = System.currentTimeMillis();
	int depth = 3;
	System.out.println("Starting union merge with depth " + depth + ".");
	List<Entry<AbstractedPolicy, Double>> merged = AbstractedPolicy
		.unionMerge(hf, toMerge, depth);
	System.out
		.println("Finished union merge; took "
			+ ((System.currentTimeMillis() - uTime) / 60000.0)
			+ " minutes");

	System.out.println(merged.size() + " options generated.");
	List<AbstractedOption> ops = new ArrayList<AbstractedOption>();
	int numOptions = 1;
	// TODO maybe add a heuristic for only letting in options that score above a threshold (e.g. >= 0.3)
	for (int i = 0; i < numOptions; i++) {
	    System.out.println("Option number " + (i + 1) + " of size "
		    + merged.get(i).getKey().size() + " and score "
		    + merged.get(i).getValue() + " added.");
	    ops.add(new AbstractedOption(hf,
		    merged.get(i).getKey().abstractedPolicy, "" + i));
	}

	TaxiWorldDomain.MAXPASS = 1;
	new TaxiWorldDomain().generateDomain();
	int[][] ps6 = new int[][] {{8, 3}};//TaxiWorldDomain.getRandomSpots(TaxiWorldDomain.MAXPASS);
	PolicyBlockPolicy policy6 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy6, hf, ps6, ops, episodes,
		path + "pmodal.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);
	c++;
	// TODO Experiments: Q-learning; Find what number of options is optimal;
	// Find what number of source policies is optimal; SARSA(\); Random
	// Option; Hand Crafted Option; Flat Policy Option

	PolicyBlockPolicy policy7 = new PolicyBlockPolicy(epsilon);
	System.out.println("Starting policy " + c + ": MAXPASS="
		+ TaxiWorldDomain.MAXPASS);
	System.out.println(runTaxiLearning(policy7, hf, ps6, episodes, path
		+ "qlearn.csv")[episodes - 1]);
	System.out.println("Finished policy: " + c);

	System.out.println("Experiment finished. Took a total of "
		+ ((System.currentTimeMillis() - startTime) / 60000.0)
		+ " minutes.");
    }
}
