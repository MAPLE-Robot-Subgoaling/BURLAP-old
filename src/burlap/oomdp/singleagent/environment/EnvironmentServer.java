package burlap.oomdp.singleagent.environment;

import java.util.LinkedList;
import java.util.List;

import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

/**
 * A server that delegates all
 * {@link burlap.oomdp.singleagent.environment.Environment} interactions and
 * request to a provided
 * {@link burlap.oomdp.singleagent.environment.Environment} delegate. This class
 * will also intercept all interactions through the
 * {@link #executeAction(burlap.oomdp.singleagent.GroundedAction)} and
 * {@link #resetEnvironment()} methods and tell all
 * {@link burlap.oomdp.singleagent.environment.EnvironmentOutcome} instances
 * registered with this server about the event.
 * 
 * @author James MacGlashan.
 */
public class EnvironmentServer implements Environment {

	public static class StateSettableEnvironmentServer extends
			EnvironmentServer implements StateSettableEnvironment {

		public StateSettableEnvironmentServer(
				StateSettableEnvironment delegate,
				EnvironmentObserver... observers) {
			super(delegate, observers);
		}

		@Override
		public void setCurStateTo(State s) {
			((StateSettableEnvironment) this.delegate).setCurStateTo(s);
		}
	}

	/**
	 * Constructs an
	 * {@link burlap.oomdp.singleagent.environment.EnvironmentServer} or
	 * {@link burlap.oomdp.singleagent.environment.EnvironmentServer.StateSettableEnvironmentServer}
	 * , based on whether the input delegate implements
	 * {@link burlap.oomdp.singleagent.environment.StateSettableEnvironment}.
	 * 
	 * @param delegate
	 *            the delegate
	 *            {@link burlap.oomdp.singleagent.environment.Environment} for
	 *            most environment interactions.
	 * @param observers
	 *            the
	 *            {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}
	 *            objects notified of Environment events.
	 * @return an {@link burlap.oomdp.singleagent.environment.EnvironmentServer}
	 *         or
	 *         {@link burlap.oomdp.singleagent.environment.EnvironmentServer.StateSettableEnvironmentServer}
	 *         .
	 */
	public static EnvironmentServer constructor(Environment delegate,
			EnvironmentObserver... observers) {
		if (delegate instanceof StateSettableEnvironment) {
			return new StateSettableEnvironmentServer(
					(StateSettableEnvironment) delegate);
		}
		return new EnvironmentServer(delegate, observers);
	}

	/**
	 * the {@link burlap.oomdp.singleagent.environment.Environment} delegate
	 * that handles all primary
	 * {@link burlap.oomdp.singleagent.environment.Environment} functionality.
	 */
	protected Environment delegate;

	/**
	 * The {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}
	 * objects that will be notified of
	 * {@link burlap.oomdp.singleagent.environment.Environment} events.
	 */
	protected List<EnvironmentObserver> observers = new LinkedList<EnvironmentObserver>();

	public EnvironmentServer(Environment delegate,
			EnvironmentObserver... observers) {
		this.delegate = delegate;
		for (EnvironmentObserver observer : observers) {
			this.observers.add(observer);
		}
	}

	/**
	 * Adds one or more
	 * {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s
	 * 
	 * @param observers
	 *            and
	 *            {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}
	 */
	public void addObservers(EnvironmentObserver... observers) {
		for (EnvironmentObserver observer : observers) {
			this.observers.add(observer);
		}
	}

	/**
	 * Clears all
	 * {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s from
	 * this server.
	 */
	public void clearAllObservers() {
		this.observers.clear();
	}

	@Override
	public EnvironmentOutcome executeAction(GroundedAction ga) {
		EnvironmentOutcome eo = this.delegate.executeAction(ga);
		for (EnvironmentObserver observer : this.observers) {
			observer.observeEnvironmentInteraction(eo);
		}
		return eo;
	}

	@Override
	public State getCurrentObservation() {
		return this.delegate.getCurrentObservation();
	}

	/**
	 * Returns the {@link burlap.oomdp.singleagent.environment.Environment}
	 * delegate that handles all
	 * {@link burlap.oomdp.singleagent.environment.Environment} functionality
	 * 
	 * @return the {@link burlap.oomdp.singleagent.environment.Environment}
	 *         delegate
	 */
	public Environment getEnvironmentDelegate() {
		return delegate;
	}

	@Override
	public double getLastReward() {
		return this.delegate.getLastReward();
	}

	/**
	 * Returns all
	 * {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s
	 * registered with this server.
	 * 
	 * @return all
	 *         {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s
	 *         registered with this server.
	 */
	public List<EnvironmentObserver> getObservers() {
		return this.observers;
	}

	@Override
	public boolean isInTerminalState() {
		return this.delegate.isInTerminalState();
	}

	/**
	 * Removes one or more
	 * {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}s from
	 * this server.
	 * 
	 * @param observers
	 *            the
	 *            {@link burlap.oomdp.singleagent.environment.EnvironmentObserver}
	 *            s to remove.
	 */
	public void removeObservers(EnvironmentObserver... observers) {
		for (EnvironmentObserver observer : observers) {
			this.observers.remove(observer);
		}
	}

	@Override
	public void resetEnvironment() {
		this.delegate.resetEnvironment();
		for (EnvironmentObserver observer : this.observers) {
			observer.observeEnvironmentReset(this.delegate);
		}
	}

	/**
	 * Sets the {@link burlap.oomdp.singleagent.environment.Environment}
	 * delegate that handles all
	 * {@link burlap.oomdp.singleagent.environment.Environment} functionality
	 * 
	 * @param delegate
	 *            the {@link burlap.oomdp.singleagent.environment.Environment}
	 *            delegate
	 */
	public void setEnvironmentDelegate(Environment delegate) {
		this.delegate = delegate;
	}
}
