package burlap.oomdp.core.values;

import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;

/**
 * A relational valued value subclass in which values are stored as a single
 * String object for the name of the object instance to which it is linked. If
 * the relational value is not linked to any object, then the String value is
 * set to the empty String: "".
 * 
 * @author James MacGlashan
 * 
 */
public class RelationalValue extends OOMDPValue implements Value {
	private static final String UNSET = "";
	/**
	 * A string representing the object target of this value. Targets are
	 * specified by the object name identifier. If the relational target is
	 * unset, then this value will be set to the empty string "", which is the
	 * default value.
	 */
	protected final String target;

	/**
	 * Initializes this value to be an assignment for Attribute attribute.
	 * 
	 * @param attribute
	 */
	public RelationalValue(Attribute attribute) {
		super(attribute);
		this.target = UNSET;
	}

	public RelationalValue(Attribute attribute, String target) {
		super(attribute);
		this.target = target;
	}

	/**
	 * Initializes this value as a copy from the source Value object v.
	 * 
	 * @param v
	 *            the source Value to make this object a copy of.
	 */
	public RelationalValue(RelationalValue v) {
		super(v);
		RelationalValue rv = v;
		this.target = rv.target;
	}

	@Override
	public Value addRelationalTarget(String t) {
		return new RelationalValue(this.attribute, t);
	}

	@Override
	public StringBuilder buildStringVal(StringBuilder builder) {
		return builder.append(this.target);
	}

	@Override
	public Value clearRelationTargets() {
		return new RelationalValue(this.attribute);
	}

	@Override
	public Value copy() {
		return new RelationalValue(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RelationalValue)) {
			return false;
		}

		RelationalValue op = (RelationalValue) obj;
		if (!op.attribute.equals(attribute)) {
			return false;
		}

		return this.target.equals(op.target);

	}

	@Override
	public Set<String> getAllRelationalTargets() {
		Set<String> res = new TreeSet<String>();
		res.add(this.target);
		return res;
	}

	@Override
	public double getNumericRepresentation() {
		return 0;
	}

	@Override
	public Value removeRelationalTarget(String target) {
		if (this.target.equals(target)) {
			return new RelationalValue(this.attribute);
		}
		return this;
	}

	@Override
	public Value setValue(String v) {
		return new RelationalValue(this.attribute, v);
	}

	@Override
	public boolean valueHasBeenSet() {
		return true;
	}

}
