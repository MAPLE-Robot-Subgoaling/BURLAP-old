package burlap.oomdp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.debugtools.DPrint;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stochasticgames.agentactions.SGAgentAction;

/**
 * This is the base class for a problem domain. A problem domain consists of its
 * state variables, as defined with an OO-MDP (
 * {@link burlap.oomdp.core.Attribute}s, {@link burlap.oomdp.core.ObjectClass}s,
 * and {@link burlap.oomdp.core.PropositionalFunction}s), and the physics of the
 * world, which are typically specified with some set of action definitions. For
 * single-agent domains, the physics and actions are defined with the
 * {@link burlap.oomdp.singleagent.Action} object and for multi-agent stochastic
 * games, they are defined with
 * {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction} and a
 * {@link burlap.oomdp.stochasticgames.JointActionModel}. See the respective
 * single-agent {@link burlap.oomdp.singleagent.SADomain} and stochastic games
 * {@link burlap.oomdp.stochasticgames.SGDomain} subclasses for more information
 * on their definitions. <br/>
 * <br/>
 * Note that a {@link burlap.oomdp.core.Domain} does *not* include task
 * information, which will be defined separately with a
 * {@link burlap.oomdp.singleagent.RewardFunction} or
 * {@link burlap.oomdp.stochasticgames.JointReward}, and a
 * {@link burlap.oomdp.core.TerminalFunction}.
 * 
 * @author James MacGlashan
 */
public abstract class Domain {

	protected List<ObjectClass> objectClasses; // list of object classes
	protected Map<String, ObjectClass> objectClassMap; // look up object classes
														// by name

	protected List<Attribute> attributes; // list of attributes
	protected Map<String, Attribute> attributeMap; // lookup attributes by name

	protected List<PropositionalFunction> propFunctions; // list of
															// propositional
															// functions
	protected Map<String, PropositionalFunction> propFunctionMap; // lookup
																	// propositional
																	// functions
																	// by name

	protected boolean objectIdentifierDependentDomain = false;

	protected int debugCode = 111;

	/**
	 * Initializes the data structures for indexing the object classes,
	 * attributes, and propositional functions
	 */
	public Domain() {

		objectClasses = new ArrayList<ObjectClass>();
		objectClassMap = new HashMap<String, ObjectClass>();

		attributes = new ArrayList<Attribute>();
		attributeMap = new HashMap<String, Attribute>();

		propFunctions = new ArrayList<PropositionalFunction>();
		propFunctionMap = new HashMap<String, PropositionalFunction>();

	}

	/**
	 * Add a single agent action that defines this domain. This method will
	 * throw a runtime exception if this domain is not an instance of the single
	 * agent domain (SADomain). The action will not be added if this domain
	 * already has a instance with the same name.
	 * 
	 * @param act
	 *            the single agent action to add.
	 */
	public abstract void addAction(Action act);

	/**
	 * Add an attribute that can be used to define object classes of this
	 * domain. The attribute will not be added if this domain already has a
	 * instance with the same name.
	 * 
	 * @param att
	 *            the attribtue to add to this domain.
	 */
	public void addAttribute(Attribute att) {
		if (!attributeMap.containsKey(att.name)) {
			attributes.add(att);
			attributeMap.put(att.name, att);
			if (att.type == Attribute.AttributeType.RELATIONAL
					|| att.type == Attribute.AttributeType.MULTITARGETRELATIONAL) {
				if (!this.objectIdentifierDependentDomain) {
					DPrint.cl(
							this.debugCode,
							"Relational attribute added to domain; forcing domain flag to object identifier dependent.");
					this.objectIdentifierDependentDomain = true;
				}
			}
		}
	}

	/**
	 * Add an object class to define this domain. The class will not be added if
	 * this domain already has a instance with the same name.
	 * 
	 * @param oc
	 *            the object class to add to this domain.
	 */
	public void addObjectClass(ObjectClass oc) {
		if (!objectClassMap.containsKey(oc.name)) {
			objectClasses.add(oc);
			objectClassMap.put(oc.name, oc);
		}
	}

	/**
	 * Add a propositional function that can be used to evaluate objects that
	 * belong to object classes of this domain. The function will not be added
	 * if this domain already has a instance with the same name.
	 * 
	 * @param prop
	 *            the propositional function to add.
	 */
	public void addPropositionalFunction(PropositionalFunction prop) {
		if (!propFunctionMap.containsKey(prop.getName())) {
			propFunctions.add(prop);
			propFunctionMap.put(prop.getName(), prop);
		}
	}

	/**
	 * Add a {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}
	 * that can be executed by an agent in the game. The set of
	 * {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}s defines
	 * the set of joint actions in the stochastic domain (as the cross product).
	 * This method will throw a runtime exception if this domain is not an
	 * instance of the stochastic game domain (
	 * {@link burlap.oomdp.stochasticgames.SGDomain}). The action will not be
	 * added if this domain already has a instance with the same name.
	 * 
	 * @param sa
	 *            the
	 *            {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}
	 *            that can be executed by an agent in the game.
	 */
	public abstract void addSGAgentAction(SGAgentAction sa);

