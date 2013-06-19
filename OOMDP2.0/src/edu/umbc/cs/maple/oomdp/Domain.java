package edu.umbc.cs.maple.oomdp;


import java.util.*;

public class Domain {
	
	
	private List <ObjectClass>						objectClasses_;			//list of object classes
	private Map <String, ObjectClass>				objectClassMap_;		//look up object classes by name
	
	private List <Attribute>						attributes_;			//list of attributes
	private Map <String, Attribute>					attributeMap_;			//lookup attributes by name
	
	private List <PropositionalFunction>			propFunctions_;			//list of propositional functions
	private Map <String, PropositionalFunction> 	propFunctionMap_;		//lookup propositional functions by name
	
	private List <Action>							actions_;				//list of actions
	private Map <String, Action>					actionMap_;				//lookup actions by name

	
	
	public Domain(){
		
		objectClasses_ = new ArrayList <ObjectClass>();
		objectClassMap_ = new HashMap <String, ObjectClass>();
		
		attributes_ = new ArrayList <Attribute>();
		attributeMap_ = new HashMap <String, Attribute>();
		
		propFunctions_ = new ArrayList <PropositionalFunction>();
		propFunctionMap_ = new HashMap <String, PropositionalFunction>();
		
		actions_ = new ArrayList <Action>();
		actionMap_ = new HashMap <String, Action>();

		
	}
	
	
	public void addObjectClass(ObjectClass oc){
		if(!objectClassMap_.containsKey(oc.name_)){
			objectClasses_.add(oc);
			objectClassMap_.put(oc.name_, oc);
		}
	}
	
	public void addAttribute(Attribute att){
		if(!attributeMap_.containsKey(att.name_)){
			attributes_.add(att);
			attributeMap_.put(att.name_, att);
		}
	}
	
	public void addPropositionalFunction(PropositionalFunction prop){
		if(!propFunctionMap_.containsKey(prop.getName())){
			propFunctions_.add(prop);
			propFunctionMap_.put(prop.getName(), prop);
		}
	}
	
	
	public void addAction(Action act){
		if(!actionMap_.containsKey(act.getName())){
			actions_.add(act);
			actionMap_.put(act.getName(), act);
		}
	}


	
	
	public List <ObjectClass> getObjectClasses(){
		return new ArrayList <ObjectClass>(objectClasses_);
	}
	
	public ObjectClass getObjectClass(String name){
		return objectClassMap_.get(name);
	}
	
	
	public List <Attribute> getAttributes(){
		return new ArrayList <Attribute>(attributes_);
	}
	
	public Attribute getAttribute(String name){
		return attributeMap_.get(name);
	}
	
	
	public List <PropositionalFunction> getPropFunctions(){
		return new ArrayList <PropositionalFunction>(propFunctions_);
	}
	
	public PropositionalFunction getPropFunction(String name){
		return propFunctionMap_.get(name);
	}
	
	
	public List <Action> getActions(){
		return new ArrayList <Action>(actions_);
	}
	
	public Action getAction(String name){
		return actionMap_.get(name);
	}
	

	// Maps propFuncClass -> propList
	// eg: color -> isWhite, isBlue, isYellow...
	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsMap() {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions_) {
			for(String paramClass : pf.getParameterClasses()) {
				String propFuncClass = pf.getClassName();
				Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
				if(propList == null) {
					propList = new HashSet<PropositionalFunction>();
				}

				propList.add(pf);
				propFuncs.put(propFuncClass, propList);
			}
		}
		return propFuncs;
	}

	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsFromObjectClass(String objectName) {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions_) {
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
