package edu.umbc.cs.maple.oomdp;


import java.util.*;

public class Domain {
	
	
	private List <ObjectClass>						objectClasses;			//list of object classes
	private Map <String, ObjectClass>				objectClassMap;		//look up object classes by name
	
	private List <Attribute>						attributes;			//list of attributes
	private Map <String, Attribute>					attributeMap;			//lookup attributes by name
	
	private List <PropositionalFunction>			propFunctions;			//list of propositional functions
	private Map <String, PropositionalFunction> 	propFunctionMap;		//lookup propositional functions by name
	
	private List <Action>							actions;				//list of actions
	private Map <String, Action>					actionMap;				//lookup actions by name

	
	
	public Domain(){
		
		objectClasses = new ArrayList <ObjectClass>();
		objectClassMap = new HashMap <String, ObjectClass>();
		
		attributes = new ArrayList <Attribute>();
		attributeMap = new HashMap <String, Attribute>();
		
		propFunctions = new ArrayList <PropositionalFunction>();
		propFunctionMap = new HashMap <String, PropositionalFunction>();
		
		actions = new ArrayList <Action>();
		actionMap = new HashMap <String, Action>();

		
	}
	
	public void addObjectClass(ObjectClass oc){
		if(!objectClassMap.containsKey(oc.name_)){
			objectClasses.add(oc);
			objectClassMap.put(oc.name_, oc);
		}
	}
	
	public void addAttribute(Attribute att){
		if(!attributeMap.containsKey(att.name)){
			attributes.add(att);
			attributeMap.put(att.name, att);
		}
	}
	
	public void addPropositionalFunction(PropositionalFunction prop){
		if(!propFunctionMap.containsKey(prop.getName())){
			propFunctions.add(prop);
			propFunctionMap.put(prop.getName(), prop);
		}
	}
	
	
	public void addAction(Action act){
		if(!actionMap.containsKey(act.getName())){
			actions.add(act);
			actionMap.put(act.getName(), act);
		}
	}
	
	
	public List <ObjectClass> getObjectClasses(){
		return new ArrayList <ObjectClass>(objectClasses);
	}
	
	public ObjectClass getObjectClass(String name){
		return objectClassMap.get(name);
	}
	
	
	public List <Attribute> getAttributes(){
		return new ArrayList <Attribute>(attributes);
	}
	
	public Attribute getAttribute(String name){
		return attributeMap.get(name);
	}
	
	
	public List <PropositionalFunction> getPropFunctions(){
		return new ArrayList <PropositionalFunction>(propFunctions);
	}
	
	public PropositionalFunction getPropFunction(String name){
		return propFunctionMap.get(name);
	}
	
	
	public List <Action> getActions(){
		return new ArrayList <Action>(actions);
	}
	
	public Action getAction(String name){
		return actionMap.get(name);
	}
	

	// Maps propFuncClass -> propList
	// eg: color -> isWhite, isBlue, isYellow...
	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsMap() {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions) {

			String propFuncClass = pf.getClassName();
			Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
			if(propList == null) {
				propList = new HashSet<PropositionalFunction>();
			}

			propList.add(pf);
			propFuncs.put(propFuncClass, propList);
			
		}
		return propFuncs;
	}

	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsFromObjectClass(String objectName) {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions) {
			for(String paramClass : pf.getParameterClasses()) {
				if(paramClass.equals(objectName)) {
					String propFuncClass = pf.getClassName();
					Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
					if(propList == null) {
						propList = new HashSet<PropositionalFunction>();
					}

					propList.add(pf);
					propFuncs.put(propFuncClass, propList);
				}
			}
		}
		return propFuncs;
	}

}
