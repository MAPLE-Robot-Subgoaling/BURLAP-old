package burlap.behavior.singleagent.learning.tdmethods;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import burlap.behavior.learningrate.ConstantLR;
import burlap.behavior.learningrate.LearningRate;
import burlap.behavior.policy.EpsilonGreedy;
import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.MDPSolver;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.options.Option;
import burlap.behavior.singleagent.options.support.EnvironmentOptionOutcome;
import burlap.behavior.singleagent.planning.Planner;
import burlap.behavior.valuefunction.QFunction;
import burlap.behavior.valuefunction.QValue;
import burlap.behavior.valuefunction.ValueFunctionInitialization;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.environment.Environment;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;
import burlap.oomdp.singleagent.environment.SimulatedEnvironment;
import burlap.oomdp.statehashing.HashableState;
import burlap.oomdp.statehashing.HashableStateFactory;

/**
 * Tabular Q-learning algorithm [1]. This implementation will work correctly
 * with Options [2]. The implementation can either be used for learning or
 * planning, the latter of which is performed by running many learning episodes
 * in succession in a
 * {@link burlap.oomdp.singleagent.environment.SimulatedEnvironment}. If you are
 * going to use this algorithm for planning, call the
 * {@link #initializeForPlanning(burlap.oomdp.singleagent.RewardFunction, burlap.oomdp.core.TerminalFunction, int)}
 * method before calling {@link #planFromState(burlap.oomdp.core.states.State)}.
 * The number of episodes used for planning can be determined by a threshold
 * maximum number of episodes, or by a maximum change in the Q-function
 * threshold. <br/>
 * <br/>
 * By default, this agent will use an epsilon-greedy policy with epsilon=0.1.
 * You can change the learning policy to anything with the
 * {@link #setLearningPolicy(burlap.behavior.policy.Policy)} policy. <br/>
 * <br/>
 * If you want to use a custom learning rate decay schedule rather than a
 * constant learning rate, use the
 * {@link #setLearningRateFunction(burlap.behavior.learningrate.LearningRate)}. <br/>
 * <br/>
 * 
 * <p/>
 * 1. Watkins, Christopher JCH, and Peter Dayan. "Q-learning." Machine learning
 * 8.3-4 (1992): 279-292. <br/>
 * 2. Sutton, Richard S., Doina Precup, and Satinder Singh.
 * "Between MDPs and semi-MDPs: A framework for temporal abstraction in reinforcement learning."
 * Artificial intelligence 112.1 (1999): 181-211.
 * 
 * @author James MacGlashan
 * 
 */
