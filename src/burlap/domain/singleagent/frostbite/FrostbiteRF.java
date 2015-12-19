package burlap.domain.singleagent.frostbite;

import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

/**
 * @author Phillipe Morere
 */
public class FrostbiteRF implements RewardFunction {

	public double goalReward = 1000.0;
	public double lostReward = -1000.0;
	public double activatedPlatformReward = 10.0;
	public double defaultReward = -1.0;
	private PropositionalFunction onIce;
	private PropositionalFunction inWater;
	private PropositionalFunction iglooBuilt;

	public FrostbiteRF(Domain domain) {
		this.inWater = domain.getPropFunction(FrostbiteDomain.PFINWATER);
		this.onIce = domain.getPropFunction(FrostbiteDomain.PFONICE);
		this.iglooBuilt = domain.getPropFunction(FrostbiteDomain.PFIGLOOBUILT);
	}

	private int numberPlatformsActive(State s) {
		List<ObjectInstance> platforms = s
				.getObjectsOfClass(FrostbiteDomain.PLATFORMCLASS);
		int nb = 0;
		for (ObjectInstance platform : platforms)
			if (platform
					.getBooleanValForAttribute(FrostbiteDomain.ACTIVATEDATTNAME))
				nb++;
		return nb;
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		if (inWater.somePFGroundingIsTrue(sprime))
			return lostReward;
		if (iglooBuilt.somePFGroundingIsTrue(sprime)
				&& onIce.somePFGroundingIsTrue(s))
			return goalReward;
		if (numberPlatformsActive(s) != numberPlatformsActive(sprime))
			return activatedPlatformReward;
		return defaultReward;
	}

}
