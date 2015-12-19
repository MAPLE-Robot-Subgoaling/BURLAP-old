package burlap.domain.singleagent.gridworld;

import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.legacy.StateParser;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;

/**
 * A factory for producing a simple
 * {@link burlap.oomdp.stateserialization.SerializableState} representation of
 * {@link burlap.domain.singleagent.gridworld.GridWorldDomain} states by using a
 * string representation. <br/>
 * String format:<br/>
 * ax ay, l1x l1y l1t, l2x l2y lt2, ..., lnx lny lnt <br/>
 * where ax and ay is the agent x and y position and lix liy is the ith location
 * objects x and y position and lit is the type of the ith location object.
 * 
 * @author James MacGlashan.
 */
public class SerializableGridWorldStateFactory implements
		SerializableStateFactory {

	/**
	 * A legacy {@link burlap.oomdp.legacy.StateParser} for
	 * {@link burlap.domain.singleagent.gridworld.GridWorldDomain} states.
	 * Follows the same string format as
	 * {@link SerializableGridWorldStateFactory}.
	 */
	public static class GridWorldStateParser implements StateParser {

		protected Domain domain;

		public GridWorldStateParser(Domain domain) {
			this.domain = domain;
		}

		@Override
		public String stateToString(State s) {
			return SerializableGridWorldStateFactory.stateToString(s);
		}

		@Override
		public State stringToState(String str) {
			return SerializableGridWorldStateFactory.stringToState(domain, str);
		}
	}

	public static class SerializableGridWorldState extends SerializableState {

		public String stringRep;

		public SerializableGridWorldState() {
		}

		public SerializableGridWorldState(State s) {
			super(s);
		}

		@Override
		public State deserialize(Domain domain) {
			return stringToState(domain, this.stringRep);
		}

		@Override
		public void serialize(State s) {
			this.stringRep = stateToString(s);
		}

	}

	public static String stateToString(State s) {
		StringBuilder sbuf = new StringBuilder(256);

		ObjectInstance a = s.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(
				0);
		List<ObjectInstance> locs = s
				.getObjectsOfClass(GridWorldDomain.CLASSLOCATION);

		String xa = GridWorldDomain.ATTX;
		String ya = GridWorldDomain.ATTY;
		String lt = GridWorldDomain.ATTLOCTYPE;

		sbuf.append(a.getIntValForAttribute(xa)).append(" ")
				.append(a.getIntValForAttribute(ya));
		for (ObjectInstance l : locs) {
			sbuf.append(", ").append(l.getIntValForAttribute(xa)).append(" ")
					.append(l.getIntValForAttribute(ya)).append(" ")
					.append(l.getIntValForAttribute(lt));
		}

		return sbuf.toString();
	}

	public static State stringToState(Domain domain, String str) {
		String[] obcomps = str.split(", ");

		String[] acomps = obcomps[0].split(" ");
		int ax = Integer.parseInt(acomps[0]);
		int ay = Integer.parseInt(acomps[1]);

		int nl = obcomps.length - 1;

		State s = GridWorldDomain.getOneAgentNLocationState(domain, nl);
		GridWorldDomain.setAgent(s, ax, ay);

		for (int i = 1; i < obcomps.length; i++) {
			String[] lcomps = obcomps[i].split(" ");
			int lx = Integer.parseInt(lcomps[0]);
			int ly = Integer.parseInt(lcomps[1]);

			if (lcomps.length < 3) {
				GridWorldDomain.setLocation(s, i - 1, lx, ly);
			} else {
				int lt = Integer.parseInt(lcomps[2]);
				GridWorldDomain.setLocation(s, i - 1, lx, ly, lt);
			}

		}

		return s;
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SerializableGridWorldState.class;
	}

	@Override
	public SerializableState serialize(State s) {
		return new SerializableGridWorldState(s);
	}

}
