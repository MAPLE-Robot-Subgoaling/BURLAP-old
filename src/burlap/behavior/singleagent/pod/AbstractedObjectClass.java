package burlap.behavior.singleagent.pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;

/**
 * This class extends the object class framework in BURLAP to allow for nullable attributes. That is, attributes that may be unset, allowing for abstraction based on attributes without running into schema issues and null pointer exceptions.
 * @author Nicholas Haltmeyer
 * TODO: This class isn't done yet.
 */
public class AbstractedObjectClass extends ObjectClass {
    protected List<Attribute> nilAttributeList;
    protected Map<String, Integer> nilAttributeMap;
    
    public AbstractedObjectClass(Domain domain, String name) {
	super(domain, name);
	nilAttributeList = new ArrayList<Attribute>();
	nilAttributeMap = new HashMap<String, Integer>();
    }
    
    public AbstractedObjectClass(Domain domain, String name, boolean hidden) {
	super(domain, name, hidden);
	nilAttributeList = new ArrayList<Attribute>();
	nilAttributeMap = new HashMap<String, Integer>();
    }

    public AbstractedObjectClass(ObjectClass o1, ObjectClass o2) {
	super(o1.domain, o1.name, false);
	nilAttributeList = new ArrayList<Attribute>();
	nilAttributeMap = new HashMap<String, Integer>();
    }
    
    @Override
    public AbstractedObjectClass copy(Domain newDomain) {
	return null;
    }
    
    public void toggleAttribute(Attribute attribute, boolean on) {
	
    }
    public void toggleAttribute(String attribute, boolean on) {
	
    }
    
    public boolean isAttributeOn(Attribute attribute) {
	return false;
    }
    public boolean isAttributeOn(String attribute) {
	return false;
    }
}
