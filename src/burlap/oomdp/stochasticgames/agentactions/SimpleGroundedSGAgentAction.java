package burlap.oomdp.stochasticgames.agentactions;

/**
 * A {@link burlap.oomdp.stochasticgames.agentactions.GroundedSGAgentAction}
 * implementation for actions that are parameter-less.
 * 
 * @author James MacGlashan.
 */
public class SimpleGroundedSGAgentAction extends GroundedSGAgentAction {

	public SimpleGroundedSGAgentAction(String actingAgent, SGAgentAction a) {
		super(actingAgent, a);
	}

	@Override
	public GroundedSGAgentAction copy() {
		return new SimpleGroundedSGAgentAction(this.actingAgent, this.action);
	}

	@Override
	public String[] getParametersAsString() {
		return new String[0];
	}

	@Override
	public void initParamsWithStringRep(String[] params) {
		// do nothing
	}
}
