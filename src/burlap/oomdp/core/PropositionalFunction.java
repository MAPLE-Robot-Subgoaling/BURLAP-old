package burlap.oomdp.core;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.states.State;

/**
 * The propositional function class defines evaluations of object instances in
 * an OO-MDP state and are part of the definition for an OO-MDP domain.
 * Propositional functions have an name to identify them, a set of parameters
 * that they can take, and an optional propositional function class to which
 * they belong. The parameters of a propositional function may also be assigned
 * to order groups that indicate any symmetry in the evaluation of the
 * propositional function. For instance, suppose we were to define a "touching"
 * propositional function that specified whether two objects of a class
 * representing blocks (called BLOCK) were touching in the world or not. For
 * instance, we can imagine evaluating touching(block0, block1). Note that there
 * is a symmetry in this evaluation; that is touching(block0, block1) =
 * touching(block1, block0) in which case there is no point to evaluate both
 * propositional functions. Specifying parameter order groups allows the
 * designer to indicate these kinds of symmetry. If two parameters are set to
 * the same order group, then it indicates that the evaluation for the
 * propositional function will be the same for any order of parameters within
 * that order group. If the order group between two parameters is different,
 * then it indicates that the order of the parameters can change the evaluation.
 * So, in the previous touching example, if the parameter order groups of the
 * two parameters were both "p0", then it indicates that touching(block0,
 * block1) = touching(block1, block0); if one parameter was assigned to order
 * group "p0" and the other to "p1", then it is possible that touching(block0,
 * block1) != touching(block1, block0). If the parameter order groups of
 * parameters are not specified, then they are all assumed to be different.
 * 
 * @author James MacGlashan
 * 
 */
public abstract class PropositionalFunction {

	/**
	 * Returns all possible groundings for all of the
	 * {@link PropositionalFunction}s in the provided list for the given
	 * {@link State}.
	 * 
	 * @param pfs
	 *            The list of {@link PropositionalFunction}s for which all
	 *            groundings will be returned.
	 * @param s
	 *            the {@link State} in which the groundings should be produced.
	 * @return a {@link List} of all possible groundings for all of the
	 *         {@link PropositionalFunction}s in the provided list for the given
	 *         {@link State}
	 */
	public static List<GroundedProp> getAllGroundedPropsFromPFList(
			List<PropositionalFunction> pfs, State s) {
		List<GroundedProp> res = new ArrayList<GroundedProp>();
		for (PropositionalFunction pf : pfs) {
			List<GroundedProp> gps = pf.getAllGroundedPropsForState(s);
			res.addAll(gps);
		}
		return res;
	}

	/*
	 * name of the propositional function
	 */
	protected String name;

	/*
	 * domain that hosts this function
	 */
	protected Domain domain;

	/*
	 * list of class names for each parameter of the function
	 */
	protected String[] parameterClasses;

	/*
	 * Defines symmetry between parameters. For example, setting two or more
	 * parameters to the same order group indicates that the function evaluate
	 * the same regardless of which specific object is set to each parameter
	 */
	protected String[] parameterOrderGroup;

	/**
	 * optional; allows propositional functions to be grouped by class names
	 */
	protected String pfClass;

	/**
	 * Initializes a propositional function with the given name, domain and
	 * parameter object classes. The domain will automatically have this
	 * propositional function added to its definition. Unique parameter order
	 * groups for each parameter are assumed.
	 * 
	 * @param name
	 *            the name of the propositional function
	 * @param domain
	 *            the domain to which this propositional function should be
	 *            connected.
	 * @param parameterClasses
	 *            a comma delineated string specifying the name of object
	 *            classes that the parameters must satisfy.
	 */
	public PropositionalFunction(String name, Domain domain,
			String parameterClasses) {

		String[] pClassArray;
		if (parameterClasses.equals("")) {
			pClassArray = new String[0];
		} else {
			pClassArray = parameterClasses.split(",");
		}

		String[] pog = new String[pClassArray.length];
		for (int i = 0; i < pog.length; i++) {
			pog[i] = name + ".P" + i;
		}

		this.init(name, domain, pClassArray, pog, name);

	}

