package oomdptb.behavior.learning.actorcritic;


import oomdptb.behavior.Policy;
import oomdptb.oomdp.Action;

public abstract class Actor extends Policy {

	public abstract void updateFromCritqique(CritiqueResult critqiue);
	public abstract void addNonDomainReferencedAction(Action a);

}
