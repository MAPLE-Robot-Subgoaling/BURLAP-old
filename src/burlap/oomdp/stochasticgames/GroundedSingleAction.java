package burlap.oomdp.stochasticgames;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;

/**
 * Provides a grounded version of a {@link SingleAction}. That is, since
 * {@link SingleAction} objects may take parameters, this class provides a
 * grounded version of the action that has specific objects bound to its
 * parameters. Additionally, this class also specifies who the acting agent is.
 * 
 * @author James MacGlashan
 * 
 */
public class GroundedSingleAction extends AbstractGroundedAction {

    /**
     * The name of the agent performing the action
     */
    public String actingAgent;

    /**
     * The single action the acting agent will be performing
     */
    public SingleAction action;

    /**
     * Initializes this object with the name of the acting agent, the
     * SingleAction reference, and the parameters used.
     * 
     * @param actingAgent
     *            the acting agent.
     * @param a
     *            the {@link SingleAction}.
     * @param p
     *            a String array specifying the parameters bound to a.
     */
    public GroundedSingleAction(String actingAgent, SingleAction a, String[] p) {
	this.init(actingAgent, a, p);
    }

    /**
     * Initializes this object with the name of the acting agent, the
     * SingleAction reference, and the parameters used.
     * 
     * @param actingAgent
     *            the acting agent.
     * @param a
     *            the {@link SingleAction}.
     * @param p
     *            a command delineated string specifying the parameters bound to
     *            a.
     */
    public GroundedSingleAction(String actingAgent, SingleAction a, String p) {

	String[] ps = null;
	if (p.equals("")) {
	    ps = new String[0];
	} else {
	    ps = p.split(",");
	}
	this.init(actingAgent, a, ps);
    }

    private void init(String actingAgent, SingleAction a, String[] p) {
	this.actingAgent = actingAgent;
	this.action = a;
	this.params = p;
    }

    /**
     * Returns a string specifying the action name and parameters used in this
     * GroundedSingleAction.
     * 
     * @return a string specifying the action name and parameters used in this
     *         GroundedSingleAction.
     */
    public String justActionString() {
	StringBuffer buf = new StringBuffer();
	buf.append(action.actionName);
	for (int i = 0; i < params.length; i++) {
	    buf.append(" ").append(params[i]);
	}

	return buf.toString();

    }

    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append(actingAgent).append(":");
	buf.append(action.actionName);
	for (int i = 0; i < params.length; i++) {
	    buf.append(" ").append(params[i]);
	}

	return buf.toString();
    }

    @Override
    public boolean equals(Object other) {

	if (this == other) {
	    return true;
	}

	if (!(other instanceof GroundedSingleAction)) {
	    return false;
	}

	GroundedSingleAction go = (GroundedSingleAction) other;
	if (!this.action.actionName.equals(go.action.actionName)) {
	    return false;
	}

	String[] rclasses = this.action.parameterOrderGroups;

	for (int i = 0; i < this.params.length; i++) {
	    String p = this.params[i];
	    String replaceClass = rclasses[i];
	    boolean foundMatch = false;
	    for (int j = 0; j < go.params.length; j++) {
		if (p.equals(go.params[j]) && replaceClass.equals(rclasses[j])) {
		    foundMatch = true;
		    break;
		}
	    }
	    if (!foundMatch) {
		return false;
	    }

	}

	return true;
    }

    @Override
    public String actionName() {
	return this.action.actionName;
    }

    @Override
    public boolean isExcutable() {
	return false;
    }

    @Override
    public State executeIn(State s) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean actionDomainIsObjectIdentifierDependent() {
	return this.action.domain.isObjectIdentifierDependent();
    }

    @Override
    public AbstractGroundedAction copy() {
	return new GroundedSingleAction(this.actingAgent, this.action,
		this.params);
    }

}