	/**
	 * Initializes a propositional function with the given name, domain,
	 * parameter object classes, and propositional function class name. The
	 * domain will automatically have this propositional function added to its
	 * definition. Unique parameter order groups for each parameter are assumed.
	 * 
	 * @param name
	 *            the name of the propositional function
	 * @param domain
	 *            the domain to which this propositional function should be
	 *            connected.
	 * @param parameterClasses
	 *            a comma delineated string specifying the name of object
	 *            classes that the parameters must satisfy.
	 * @param pfClassName
	 *            the name of the propositional function class to which this PF
	 *            belongs.
	 */
	public PropositionalFunction(String name, Domain domain,
			String parameterClasses, String pfClassName) {

		String[] pClassArray;
		if (parameterClasses.equals("")) {
			pClassArray = new String[0];
		} else {
			pClassArray = parameterClasses.split(",");
		}

		String[] pog = new String[pClassArray.length];
		for (int i = 0; i < pog.length; i++) {
			pog[i] = name + ".P" + i;
		}

		this.init(name, domain, pClassArray, pog, pfClassName);

	}

	/**
	 * Initializes a propositional function with the given name, domain and
	 * parameter object classes. The domain will automatically have this
	 * propositional function added to its definition. Unique parameter order
	 * groups for each parameter are assumed.
	 * 
	 * @param name
	 *            the name of the propositional function
	 * @param domain
	 *            the domain to which this propositional function should be
	 *            connected.
	 * @param parameterClasses
	 *            an array of strings specifying the name of object classes that
	 *            the parameters must satisfy.
	 */
	public PropositionalFunction(String name, Domain domain,
			String[] parameterClasses) {

		String[] rcn = new String[parameterClasses.length];
		for (int i = 0; i < rcn.length; i++) {
			rcn[i] = name + ".P" + i;
		}

		this.init(name, domain, parameterClasses, rcn, name);

	}

	/**
	 * Initializes a propositional function with the given name, domain,
	 * parameter object classes, and propositional function class name. The
	 * domain will automatically have this propositional function added to its
	 * definition. Unique parameter order groups for each parameter are assumed.
	 * 
	 * @param name
	 *            the name of the propositional function
	 * @param domain
	 *            the domain to which this propositional function should be
	 *            connected.
	 * @param parameterClasses
	 *            an array of strings specifying the name of object classes that
	 *            the parameters must satisfy.
	 * @param pfClassName
	 *            the name of the propositional function class to which this PF
	 *            belongs.
	 */
	public PropositionalFunction(String name, Domain domain,
			String[] parameterClasses, String pfClassName) {

		String[] rcn = new String[parameterClasses.length];
		for (int i = 0; i < rcn.length; i++) {
			rcn[i] = name + ".P" + i;
		}

		this.init(name, domain, parameterClasses, rcn, pfClassName);

	}

	/**
	 * Initializes a propositional function with the given name, domain,
	 * parameter object classes, and the parameter order groups of the
	 * parameters. The domain will automatically have this propositional
	 * function added to its definition.
	 * 
	 * @param name
	 *            the name of the propositional function
	 * @param domain
	 *            the domain to which this propositional function should be
	 *            connected.
	 * @param parameterClasses
	 *            an array of strings specifying the name of object classes that
	 *            the parameters must satisfy.
	 * @param parameterOrderGroup
	 *            an array of strings specifying order group names for the
	 *            parameters
	 */
	public PropositionalFunction(String name, Domain domain,
			String[] parameterClasses, String[] parameterOrderGroup) {
		this.init(name, domain, parameterClasses, parameterOrderGroup, name);
	}

