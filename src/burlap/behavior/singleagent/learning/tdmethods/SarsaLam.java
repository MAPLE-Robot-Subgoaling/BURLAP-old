package burlap.behavior.singleagent.learning.tdmethods;

import java.util.LinkedList;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.ValueFunctionInitialization;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * Tabular SARSA(\lambda) implementation [1]. This implementation will work
 * correctly with options [2]. The implementation can either be used for
 * learning or planning, the latter of which is performed by running many
 * learning episodes in succession. The number of episodes used for planning can
 * be determined by a threshold maximum number of episodes, or by a maximum
 * change in the Q-function threshold.
 * 
 * @author James MacGlashan
 * 
 *         <p/>
 *         1. Rummery, Gavin A., and Mahesan Niranjan. On-line Q-learning using
 *         connectionist systems. University of Cambridge, Department of
 *         Engineering, 1994. <br/>
 *         2. Sutton, Richard S., Doina Precup, and Satinder Singh.
 *         "Between MDPs and semi-MDPs: A framework for temporal abstraction in reinforcement learning."
 *         Artificial intelligence 112.1 (1999): 181-211.
 * 
 */
public class SarsaLam extends QLearning {

    /**
     * the strength of eligibility traces (0 for one step, 1 for full
     * propagation)
     */
    protected double lambda;

