package oomdptb.oomdp.stocashticgames.tournament.common;

import oomdptb.oomdp.StateAbstraction;
import oomdptb.oomdp.TerminalFunction;
import oomdptb.oomdp.common.NullAbstraction;
import oomdptb.oomdp.stocashticgames.JointActionModel;
import oomdptb.oomdp.stocashticgames.JointReward;
import oomdptb.oomdp.stocashticgames.SGDomain;
import oomdptb.oomdp.stocashticgames.SGStateGenerator;
import oomdptb.oomdp.stocashticgames.World;
import oomdptb.oomdp.stocashticgames.WorldGenerator;

public class ConstantWorldGenerator implements WorldGenerator {

	protected SGDomain							domain;
	protected JointActionModel 					worldModel;
	protected JointReward						jointRewardModel;
	protected TerminalFunction					tf;
	protected SGStateGenerator					initialStateGenerator;
	
	protected StateAbstraction					abstractionForAgents;
	
	
	
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg){
		this.CWGInit(domain, jam, jr, tf, sg, new NullAbstraction());
	}
	
	public ConstantWorldGenerator(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.CWGInit(domain, jam, jr, tf, sg, abstractionForAgents);
	}
	
	protected void CWGInit(SGDomain domain, JointActionModel jam, JointReward jr, TerminalFunction tf, SGStateGenerator sg, StateAbstraction abstractionForAgents){
		this.domain = domain;
		this.worldModel = jam;
		this.jointRewardModel = jr;
		this.tf = tf;
		this.initialStateGenerator = sg;
		this.abstractionForAgents = abstractionForAgents;
	}
	
	
	@Override
	public World generateWorld() {
		return new World(domain, worldModel, jointRewardModel, tf, initialStateGenerator, abstractionForAgents);
	}

}
