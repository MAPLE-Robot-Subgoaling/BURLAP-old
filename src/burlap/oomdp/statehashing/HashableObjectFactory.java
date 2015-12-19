package burlap.oomdp.statehashing;

import burlap.oomdp.core.objects.ObjectInstance;

public interface HashableObjectFactory {

	HashableValueFactory getValueHashFactory();

	HashableObject hashObject(ObjectInstance object);
}