	/**
	 * Returns the single agent action with the given name. This method will
	 * throw a runtime exception if it is not an instance of the single agent
	 * domain (SADomain).
	 * 
	 * @param name
	 *            the name of the action to return
	 * @return the action with the given name or null if it does not exist.
	 */
	public abstract Action getAction(String name);

	/**
	 * Returns a list of the single agent actions that define this domain.
	 * Modifying the returned list will not alter the list of actions that
	 * define this domain, because it returns a shallow copy. Modifying the
	 * actions in the returned list will, however, modify the actions in this
	 * domain. This method will throw a runtime exception if it is not an
	 * instance of the single agent domain (SADomain).
	 * 
	 * @return a list of the single agent actions that define this domain
	 */
	public abstract List<Action> getActions();

	/**
	 * Returns a list of the stochastic game actions that that can be taken by
	 * individual agents in this domain. Modifying the returned list will not
	 * alter the list of actions that define this domain, because it returns a
	 * shallow copy. Modifying the actions in the returned list will, however,
	 * modify the actions in this domain. This method will throw a runtime
	 * exception if it is not an instance of the stochastic game domain
	 * (SGDomain).
	 * 
	 * @return a list of the stochastic game actions that that can be taken by
	 *         individual agents in this domain
	 */
	public abstract List<SGAgentAction> getAgentActions();

	/**
	 * Returns the attribute in this domain with the given name
	 * 
	 * @param name
	 *            the name of the attribute to return
	 * @return the attribute with the given name, or null if it is not present.
	 */
	public Attribute getAttribute(String name) {
		return attributeMap.get(name);
	}

	/**
	 * Returns a list of the attributes that define this domain. Modifying the
	 * returned list will not alter the list of attributes that define this
	 * domain, because it returns a shallow copy. Modifying the attributes in
	 * the returned list will, however, modify the attributes in this domain.
	 * 
	 * @return a shallow copy of the attributes in this domain.
	 */
	public List<Attribute> getAttributes() {
		return new ArrayList<Attribute>(attributes);
	}

	/**
	 * Returns the debug code used for printing debug messages.
	 * 
	 * @return the debug code used for printing debug messages.
	 */
	public int getDebugCode() {
		return this.debugCode;
	}

	/**
	 * This will return a new domain object populated with copies of this
	 * Domain's ObjectClasses. Note that propositional functions and actions are
	 * not copied into the new domain.
	 * 
	 * @return a new Domain object with copies of this Domain's ObjectClasses
	 */
	public Domain getNewDomainWithCopiedObjectClasses() {
		Domain d = this.newInstance();
		for (ObjectClass oc : this.objectClasses) {
			oc.copy(d);
		}
		d.objectIdentifierDependentDomain = this.objectIdentifierDependentDomain;
		return d;
	}

	/**
	 * Returns the object class in this domain with the given name.
	 * 
	 * @param name
	 *            the name of the object class to return
	 * @return the object class with the given name or null if it is not
	 *         present.
	 */
	public ObjectClass getObjectClass(String name) {
		return objectClassMap.get(name);
	}

	/**
	 * Returns the list of object classes that define this domain. Modifying the
	 * returned list will not alter the list of object classes that define this
	 * domain, because it returns a shallow copy. Modifying the object classes
	 * in the returned list will, however, modify the object classes in this
	 * domain.
	 * 
	 * @return a shallow copy of the object classes in this domain.
	 */
	public List<ObjectClass> getObjectClasses() {
		return new ArrayList<ObjectClass>(objectClasses);
	}

	/**
	 * Returns the propositional function in this domain with the given name
	 * 
	 * @param name
	 *            the name of the attribute to return.
	 * @return the propositional function with the given name or null if it is
	 *         not present.
	 */
	public PropositionalFunction getPropFunction(String name) {
		return propFunctionMap.get(name);
	}

	/**
	 * Returns a list of the propositional functions that define this domain.
	 * Modifying the returned list will not alter the list of propositional
	 * functions that define this domain, because it returns a shallow copy.
	 * Modifying the propositional functions in the returned list will, however,
	 * modify the propositional functions in this domain.
	 * 
	 * @return a list of the propositional functions that define this domain
	 */
	public List<PropositionalFunction> getPropFunctions() {
		return new ArrayList<PropositionalFunction>(propFunctions);
	}

	/**
	 * Returns a map of propositional function classes to the set of
	 * propositional functions that belong to that class, but only includes
	 * propositional function classes that define propositional functions that
	 * operate on a given object class parameter.
	 * 
	 * @param objectClassName
	 *            the name of the object class for which propositional function
	 *            classes should be returned
	 * @return a map of propositional function classes to the set of
	 *         propositional functions that belong to that class.
	 */
	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsFromObjectClass(
			String objectClassName) {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for (PropositionalFunction pf : this.propFunctions) {
			for (String paramClass : pf.getParameterClasses()) {
				if (paramClass.equals(objectClassName)) {
					String propFuncClass = pf.getClassName();
					Set<PropositionalFunction> propList = propFuncs
							.get(propFuncClass);
					if (propList == null) {
						propList = new HashSet<PropositionalFunction>();
					}

					propList.add(pf);
					propFuncs.put(propFuncClass, propList);
				}
			}
		}
		return propFuncs;
	}