public class QLearning extends MDPSolver implements QFunction, LearningAgent,
		Planner {

	/**
	 * The tabular mapping from states to Q-values
	 */
	protected Map<HashableState, QLearningStateNode> qIndex;

	/**
	 * The object that defines how Q-values are initialized.
	 */
	protected ValueFunctionInitialization qInitFunction;

	/**
	 * The learning rate function used.
	 */
	protected LearningRate learningRate;

	/**
	 * The learning policy to use. Typically these will be policies that link
	 * back to this object so that they change as the Q-value estimate change.
	 */
	protected Policy learningPolicy;

	/**
	 * The maximum number of steps that will be taken in an episode before the
	 * agent terminates a learning episode
	 */
	protected int maxEpisodeSize;

	/**
	 * A counter for counting the number of steps in an episode that have been
	 * taken thus far
	 */
	protected int eStepCounter;

	/**
	 * The maximum number of episodes to use for planning
	 */
	protected int numEpisodesForPlanning;

	/**
	 * The maximum allowable change in the Q-function during an episode before
	 * the planning method terminates.
	 */
	protected double maxQChangeForPlanningTermination;

	/**
	 * The maximum Q-value change that occurred in the last learning episode.
	 */
	protected double maxQChangeInLastEpisode = Double.POSITIVE_INFINITY;

	/**
	 * the saved previous learning episodes
	 */
	protected LinkedList<EpisodeAnalysis> episodeHistory;

	/**
	 * The number of the most recent learning episodes to store.
	 */
	protected int numEpisodesToStore;

	/**
	 * Whether options should be decomposed into actions in the returned
	 * {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean shouldDecomposeOptions = true;

	/**
	 * Whether decomposed options should have their primitive actions annotated
	 * with the options name in the returned
	 * {@link burlap.behavior.singleagent.EpisodeAnalysis} objects.
	 */
	protected boolean shouldAnnotateOptions = true;

	/**
	 * The total number of learning steps performed by this agent.
	 */
	protected int totalNumberOfSteps = 0;

	/**
	 * Initializes Q-learning with 0.1 epsilon greedy policy, the same Q-value
	 * initialization everywhere, and places no limit on the number of steps the
	 * agent can take in an episode. By default the agent will only save the
	 * last learning episode and a call to the {@link #planFromState(State)}
	 * method will cause the valueFunction to use only one episode for planning;
	 * this should probably be changed to a much larger value if you plan on
	 * using this algorithm as a planning algorithm.
	 * 
	 * @param domain
	 *            the domain in which to learn
	 * @param gamma
	 *            the discount factor
	 * @param hashingFactory
	 *            the state hashing factory to use for Q-lookups
	 * @param qInit
	 *            the initial Q-value to user everywhere
	 * @param learningRate
	 *            the learning rate
	 */
	public QLearning(Domain domain, double gamma,
			HashableStateFactory hashingFactory, double qInit,
			double learningRate) {
		this.QLInit(
				domain,
				gamma,
				hashingFactory,
				new ValueFunctionInitialization.ConstantValueFunctionInitialization(
						qInit), learningRate, new EpsilonGreedy(this, 0.1),
				Integer.MAX_VALUE);
	}

	/**
	 * Initializes Q-learning with 0.1 epsilon greedy policy, the same Q-value
	 * initialization everywhere. By default the agent will only save the last
	 * learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the valueFunction to use only one episode for planning; this
	 * should probably be changed to a much larger value if you plan on using
	 * this algorithm as a planning algorithm.
	 * 
	 * @param domain
	 *            the domain in which to learn
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
	 */
	public QLearning(Domain domain, double gamma,
			HashableStateFactory hashingFactory, double qInit,
			double learningRate, int maxEpisodeSize) {
		this.QLInit(
				domain,
				gamma,
				hashingFactory,
				new ValueFunctionInitialization.ConstantValueFunctionInitialization(
						qInit), learningRate, new EpsilonGreedy(this, 0.1),
				maxEpisodeSize);
	}

	/**
	 * Initializes the same Q-value initialization everywhere. Note that if the
	 * provided policy is derived from the Q-value of this learning agent (as it
	 * should be), you may need to set the policy to point to this object after
	 * call this constructor; the constructor will not do this automatically in
	 * case it was by design to use the policy that was learned in some other
	 * domain. By default the agent will only save the last learning episode and
	 * a call to the {@link #planFromState(State)} method will cause the
	 * valueFunction to use only one episode for planning; this should probably
	 * be changed to a much larger value if you plan on using this algorithm as
	 * a planning algorithm.
	 * 
	 * @param domain
	 *            the domain in which to learn
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
	 */
	public QLearning(Domain domain, double gamma,
			HashableStateFactory hashingFactory, double qInit,
			double learningRate, Policy learningPolicy, int maxEpisodeSize) {
		this.QLInit(
				domain,
				gamma,
				hashingFactory,
				new ValueFunctionInitialization.ConstantValueFunctionInitialization(
						qInit), learningRate, learningPolicy, maxEpisodeSize);
	}

	/**
	 * Initializes the algorithm. Note that if the provided policy is derived
	 * from the Q-value of this learning agent (as it should be), you may need
	 * to set the policy to point to this object after call this constructor;
	 * the constructor will not do this automatically in case it was by design
	 * to use the policy that was learned in some other domain. By default the
	 * agent will only save the last learning episode and a call to the
	 * {@link #planFromState(State)} method will cause the valueFunction to use
	 * only one episode for planning; this should probably be changed to a much
	 * larger value if you plan on using this algorithm as a planning algorithm.
	 * 
	 * @param domain
	 *            the domain in which to learn
	 * @param gamma
	 *            the discount factor
	 * @param hashingFactory
	 *            the state hashing factory to use for Q-lookups
	 * @param qInit
	 *            a
	 *            {@link burlap.behavior.valuefunction.ValueFunctionInitialization}
	 *            object that can be used to initialize the Q-values.
	 * @param learningRate
	 *            the learning rate
	 * @param learningPolicy
	 *            the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize
	 *            the maximum number of steps the agent will take in a learning
	 *            episode for the agent stops trying.
	 */
	public QLearning(Domain domain, double gamma,
			HashableStateFactory hashingFactory,
			ValueFunctionInitialization qInit, double learningRate,
			Policy learningPolicy, int maxEpisodeSize) {
		this.QLInit(domain, gamma, hashingFactory, qInit, learningRate,
				learningPolicy, maxEpisodeSize);
	}

	public List<EpisodeAnalysis> getAllStoredLearningEpisodes() {
		return episodeHistory;
	}

	public EpisodeAnalysis getLastLearningEpisode() {
		return episodeHistory.getLast();
	}

	/**
	 * Returns the number of steps taken in the last episode;
	 * 
	 * @return the number of steps taken in the last episode;
	 */
	public int getLastNumSteps() {
		return eStepCounter;
	}

	/**
	 * Returns the maximum Q-value in the hashed stated.
	 * 
	 * @param s
	 *            the state for which to get he maximum Q-value;
	 * @return the maximum Q-value in the hashed stated.
	 */
	protected double getMaxQ(HashableState s) {
		List<QValue> qs = this.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for (QValue q : qs) {
			if (q.q > max) {
				max = q.q;
			}
		}
		return max;
	}

	/**
	 * Returns the Q-value for a given hashed state and action.
	 * 
	 * @param s
	 *            the hashed state
	 * @param a
	 *            the action
	 * @return the Q-value for a given hashed state and action; null is returned
	 *         if there is not Q-value currently stored.
	 */
	protected QValue getQ(HashableState s, GroundedAction a) {
		QLearningStateNode node = this.getStateNode(s);

		a = (GroundedAction) AbstractObjectParameterizedGroundedAction.Helper
				.translateParameters(a, s.s, node.s.s);

		// legacy method
		// if(a.params.length > 0 && !this.domain.isObjectIdentifierDependent()
		// && a.parametersAreObjects()){
		// Map<String, String> matching = s.s.getObjectMatchingTo(node.s.s,
		// false);
		// a = this.translateAction(a, matching);
		// }

		for (QValue qv : node.qEntry) {
			if (qv.a.equals(a)) {
				return qv;
			}
		}

		return null; // no action for this state indexed
	}

	@Override
	public QValue getQ(State s, AbstractGroundedAction a) {
		return this.getQ(this.stateHash(s), (GroundedAction) a);
	}

	/**
	 * Returns the possible Q-values for a given hashed stated.
	 * 
	 * @param s
	 *            the hashed state for which to get the Q-values.
	 * @return the possible Q-values for a given hashed stated.
	 */
	protected List<QValue> getQs(HashableState s) {
		QLearningStateNode node = this.getStateNode(s);
		return node.qEntry;
	}

	@Override
	public List<QValue> getQs(State s) {
		return this.getQs(this.stateHash(s));
	}

	/**
	 * Returns the {@link QLearningStateNode} object stored for the given hashed
	 * state. If no {@link QLearningStateNode} object. is stored, then it is
	 * created and has its Q-value initialize using this objects
	 * {@link burlap.behavior.valuefunction.ValueFunctionInitialization} data
	 * member.
	 * 
	 * @param s
	 *            the hashed state for which to get the
	 *            {@link QLearningStateNode} object
	 * @return the {@link QLearningStateNode} object stored for the given hashed
	 *         state. If no {@link QLearningStateNode} object.
	 */
	protected QLearningStateNode getStateNode(HashableState s) {

		QLearningStateNode node = qIndex.get(s);

		if (node == null) {
			node = new QLearningStateNode(s);
			List<GroundedAction> gas = this.getAllGroundedActions(s.s);
			if (gas.size() == 0) {
				gas = this.getAllGroundedActions(s.s);
				throw new RuntimeErrorException(
						new Error(
								"No possible actions in this state, cannot continue Q-learning"));
			}
			for (GroundedAction ga : gas) {
				node.addQValue(ga, qInitFunction.qValue(s.s, ga));
			}

			qIndex.put(s, node);
		}

		return node;

	}

	/**
	 * Sets the {@link burlap.oomdp.singleagent.RewardFunction},
	 * {@link burlap.oomdp.core.TerminalFunction}, and the number of simulated
	 * episodes to use for planning when the
	 * {@link #planFromState(burlap.oomdp.core.states.State)} method is called.
	 * If the {@link burlap.oomdp.singleagent.RewardFunction} and
	 * {@link burlap.oomdp.core.TerminalFunction} are not set, the
	 * {@link #planFromState(burlap.oomdp.core.states.State)} method will throw
	 * a runtime exception.
	 * 
	 * @param rf
	 *            the reward function to use for planning
	 * @param tf
	 *            the terminal function to use for planning
	 * @param numEpisodesForPlanning
	 *            the number of simulated episodes to run for planning.
	 */
	public void initializeForPlanning(RewardFunction rf, TerminalFunction tf,
			int numEpisodesForPlanning) {
		this.rf = rf;
		this.tf = tf;
		this.numEpisodesForPlanning = numEpisodesForPlanning;
	}

	/**
	 * Plans from the input state and then returns a
	 * {@link burlap.behavior.policy.GreedyQPolicy} that greedily selects the
	 * action with the highest Q-value and breaks ties uniformly randomly.
	 * 
	 * @param initialState
	 *            the initial state of the planning problem
	 * @return a {@link burlap.behavior.policy.GreedyQPolicy}.
	 */
	@Override
	public GreedyQPolicy planFromState(State initialState) {

		if (this.rf == null || this.tf == null) {
			throw new RuntimeException(
					"QLearning (and its subclasses) cannot execute planFromState because the reward function and/or terminal function for planning have not been set. Use the initializeForPlanning method to set them.");
		}

		SimulatedEnvironment env = new SimulatedEnvironment(this.domain,
				this.rf, this.tf, initialState);

		int eCount = 0;
		do {
			this.runLearningEpisode(env, this.maxEpisodeSize);
			eCount++;
		} while (eCount < numEpisodesForPlanning
				&& maxQChangeInLastEpisode > maxQChangeForPlanningTermination);

		return new GreedyQPolicy(this);

	}

	/**
	 * Initializes the algorithm. By default the agent will only save the last
	 * learning episode and a call to the {@link #planFromState(State)} method
	 * will cause the valueFunction to use only one episode for planning; this
	 * should probably be changed to a much larger value if you plan on using
	 * this algorithm as a planning algorithm.
	 * 
	 * @param domain
	 *            the domain in which to learn
	 * @param gamma
	 *            the discount factor
	 * @param hashingFactory
	 *            the state hashing factory to use for Q-lookups
	 * @param qInitFunction
	 *            a
	 *            {@link burlap.behavior.valuefunction.ValueFunctionInitialization}
	 *            object that can be used to initialize the Q-values.
	 * @param learningRate
	 *            the learning rate
	 * @param learningPolicy
	 *            the learning policy to follow during a learning episode.
	 * @param maxEpisodeSize
	 *            the maximum number of steps the agent will take in a learning
	 *            episode for the agent stops trying.
	 */
	protected void QLInit(Domain domain, double gamma,
			HashableStateFactory hashingFactory,
			ValueFunctionInitialization qInitFunction, double learningRate,
			Policy learningPolicy, int maxEpisodeSize) {

		this.solverInit(domain, null, null, gamma, hashingFactory);
		this.qIndex = new HashMap<HashableState, QLearningStateNode>();
		this.learningRate = new ConstantLR(learningRate);
		this.learningPolicy = learningPolicy;
		this.maxEpisodeSize = maxEpisodeSize;
		this.qInitFunction = qInitFunction;

		numEpisodesToStore = 1;
		episodeHistory = new LinkedList<EpisodeAnalysis>();

		numEpisodesForPlanning = 1;
		maxQChangeForPlanningTermination = 0.;

	}

	@Override
	public void resetSolver() {
		this.mapToStateIndex.clear();
		this.qIndex.clear();
		this.episodeHistory.clear();
		this.eStepCounter = 0;
		this.maxQChangeInLastEpisode = Double.POSITIVE_INFINITY;
	}

	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env) {
		return this.runLearningEpisode(env, -1);
	}

	@Override
	public EpisodeAnalysis runLearningEpisode(Environment env, int maxSteps) {

		this.toggleShouldAnnotateOptionDecomposition(shouldAnnotateOptions);

		State initialState = env.getCurrentObservation();

		EpisodeAnalysis ea = new EpisodeAnalysis(initialState);
		HashableState curState = this.stateHash(initialState);
		eStepCounter = 0;

		maxQChangeInLastEpisode = 0.;
		while (!env.isInTerminalState()
				&& (eStepCounter < maxSteps || maxSteps == -1)) {

			GroundedAction action = (GroundedAction) learningPolicy
					.getAction(curState.s);
			QValue curQ = this.getQ(curState, action);

			EnvironmentOutcome eo = action.executeIn(env);

			HashableState nextState = this.stateHash(eo.op);
			double maxQ = 0.;

			if (!eo.terminated) {
				maxQ = this.getMaxQ(nextState);
			}

			// manage option specifics
			double r = eo.r;
			double discount = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).discount
					: this.gamma;
			int stepInc = eo instanceof EnvironmentOptionOutcome ? ((EnvironmentOptionOutcome) eo).numSteps
					: 1;
			eStepCounter += stepInc;

			if (action.action.isPrimitive() || !this.shouldAnnotateOptions) {
				ea.recordTransitionTo(action, nextState.s, r);
			} else {
				ea.appendAndMergeEpisodeAnalysis(((Option) action.action)
						.getLastExecutionResults());
			}

			double oldQ = curQ.q;

			// update Q-value
			curQ.q = curQ.q
					+ this.learningRate.pollLearningRate(
							this.totalNumberOfSteps, curState.s, action)
					* (r + (discount * maxQ) - curQ.q);

			double deltaQ = Math.abs(oldQ - curQ.q);
			if (deltaQ > maxQChangeInLastEpisode) {
				maxQChangeInLastEpisode = deltaQ;
			}

			// move on polling environment for its current state in case it
			// changed during processing
			curState = this.stateHash(env.getCurrentObservation());
			this.totalNumberOfSteps++;

		}

		if (episodeHistory.size() >= numEpisodesToStore) {
			episodeHistory.poll();
		}
		episodeHistory.offer(ea);

		return ea;

	}

	/**
	 * Sets which policy this agent should use for learning.
	 * 
	 * @param p
	 *            the policy to use for learning.
	 */
	public void setLearningPolicy(Policy p) {
		this.learningPolicy = p;
	}

	/**
	 * Sets the learning rate function to use
	 * 
	 * @param lr
	 *            the learning rate function to use
	 */
	public void setLearningRateFunction(LearningRate lr) {
		this.learningRate = lr;
	}

	/**
	 * Sets the maximum number of episodes that will be performed when the
	 * {@link #planFromState(State)} method is called.
	 * 
	 * @param n
	 *            the maximum number of episodes that will be performed when the
	 *            {@link #planFromState(State)} method is called.
	 */
	public void setMaximumEpisodesForPlanning(int n) {
		if (n > 0) {
			this.numEpisodesForPlanning = n;
		} else {
			this.numEpisodesForPlanning = 1;
		}
	}

	/**
	 * Sets a max change in the Q-function threshold that will cause the
	 * {@link #planFromState(State)} to stop planning when it is achieved.
	 * 
	 * @param m
	 *            the maximum allowable change in the Q-function before planning
	 *            stops
	 */
	public void setMaxQChangeForPlanningTerminaiton(double m) {
		if (m > 0.) {
			this.maxQChangeForPlanningTermination = m;
		} else {
			this.maxQChangeForPlanningTermination = 0.;
		}
	}

	public void setNumEpisodesToStore(int numEps) {
		if (numEps > 0) {
			numEpisodesToStore = numEps;
		} else {
			numEpisodesToStore = 1;
		}
	}

	/**
	 * Sets how to initialize Q-values for previously unexperienced state-action
	 * pairs.
	 * 
	 * @param qInit
	 *            a
	 *            {@link burlap.behavior.valuefunction.ValueFunctionInitialization}
	 *            object that can be used to initialize the Q-values.
	 */
	public void setQInitFunction(ValueFunctionInitialization qInit) {
		this.qInitFunction = qInit;
	}

	/**
	 * Sets whether options that are decomposed into primitives will have the
	 * option that produced them and listed. The default value is true. If
	 * option decomposition is not enabled, changing this value will do nothing.
	 * When it is enabled and this is set to true, primitive actions taken by an
	 * option in EpisodeAnalysis objects will be recorded with a special action
	 * name that indicates which option was called to produce the primitive
	 * action as well as which step of the option the primitive action is. When
	 * set to false, recorded names of primitives will be only the primitive
	 * aciton's name it will be unclear which option was taken to generate it.
	 * 
	 * @param toggle
	 *            whether to annotate the primitive actions of options with the
	 *            calling option's name.
	 */
	public void toggleShouldAnnotateOptionDecomposition(boolean toggle) {
		shouldAnnotateOptions = toggle;
		for (Action a : actions) {
			if (a instanceof Option) {
				((Option) a).toggleShouldAnnotateResults(toggle);
			}
		}
	}

	/**
	 * Sets whether the primitive actions taken during an options will be
	 * included as steps in produced EpisodeAnalysis objects. The default value
	 * is true. If this is set to false, then EpisodeAnalysis objects returned
	 * from a learning episode will record options as a single "action" and the
	 * steps taken by the option will be hidden.
	 * 
	 * @param toggle
	 *            whether to decompose options into the primitive actions taken
	 *            by them or not.
	 */
	public void toggleShouldDecomposeOption(boolean toggle) {

		this.shouldDecomposeOptions = toggle;
		for (Action a : actions) {
			if (a instanceof Option) {
				((Option) a).toggleShouldRecordResults(toggle);
			}
		}
	}

	@Override
	public double value(State s) {
		return QFunction.QFunctionHelper.getOptimalValue(this, s);
	}

}
