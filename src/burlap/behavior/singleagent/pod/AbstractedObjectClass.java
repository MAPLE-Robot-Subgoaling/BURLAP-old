package burlap.behavior.singleagent.pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;

/**
 * This class extends the object class framework in BURLAP to allow for nullable
 * attributes. That is, attributes that may be unset, allowing for abstraction
 * based on attributes without running into schema issues and null pointer
 * exceptions.
 * 
 * @author Nicholas Haltmeyer
 */
public class AbstractedObjectClass extends ObjectClass {
    /**
     * This is a mapping of attribute name to reference to keep track of what
     * attributes are currently turned off
     */
    protected Map<String, Attribute> nilMap;

    public AbstractedObjectClass(Domain domain, String name) {
	super(domain, name);
	nilMap = new HashMap<String, Attribute>();
    }

    public AbstractedObjectClass(Domain domain, String name, boolean hidden) {
	super(domain, name, hidden);
	nilMap = new HashMap<String, Attribute>();
    }

    public AbstractedObjectClass(Domain domain, String name, boolean hidden,
	    ObjectClass o1, ObjectClass o2) {
	this(domain, name, hidden);
	Set<Attribute> tempSet = new HashSet<Attribute>();
	tempSet.addAll(o1.attributeList);
	tempSet.addAll(o2.attributeList);
	super.setAttributes(new ArrayList<Attribute>(tempSet));
	tempSet.clear();

	// Set the hidden attributes
	// Find the non-intersection (union - intersection)
	for (Attribute a1 : o1.attributeList) {
	    if (o2.attributeList.contains(a1)) {
		continue;
	    }
	    toggleAttribute(a1, false);
	}
	for (Attribute a2 : o2.attributeList) {
	    if (o1.attributeList.contains(a2)) {
		continue;
	    }
	    toggleAttribute(a2, false);
	}
    }

    @Override
    public AbstractedObjectClass copy(Domain newDomain) {
	AbstractedObjectClass ret = new AbstractedObjectClass(newDomain, name,
		hidden);
	for (Entry<String, Attribute> e : nilMap.entrySet()) {
	    ret.nilMap.put(e.getKey(), e.getValue());
	}
	ret.setAttributes(attributeList);

	return ret;
    }

    public void toggleAttribute(Attribute attribute, boolean on) {
	toggleAttribute(attribute.name, on);
    }

    public void toggleAttribute(String attribute, boolean on) {
	if (on) {
	    if (nilMap.get(attribute) == null) {
		// If the attribute doesn't exist in the null mapping (it's not
		// turned off)
		if (!super.hasAttribute(attribute)) {
		    throw new IllegalArgumentException(
			    "Attribute does not exist in this class schema.");
		}

		return;
	    }

	    // Toggling the attribute back on
	    super.addAttribute(nilMap.get(attribute));
	    nilMap.remove(attribute);
	} else {
	    if (!super.hasAttribute(attribute)) {
		throw new IllegalArgumentException(
			"Attribute does not exist in this class schema.");
	    }

	    // Turning the attribute off
	    nilMap.put(attribute, super.getAttribute(attribute));
	    List<Attribute> tempAtts = super.attributeList;
	    tempAtts.remove(super.getAttribute(attribute));
	    super.setAttributes(tempAtts);
	}
    }

    public boolean isAttributeOn(Attribute attribute) {
	return isAttributeOn(attribute.name);
    }

    public boolean isAttributeOn(String attribute) {
	return nilMap.get(attribute) == null ? true : false;
    }
}
