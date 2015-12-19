package burlap.behavior.singleagent.vfa;

import burlap.oomdp.core.states.State;

/**
 * Many functions approximation techniques require a fixed feature vector to
 * work and in many cases, using abstract features from the state attributes is
 * useful. This interface provides a means to take a BURLAP OO-MDP state and
 * transform it into a feature vector represented as a double array so that
 * these function approximation techniques may be used.
 * 
 * @author James MacGlashan
 * 
 */
public interface StateToFeatureVectorGenerator {

	/**
	 * Returns a feature vector represented as a double array for a given input
	 * state.
	 * 
	 * @param s
	 *            the input state to turn into a feature vector.
	 * @return the feature vector represented as a double array.
	 */
	public double[] generateFeatureVectorFrom(State s);

}
