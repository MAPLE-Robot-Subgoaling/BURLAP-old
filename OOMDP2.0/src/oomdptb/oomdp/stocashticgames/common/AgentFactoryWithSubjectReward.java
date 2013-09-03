package oomdptb.oomdp.stocashticgames.common;

import oomdptb.oomdp.stocashticgames.Agent;
import oomdptb.oomdp.stocashticgames.AgentFactory;
import oomdptb.oomdp.stocashticgames.JointReward;

public class AgentFactoryWithSubjectReward implements AgentFactory {

	protected AgentFactory			baseFactory;
	protected JointReward			internalReward;
	
	public AgentFactoryWithSubjectReward(AgentFactory baseFactory, JointReward internalReward) {
		this.baseFactory = baseFactory;
		this.internalReward = internalReward;
	}

	@Override
	public Agent generateAgent() {
		Agent a = baseFactory.generateAgent();
		a.setInternalRewardFunction(internalReward);
		return a;
	}

}
