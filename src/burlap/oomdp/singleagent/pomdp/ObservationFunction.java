package burlap.oomdp.singleagent.pomdp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * Defines the observation function and observations associated with a POMDP
 * domain ({@link burlap.oomdp.singleagent.pomdp.PODomain}). Defining the
 * observation function requires sub classing this class and implementing the
 * methods {@link #canEnumerateObservations()},
 * {@link #getAllPossibleObservations()},
 * {@link #getObservationProbability(burlap.oomdp.core.states.State, burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * ,
 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * , and
 * {@link #sampleObservation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * . <br/>
 * {@link #canEnumerateObservations()} indicates whether this object can
 * enumerate all observations and therefore implements the
 * {@link #getAllPossibleObservations()} and
 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * methods. If those methods cannot be implement because the observation space
 * is infinite (or simply too large), then the method should return false. <br/>
 * {@link #getAllPossibleObservations()} returns the list of possible POMDP
 * observations. Each observation is represented using the
 * {@link burlap.oomdp.core.states.State} interface, since observations may
 * consist of multiple observations of distinct objects in the world; however,
 * that the observation is represented with a
 * {@link burlap.oomdp.core.states.State} interface does not mean it should be
 * confused with the MDP state. If the observations cannot be enumerated, then
 * it should throw an {@link java.lang.UnsupportedOperationException}. should be
 * thrown. <br/>
 * (
 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * specifies the probability mass/density function for a given observation
 * conditioned on the MDP state and previous action selected that led to the MDP
 * state. <br/>
 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * generates a list of all possible observations with non-zero probability and
 * their associated probability mass, conditioned on the given true hidden state
 * and previously selected action that led to that state. If the observations
 * cannot be enumerated then an {@link java.lang.UnsupportedOperationException}
 * should be thrown instead. <br/>
 * {@link #sampleObservation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * samples an observation from the observation distribution conditioned on the
 * given true hidden state and previously selected action that led to the hidden
 * state. <br/>
 * <br/>
 * The
 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * and
 * {@link #sampleObservation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * methods can be trivially implemented by having them return the result of the
 * super class methods
 * {@link #getObservationProbabilitiesByEnumeration(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * and
 * {@link #sampleObservationByEnumeration(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * , respectively, provided that the {@link #getAllPossibleObservations()}
 * method is implemented. However, these methods may be computationally
 * inefficient because they require enumerating all observations as returned by
 * the
 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
 * method. Therefore, if computational speed is a concern, it would be better to
 * implement the abstract methods directly instead of having them return the
 * result of these helper methods.
 */
public abstract class ObservationFunction {

	/**
	 * A class for associating a probability with an observation. The class is
	 * pair consisting of a {@link burlap.oomdp.core.states.State} for the
	 * observation and a double for the probability.
	 */
	public class ObservationProbability {

		/**
		 * The observation represented with a
		 * {@link burlap.oomdp.core.states.State} object.
		 */
		public State observation;

		/**
		 * The probability of the observation
		 */
		public double p;

		/**
		 * Initializes.
		 * 
		 * @param observation
		 *            the observation represented with a
		 *            {@link burlap.oomdp.core.states.State} object.
		 * @param p
		 *            the probability of the observation
		 */
		public ObservationProbability(State observation, double p) {
			this.observation = observation;
			this.p = p;
		}
	}

	/**
	 * The POMDP domain with which this
	 * {@link burlap.oomdp.singleagent.pomdp.ObservationFunction} is associated.
	 */
	protected PODomain domain;

	/**
	 * Initializes and set the {@link burlap.oomdp.singleagent.pomdp.PODomain}
	 * object's {@link burlap.oomdp.singleagent.pomdp.ObservationFunction} to
	 * this object.
	 * 
	 * @param domain
	 *            the {@link burlap.oomdp.singleagent.pomdp.PODomain} with which
	 *            this
	 *            {@link burlap.oomdp.singleagent.pomdp.ObservationFunction} is
	 *            associated.
	 */
	public ObservationFunction(PODomain domain) {
		this.domain = domain;
		this.domain.setObservationFunction(this);
	}

	/**
	 * Returns whether this object can enumerate observations and implements the
	 * {@link #getAllPossibleObservations()} and
	 * {@link #getObservationProbability(burlap.oomdp.core.states.State, burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * methods.
	 * 
	 * @return True if this object can enumerate the observations; false
	 *         otherwise.
	 */
	public abstract boolean canEnumerateObservations();

	/**
	 * Returns a {@link java.util.List} containing all possible observations.
	 * Observations are represented with a class that implements the
	 * {@link State} interface, since observations may consist of multiple
	 * observations of distinct objects in the world.
	 * 
	 * @return a {@link java.util.List} of all possible observations.
	 */
	public abstract List<State> getAllPossibleObservations();

	/**
	 * Returns the observation probability mass/density function for all
	 * observations that have non-zero mass/density conditioned on the true MDP
	 * state and previous action taken that led to the state. The function is
	 * represented as a {@link java.util.List} of
	 * {@link burlap.oomdp.singleagent.pomdp.ObservationFunction.ObservationProbability}
	 * objects, which is a pair of an observation (represented by a
	 * {@link burlap.oomdp.core.states.State} and double specifying its
	 * mass/density.
	 * 
	 * @param state
	 *            the true MDP state that generated the observations
	 * @param action
	 *            the action that led to the MDP state and which generated the
	 *            observations.
	 * @return the observation probability mass/density function represented by
	 *         a {@link java.util.List} of
	 *         {@link burlap.oomdp.singleagent.pomdp.ObservationFunction.ObservationProbability}
	 *         objects.
	 */
	public abstract List<ObservationProbability> getObservationProbabilities(
			State state, GroundedAction action);

	/**
	 * A superclass method for easily implementing the
	 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method that computes observation probability distribution by enumerating
	 * all possible observations (as defined by the
	 * {@link #getAllPossibleObservations()} method) and assigning their
	 * probability according to the
	 * {@link #getObservationProbability(burlap.oomdp.core.states.State, burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method. Note that because this method requires enumerating all
	 * observations, it may be more computationally efficient to instead
	 * directly implement domain specific code for the
	 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method.
	 * 
	 * @param state
	 *            the true MDP state that generated the observations
	 * @param action
	 *            the action that led to the MDP state and which generated the
	 *            observations.
	 * @return the observation probability mass/density function represented by
	 *         a {@link java.util.List} of
	 *         {@link burlap.oomdp.singleagent.pomdp.ObservationFunction.ObservationProbability}
	 *         objects.
	 */
	protected final List<ObservationProbability> getObservationProbabilitiesByEnumeration(
			State state, GroundedAction action) {
		List<State> possibleObservations = this.getAllPossibleObservations();
		List<ObservationProbability> probs = new ArrayList<ObservationFunction.ObservationProbability>(
				possibleObservations.size());

		for (State obs : possibleObservations) {
			double p = this.getObservationProbability(obs, state, action);
			if (p != 0) {
				probs.add(new ObservationProbability(obs, p));
			}
		}

		return probs;
	}

	/**
	 * Returns the probability that an observation will be observed conditioned
	 * on the MDP state and previous action taken that led to the state.
	 * 
	 * @param observation
	 *            the observation, represented by a
	 *            {@link burlap.oomdp.core.states.State}
	 * @param state
	 *            The true MDP state that generated the observation.
	 * @param action
	 *            the action that led to the MDP state and which generated the
	 *            observation
	 * @return the probability of observing the observation.
	 */
	public abstract double getObservationProbability(State observation,
			State state, GroundedAction action);

	/**
	 * Samples an observation given the true MDP state and action taken in the
	 * previous step that led to the MDP state.
	 * 
	 * @param state
	 *            the true MDP state
	 * @param action
	 *            the action that led to the MDP state
	 * @return an observation represented with a
	 *         {@link burlap.oomdp.core.states.State}.
	 */
	public abstract State sampleObservation(State state, GroundedAction action);

	/**
	 * A superclass method for easily implementing the
	 * {@link #sampleObservation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method that samples an observation by first getting all non-zero
	 * probability observations, as returned by the
	 * {@link #getObservationProbabilities(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method, and then sampling from the enumerated distribution. Note that
	 * enumerating all observation probabilities may be computationally
	 * inefficient; therefore, it may be better to directly implement the
	 * {@link #sampleObservation(burlap.oomdp.core.states.State, burlap.oomdp.singleagent.GroundedAction)}
	 * method with efficient domain specific code.
	 * 
	 * @param state
	 *            the true MDP state
	 * @param action
	 *            the action that led to the MDP state
	 * @return an observation represented with a
	 *         {@link burlap.oomdp.core.states.State}.
	 */
	protected final State sampleObservationByEnumeration(State state,
			GroundedAction action) {
		List<ObservationProbability> obProbs = this
				.getObservationProbabilities(state, action);
		Random rand = RandomFactory.getMapped(0);
		double r = rand.nextDouble();
		double sumProb = 0.;
		for (ObservationProbability op : obProbs) {
			sumProb += op.p;
			if (r < sumProb) {
				return op.observation;
			}
		}

		throw new RuntimeException(
				"Could not sample observaiton because observation probabilities did not sum to 1; they summed to "
						+ sumProb);
	}

}
