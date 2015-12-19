package burlap.behavior.singleagent.learning;

/**
 * A factory interface for generating learning agents.
 * 
 * @author James MacGlashan
 * 
 */
public interface LearningAgentFactory {

	/**
	 * Will return a name to identify the kind of agent that will be generated
	 * by this factory. This is useful for enabling the
	 * {@link burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter}
	 * class to label the results for different kinds of agents that are tested.
	 * 
	 * @return a name to identify the kind of agent that will be generated
	 */
	public String getAgentName();

	/**
	 * Generates a new LearningAgent object and returns it.
	 * 
	 * @return a LearningAgent object.
	 */
	public LearningAgent generateAgent();
}