	/**
	 * Returns a map of propositional function classes to the set of
	 * propositional functions that belong to that class.
	 * 
	 * @return a map of propositional function classes to the set of
	 *         propositional functions that belong to that class.
	 */
	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsMap() {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for (PropositionalFunction pf : this.propFunctions) {

			String propFuncClass = pf.getClassName();
			Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
			if (propList == null) {
				propList = new HashSet<PropositionalFunction>();
			}

			propList.add(pf);
			propFuncs.put(propFuncClass, propList);

		}
		return propFuncs;
	}

	/**
	 * Return the stochastic game action (
	 * {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}) with the
	 * given name. This method will throw a runtime exception if it is not an
	 * instance of the stochastic game domain (SGDomain).
	 * 
	 * @param name
	 *            the name of the action to return
	 * @return the
	 *         {@link burlap.oomdp.stochasticgames.agentactions.SGAgentAction}
	 *         with the given name or null if it does not exist.
	 */
	public abstract SGAgentAction getSGAgentAction(String name);

	/**
	 * DEPRECATED: Use {@link #getSGAgentAction(String)} instead.<br/>
	 * Return the stochastic game action with the given name. This method will
	 * throw a runtime exception if it is not an instance of the stochastic game
	 * domain (SGDomain).
	 * 
	 * @param name
	 *            the name of the action to return
	 * @return the action with the given name or null if it does not exist.
	 */
	@Deprecated
	public abstract SGAgentAction getSingleAction(String name);

	/**
	 * Returns whether this domain's states are object identifier (name)
	 * dependent. In an OO-MDP states are represented as a set of object
	 * instances; therefore state equality can either be determined by whether
	 * there is a bijection between the states such that the matched objects
	 * have the same value (identifier independent), or whether objects with the
	 * same identifier have the same values (identifier dependent). For
	 * instance, imagine a state s_1 with two objects of the same class, o_1 and
	 * o_2 with value assignments v_a and v_b, respectively. Imagine a
	 * corresponding state s_2, also with objects o_1 and o_2; however, in s_2,
	 * the value assignment is o_1=v_b and o_2=v_a. If the domain is identifier
	 * independent, then s_1 == s_2, because you can match o_1 in s_1 to o_2 in
	 * s_2 (and symmetrically for the other objects). However, if the domain is
	 * identifier dependent, then s_1 != s_2, because the objects with the same
	 * identifiers (o_1 and o_2) have different values in each state.
	 * 
	 * @return true if this domain is identifier dependent and false if it
	 *         object identifier independent.
	 */
	public boolean isObjectIdentifierDependent() {
		return this.objectIdentifierDependentDomain;
	}

	/**
	 * Will return a new instance of this Domain's class (either SADomain or
	 * SGDomain)
	 * 
	 * @return a new instance of this Domain's class (either SADomain or
	 *         SGDomain)
	 */
	protected abstract Domain newInstance();

	/**
	 * Sets the debug code used for printing debug messages.
	 * 
	 * @param debugCode
	 *            the debug code used for printing debug messages
	 */
	public void setDebugCode(int debugCode) {
		this.debugCode = debugCode;
	}

	/**
	 * Sets whether this domain's states are object identifier (name) dependent.
	 * In an OO-MDP states are represented as a set of object instances;
	 * therefore state equality can either be determined by whether there is a
	 * bijection between the states such that the matched objects have the same
	 * value (identifier independent), or whether objects with the same
	 * identifier have the same values (identifier dependent). For instance,
	 * imagine a state s_1 with two objects of the same class, o_1 and o_2 with
	 * value assignments v_a and v_b, respectively. Imagine a corresponding
	 * state s_2, also with objects o_1 and o_2; however, in s_2, the value
	 * assignment is o_1=v_b and o_2=v_a. If the domain is identifier
	 * independent, then s_1 == s_2, because you can match o_1 in s_1 to o_2 in
	 * s_2 (and symmetrically for the other objects). However, if the domain is
	 * identifier dependent, then s_1 != s_2, because the objects with the same
	 * identifiers (o_1 and o_2) have different values in each state.
	 * 
	 * @param objectIdentifierDependent
	 *            sets whether this domain's states are object identifier
	 *            dependent (true) or not (false).
	 */
	public void setObjectIdentiferDependence(boolean objectIdentifierDependent) {
		this.objectIdentifierDependentDomain = objectIdentifierDependent;
	}

	/**
	 * Toggles whether debug messages are printed
	 * 
	 * @param debugPrintingOn
	 *            if true debug messages are printed; messages will not be
	 *            printed if false.
	 */
	public void toggleDebugPrinting(boolean debugPrintingOn) {
		DPrint.toggleCode(this.debugCode, debugPrintingOn);
	}

}