    /**
     * Initializes SARSA(\lambda) with 0.1 epsilon greedy policy, the same
     * Q-value initialization everywhere, and places no limit on the number of
     * steps the agent can take in an episode. By default the agent will only
     * save the last learning episode and a call to the
     * {@link #planFromState(State)} method will cause the planner to use only
     * one episode for planning; this should probably be changed to a much
     * larger value if you plan on using this algorithm as a planning algorithm.
     * 
     * @param domain
     *            the domain in which to learn
     * @param rf
     *            the reward function
     * @param tf
     *            the terminal function
     * @param gamma
     *            the discount factor
     * @param hashingFactory
     *            the state hashing factory to use for Q-lookups
     * @param qInit
     *            the initial Q-value to user everywhere
     * @param learningRate
     *            the learning rate
     * @param lambda
     *            specifies the strength of eligibility traces (0 for one step,
     *            1 for full propagation)
     */
    public SarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate, double lambda) {

	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate);
	this.sarsalamInit(lambda);

    }

    /**
     * Initializes SARSA(\lambda) with 0.1 epsilon greedy policy, the same
     * Q-value initialization everywhere. By default the agent will only save
     * the last learning episode and a call to the {@link #planFromState(State)}
     * method will cause the planner to use only one episode for planning; this
     * should probably be changed to a much larger value if you plan on using
     * this algorithm as a planning algorithm.
     * 
     * @param domain
     *            the domain in which to learn
     * @param rf
     *            the reward function
     * @param tf
     *            the terminal function
     * @param gamma
     *            the discount factor
     * @param hashingFactory
     *            the state hashing factory to use for Q-lookups
     * @param qInit
     *            the initial Q-value to user everywhere
     * @param learningRate
     *            the learning rate
     * @param maxEpisodeSize
     *            the maximum number of steps the agent will take in a learning
     *            episode for the agent stops trying.
     * @param lambda
     *            specifies the strength of eligibility traces (0 for one step,
     *            1 for full propagation)
     */
    public SarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate, int maxEpisodeSize, double lambda) {

	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		maxEpisodeSize);
	this.sarsalamInit(lambda);

    }

    /**
     * Initializes SARSA(\lambda) with the same Q-value initialization
     * everywhere. Note that if the provided policy is derived from the Q-value
     * of this learning agent (as it should be), you may need to set the policy
     * to point to this object after call this constructor; the constructor will
     * not do this automatically in case it was by design to use the policy that
     * was learned in some other domain. By default the agent will only save the
     * last learning episode and a call to the {@link #planFromState(State)}
     * method will cause the planner to use only one episode for planning; this
     * should probably be changed to a much larger value if you plan on using
     * this algorithm as a planning algorithm.
     * 
     * @param domain
     *            the domain in which to learn
     * @param rf
     *            the reward function
     * @param tf
     *            the terminal function
     * @param gamma
     *            the discount factor
     * @param hashingFactory
     *            the state hashing factory to use for Q-lookups
     * @param qInit
     *            the initial Q-value to user everywhere
     * @param learningRate
     *            the learning rate
     * @param learningPolicy
     *            the learning policy to follow during a learning episode.
     * @param maxEpisodeSize
     *            the maximum number of steps the agent will take in a learning
     *            episode for the agent stops trying.
     * @param lambda
     *            specifies the strength of eligibility traces (0 for one step,
     *            1 for full propagation)
     */
    public SarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory, double qInit,
	    double learningRate, Policy learningPolicy, int maxEpisodeSize,
	    double lambda) {

	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		learningPolicy, maxEpisodeSize);
	this.sarsalamInit(lambda);

    }

    /**
     * Initializes SARSA(\lambda). Note that if the provided policy is derived
     * from the Q-value of this learning agent (as it should be), you may need
     * to set the policy to point to this object after call this constructor;
     * the constructor will not do this automatically in case it was by design
     * to use the policy that was learned in some other domain. By default the
     * agent will only save the last learning episode and a call to the
     * {@link #planFromState(State)} method will cause the planner to use only
     * one episode for planning; this should probably be changed to a much
     * larger value if you plan on using this algorithm as a planning algorithm.
     * 
     * @param domain
     *            the domain in which to learn
     * @param rf
     *            the reward function
     * @param tf
     *            the terminal function
     * @param gamma
     *            the discount factor
     * @param hashingFactory
     *            the state hashing factory to use for Q-lookups
     * @param qInit
     *            a
     *            {@link burlap.behavior.singleagent.ValueFunctionInitialization}
     *            object that can be used to initialize the Q-values.
     * @param learningRate
     *            the learning rate
     * @param learningPolicy
     *            the learning policy to follow during a learning episode.
     * @param maxEpisodeSize
     *            the maximum number of steps the agent will take in a learning
     *            episode for the agent stops trying.
     * @param lambda
     *            specifies the strength of eligibility traces (0 for one step,
     *            1 for full propagation)
     */
    public SarsaLam(Domain domain, RewardFunction rf, TerminalFunction tf,
	    double gamma, StateHashFactory hashingFactory,
	    ValueFunctionInitialization qInit, double learningRate,
	    Policy learningPolicy, int maxEpisodeSize, double lambda) {

	super(domain, rf, tf, gamma, hashingFactory, qInit, learningRate,
		learningPolicy, maxEpisodeSize);
	this.sarsalamInit(lambda);
    }

    protected void sarsalamInit(double lambda) {
	this.lambda = lambda;
    }

    @Override
    public EpisodeAnalysis runLearningEpisodeFrom(State initialState,
	    int maxSteps) {

	EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
	maxQChangeInLastEpisode = 0.;

	StateHashTuple curState = this.stateHash(initialState);
	eStepCounter = 0;
	LinkedList<EligibilityTrace> traces = new LinkedList<SarsaLam.EligibilityTrace>();

	GroundedAction action = (GroundedAction) learningPolicy
		.getAction(curState.s);
	QValue curQ = this.getQ(curState, action);

	while (!tf.isTerminal(curState.s) && eStepCounter < maxSteps) {

	    StateHashTuple nextState = this.stateHash(action
		    .executeIn(curState.s));
	    GroundedAction nextAction = (GroundedAction) learningPolicy
		    .getAction(nextState.s);
	    QValue nextQ = this.getQ(nextState, nextAction);
	    double nextQV = nextQ.q;

	    if (tf.isTerminal(nextState.s)) {
		nextQV = 0.;
	    }

	    // manage option specifics
	    double r = 0.;
	    double discount = this.gamma;
	    if (action.action.isPrimitive()) {
		r = rf.reward(curState.s, action, nextState.s);
		eStepCounter++;
		ea.recordTransitionTo(nextState.s, action, r);
	    } else {
		Option o = (Option) action.action;
		r = o.getLastCumulativeReward();
		int n = o.getLastNumSteps();
		discount = Math.pow(this.gamma, n);
		eStepCounter += n;
		if (this.shouldDecomposeOptions) {
		    ea.appendAndMergeEpisodeAnalysis(o
			    .getLastExecutionResults());
		} else {
		    ea.recordTransitionTo(nextState.s, action, r);
		}
	    }

	    // delta
	    double delta = r + (discount * nextQV) - curQ.q;

	    // update all
	    boolean foundCurrentQTrace = false;
	    for (EligibilityTrace et : traces) {

		if (et.sh.equals(curState)) {
		    if (et.q.a.equals(action)) {
			foundCurrentQTrace = true;
			et.eligibility = 1.; // replacing traces
		    } else {
			et.eligibility = 0.; // replacing traces
		    }
		}

		double learningRate = this.learningRate.pollLearningRate(
			et.sh.s, et.q.a);

		et.q.q = et.q.q + (learningRate * et.eligibility * delta);
		et.eligibility = et.eligibility * lambda * discount;

		double deltaQ = Math.abs(et.initialQ - et.q.q);
		if (deltaQ > maxQChangeInLastEpisode) {
		    maxQChangeInLastEpisode = deltaQ;
		}

	    }

	    if (!foundCurrentQTrace) {
		// then update and add it
		double learningRate = this.learningRate.pollLearningRate(
			curQ.s, curQ.a);
		curQ.q = curQ.q + (learningRate * delta);
		EligibilityTrace et = new EligibilityTrace(curState, curQ,
			lambda * discount);

		traces.add(et);

		double deltaQ = Math.abs(et.initialQ - et.q.q);
		if (deltaQ > maxQChangeInLastEpisode) {
		    maxQChangeInLastEpisode = deltaQ;
		}

	    }

	    // move on
	    curState = nextState;
	    action = nextAction;
	    curQ = nextQ;

	}

	if (episodeHistory.size() >= numEpisodesToStore) {
	    episodeHistory.poll();
	}
	episodeHistory.offer(ea);

	return ea;
    }

    /**
     * A data structure for maintaining eligibility trace values
     * 
     * @author James MacGlashan
     * 
     */
    public static class EligibilityTrace {

	/**
	 * The eligibility value
	 */
	public double eligibility;

	/**
	 * The state for this trace
	 */
	public StateHashTuple sh;

	/**
	 * The current Q-value info for this trace (contains the action
	 * reference)
	 */
	public QValue q;

	/**
	 * The initial numeric Q-value for this trace when it was created.
	 */
	public double initialQ;

	/**
	 * Creates a new eligibility trace to track for an episode.
	 * 
	 * @param sh
	 *            the state of the trace
	 * @param q
	 *            the q-value (containing the action reference) of the trace
	 * @param elgigbility
	 *            the eligibility value
	 */
	public EligibilityTrace(StateHashTuple sh, QValue q, double elgigbility) {
	    this.sh = sh;
	    this.q = q;
	    this.eligibility = elgigbility;
	    this.initialQ = q.q;
	}

    }

}
