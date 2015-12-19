package burlap.behavior.singleagent.vfa.common;

import burlap.behavior.singleagent.vfa.*;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to perform linear value function approximation, either
 * for a states or state-actions (Q-values). It takes as input a
 * {@link burlap.behavior.singleagent.vfa.StateToFeatureVectorGenerator} which
 * defines the state features on which linear function approximation is
 * performed. In the case of Q-value function approximation, the state features
 * are replicated for each action with all other action's associated state
 * features set to zero, thereby allowing for unique predictions for each
 * action. <br/>
 * <br/>
 * Objects of this class are set to use either state value function
 * approximation or Q-value function approximation depending on whether the
 * method {@link #getStateValue(burlap.oomdp.core.states.State)} or
 * {@link #getStateActionValues(burlap.oomdp.core.states.State, java.util.List)}
 * is called first. If the former, then it performs state value function
 * approximation; if the latter then Q-value function approximation. Once it has
 * been set for either state or Q-value function approximation, it cannot be
 * used for the other and will throw a runtime exception if it queried for the
 * other kind of function.
 * 
 * @author James MacGlashan.
 */
public class LinearFVVFA implements ValueFunctionApproximation {

	/**
	 * The state feature vector generator used for linear value function
	 * approximation.
	 */
	protected StateToFeatureVectorGenerator fvGen;

	/**
	 * A feature index offset for each action when using Q-value function
	 * approximation.
	 */
	protected Map<GroundedAction, Integer> actionOffset = new HashMap<GroundedAction, Integer>();

	/**
	 * The function weights when performing state value function approximation.
	 */
	protected FunctionWeight[] stateWeights;

	/**
	 * The function weights when performing Q-value function approximation.
	 */
	protected FunctionWeight[] stateActionWeights;

	/**
	 * A default weight value for the functions weights.
	 */
	protected double defaultWeight = 0.0;

	/**
	 * Initializes. This object will be set to perform either state value
	 * function approximation or Q-value function approximation once a call to
	 * either {@link #getStateValue(burlap.oomdp.core.states.State)} or
	 * {@link #getStateActionValues(burlap.oomdp.core.states.State, java.util.List)}
	 * is queried. If the former method is called, first, then this object will
	 * be tasked with state value function approximation. If the latter method
	 * is called first, then this object will be tasked with state-action value
	 * function approximation.
	 * 
	 * @param fvGen
	 *            The state feature vector generator that produces the features
	 *            used for either linear state value function approximation or
	 *            Q-value function approximation.
	 * @param defaultWeightValue
	 *            The default weight value of all function weights.
	 */
	public LinearFVVFA(StateToFeatureVectorGenerator fvGen,
			double defaultWeightValue) {
		this.fvGen = fvGen;
		this.defaultWeight = defaultWeightValue;
	}

	public StateToFeatureVectorGenerator getFvGen() {
		return fvGen;
	}

	public double getDefaultWeight() {
		return defaultWeight;
	}

	@Override
	public ApproximationResult getStateValue(State s) {

		if (this.stateActionWeights != null) {
			throw new RuntimeException(
					"LinearFVVFA is already being used to predict the Q-value function; it cannot be overloaded to predict the state value function");
		}

		double[] vec = this.fvGen.generateFeatureVectorFrom(s);

		if (stateWeights == null) {
			stateWeights = new FunctionWeight[vec.length];
			for (int i = 0; i < stateWeights.length; i++) {
				stateWeights[i] = new FunctionWeight(i, this.defaultWeight);
			}
		}

		double sum = 0.;
		List<StateFeature> sfs = new ArrayList<StateFeature>(vec.length);
		List<FunctionWeight> fws = new ArrayList<FunctionWeight>(vec.length);
		for (int i = 0; i < vec.length; i++) {
			if (vec[i] != 0.) {
				sum += vec[i] * this.stateWeights[i].weightValue();
				sfs.add(new StateFeature(i, vec[i]));
				fws.add(stateWeights[i]);
			}
		}

		ApproximationResult res = new ApproximationResult(sum, sfs, fws);

		return res;
	}

	@Override
	public List<ActionApproximationResult> getStateActionValues(State s,
			List<GroundedAction> gas) {

		if (this.stateWeights != null) {
			throw new RuntimeException(
					"LinearFVVFA is already being used to predict the state value function; it cannot be overloaded to predict the Q-value function");
		}

		double[] vec = this.fvGen.generateFeatureVectorFrom(s);

		if (this.stateActionWeights == null) {

			this.stateActionWeights = new FunctionWeight[vec.length
					* gas.size()];
			for (int i = 0; i < stateActionWeights.length; i++) {
				this.stateActionWeights[i] = new FunctionWeight(i,
						this.defaultWeight);
			}
			for (int i = 0; i < gas.size(); i++) {
				GroundedAction ga = gas.get(i);
				this.actionOffset.put(ga, i);
			}

		}

		List<ActionApproximationResult> results = new ArrayList<ActionApproximationResult>(
				gas.size());
		for (GroundedAction ga : gas) {
			Integer Offset = this.actionOffset.get(ga);
			if (Offset == null) {
				// previously unseen action; expand our function weights
				this.actionOffset.put(ga, this.actionOffset.size());
				this.expandStateActionWeights(vec.length);
				Offset = this.actionOffset.size() - 1;
			}
			int si = Offset * vec.length;

			double sum = 0.;
			List<StateFeature> sfs = new ArrayList<StateFeature>(vec.length);
			List<FunctionWeight> fws = new ArrayList<FunctionWeight>(vec.length);
			for (int i = 0; i < vec.length; i++) {
				if (vec[i] != 0.) {
					sum += vec[i]
							* this.stateActionWeights[i + si].weightValue();
					sfs.add(new StateFeature(i + si, vec[i]));
					fws.add(stateActionWeights[i + si]);
				}
			}

			ApproximationResult res = new ApproximationResult(sum, sfs, fws);
			results.add(new ActionApproximationResult(ga, res));
		}

		return results;
	}

	/**
	 * Expands the state-action function weight vector by a fixed sized and
	 * initializes their value to the default weight value set for this object.
	 * 
	 * @param num
	 *            the number of function weights to add to the state-action
	 *            function weight vector
	 */
	protected void expandStateActionWeights(int num) {
		FunctionWeight[] nWeights = new FunctionWeight[this.stateActionWeights.length
				+ num];
		for (int i = 0; i < this.stateActionWeights.length; i++) {
			nWeights[i] = this.stateActionWeights[i];
		}
		for (int i = this.stateActionWeights.length; i < nWeights.length; i++) {
			nWeights[i] = new FunctionWeight(i, this.defaultWeight);
		}
	}

	@Override
	public WeightGradient getWeightGradient(
			ApproximationResult approximationResult) {

		WeightGradient wg = new WeightGradient(
				approximationResult.functionWeights.size());
		for (StateFeature sf : approximationResult.stateFeatures) {
			wg.put(sf.id, sf.value);
		}

		return wg;
	}

	@Override
	public void resetWeights() {
		if (this.stateWeights != null) {
			for (FunctionWeight fw : this.stateWeights) {
				fw.setWeight(this.defaultWeight);
			}
		}
		if (this.stateActionWeights != null) {
			for (FunctionWeight fw : this.stateActionWeights) {
				fw.setWeight(this.defaultWeight);
			}
		}
	}

	@Override
	public void setWeight(int featureId, double w) {

		if (this.stateWeights != null) {
			if (featureId >= this.stateWeights.length) {
				throw new RuntimeException(
						"Cannot set function weight for feature " + featureId
								+ ", because VFA dimensionality is only "
								+ this.stateWeights.length);
			}
			this.stateWeights[featureId].setWeight(w);
		} else if (this.stateActionWeights != null) {
			if (featureId >= this.stateActionWeights.length) {
				throw new RuntimeException(
						"Cannot set function weight for feature " + featureId
								+ ", because VFA dimensionality is only "
								+ this.stateActionWeights.length);
			}
			this.stateActionWeights[featureId].setWeight(w);
		}

		throw new RuntimeException(
				"VFA cannot set function weight, because function weights and dimensionality have not yet been initialized.");

	}

	@Override
	public FunctionWeight getFunctionWeight(int featureId) {

		if (this.stateWeights != null) {
			if (featureId >= this.stateWeights.length) {
				throw new RuntimeException(
						"Cannot return function weight for feature "
								+ featureId
								+ ", because VFA dimensionality is only "
								+ this.stateWeights.length);
			}
			return this.stateWeights[featureId];
		} else if (this.stateActionWeights != null) {
			if (featureId >= this.stateActionWeights.length) {
				throw new RuntimeException(
						"Cannot return function weight for feature "
								+ featureId
								+ ", because VFA dimensionality is only "
								+ this.stateActionWeights.length);
			}
			return this.stateActionWeights[featureId];
		}

		throw new RuntimeException(
				"VFA cannot return function weight, because function weights and dimensionality have not yet been initialized.");
	}

	@Override
	public int numFeatures() {

		if (this.stateWeights != null) {
			return this.stateWeights.length;
		}
		if (this.stateActionWeights != null) {
			return this.stateActionWeights.length;
		}

		return 0;
	}

	@Override
	public LinearFVVFA copy() {
		LinearFVVFA vfa = new LinearFVVFA(this.fvGen, this.defaultWeight);
		vfa.actionOffset = new HashMap<GroundedAction, Integer>(
				this.actionOffset);
		vfa.stateWeights = new FunctionWeight[this.stateWeights.length];
		for (int i = 0; i < this.stateWeights.length; i++) {
			vfa.stateWeights[i] = new FunctionWeight(
					this.stateWeights[i].weightId(),
					this.stateWeights[i].weightValue());
		}
		vfa.stateActionWeights = new FunctionWeight[this.stateActionWeights.length];
		for (int i = 0; i < this.stateActionWeights.length; i++) {
			vfa.stateActionWeights[i] = new FunctionWeight(
					this.stateActionWeights[i].weightId(),
					this.stateActionWeights[i].weightValue());
		}

		return vfa;
	}
}