	/**
	 * Initializes a propositional function with the given name, domain,
	 * parameter object classes, the parameter order groups of the parameters,
	 * and the propositional function class name. The domain will automatically
	 * have this propositional function added to its definition.
	 * 
	 * @param name
	 *            the name of the propositional function
	 * @param domain
	 *            the domain to which this propositional function should be
	 *            connected.
	 * @param parameterClasses
	 *            an array of strings specifying the name of object classes that
	 *            the parameters must satisfy.
	 * @param parameterOrderGroup
	 *            an array of strings specifying order group names for the
	 *            parameters
	 * @param pfClassName
	 *            the name of the propositional function class to which this PF
	 *            belongs.
	 */
	public PropositionalFunction(String name, Domain domain,
			String[] parameterClasses, String[] parameterOrderGroup,
			String pfClassName) {
		this.init(name, domain, parameterClasses, parameterOrderGroup,
				pfClassName);
	}

	@Override
	public boolean equals(Object obj) {
		PropositionalFunction op = (PropositionalFunction) obj;
		if (op.name.equals(name))
			return true;
		return false;
	}

	/**
	 * Returns all possible groundings of this {@link PropositionalFunction} for
	 * the given {@link State}
	 * 
	 * @param s
	 *            the {@link State} in which all groundings will be returned
	 * @return a {@link List} of all possible groundings of this
	 *         {@link PropositionalFunction} for the given {@link State}
	 */
	public List<GroundedProp> getAllGroundedPropsForState(State s) {

		List<GroundedProp> res = new ArrayList<GroundedProp>();

		if (this.getParameterClasses().length == 0) {
			res.add(new GroundedProp(this, new String[] {}));
			return res; // no parameters so just the single gp without params
		}

		List<List<String>> bindings = s
				.getPossibleBindingsGivenParamOrderGroups(
						this.getParameterClasses(),
						this.getParameterOrderGroups());

		for (List<String> params : bindings) {
			String[] aprams = params.toArray(new String[params.size()]);
			GroundedProp gp = new GroundedProp(this, aprams);
			res.add(gp);
		}

		return res;

	}

	/**
	 * Returns the propositional function class name of this PF.
	 * 
	 * @return the propositional function class name of this PF.
	 */
	public final String getClassName() {
		return pfClass;
	}

	/**
	 * Returns the name of this propositional function.
	 * 
	 * @return the name of this propositional function.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the object classes of the parameters for this propositional
	 * function
	 * 
	 * @return the object classes of the parameters for this propositional
	 *         function
	 */
	public final String[] getParameterClasses() {
		return parameterClasses;
	}

	/**
	 * Returns the parameter order group names for the parameters of this
	 * propositional function.
	 * 
	 * @return the parameter order group names for the parameters of this
	 *         propositional function.
	 */
	public final String[] getParameterOrderGroups() {
		return parameterOrderGroup;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	protected final void init(String name, Domain domain,
			String[] parameterClasses, String[] parameterOrderGroup,
			String pfClass) {
		this.name = name;
		this.domain = domain;
		this.domain.addPropositionalFunction(this);
		this.parameterClasses = parameterClasses;
		this.parameterOrderGroup = parameterOrderGroup;
		this.pfClass = pfClass;
	}

	/**
	 * Returns whether the propositional function is true for the given state
	 * with the given parameters This version is preferred to the comma
	 * delimited version.
	 * 
	 * @param s
	 *            the state that is being checked
	 * @param params
	 *            the parameters being passed in to the propositional function
	 * @return whether the propositional function is true
	 */
	public abstract boolean isTrue(State s, String... params);

	/**
	 * Sets the class name for this propositional function.
	 * 
	 * @param cn
	 *            the propositional function class name to which this PF should
	 *            be assigned.
	 */
	public final void setClassName(String cn) {
		pfClass = cn;
	}

	/**
	 * Returns true if there existing a {@link GroundedProp} for the provided
	 * {@link State} that is in true in the {@link State}.
	 * 
	 * @param s
	 *            the {@link State} to test.
	 * @return true if there existing a {@link GroundedProp} for the provided
	 *         {@link State} that is in true in the {@link State}; false
	 *         otherwise.
	 */
	public boolean somePFGroundingIsTrue(State s) {
		List<GroundedProp> gps = this.getAllGroundedPropsForState(s);
		for (GroundedProp gp : gps) {
			if (gp.isTrue(s)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
