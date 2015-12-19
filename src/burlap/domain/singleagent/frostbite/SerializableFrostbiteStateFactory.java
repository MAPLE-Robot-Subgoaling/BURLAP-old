package burlap.domain.singleagent.frostbite;

import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.legacy.StateParser;
import burlap.oomdp.stateserialization.SerializableState;
import burlap.oomdp.stateserialization.SerializableStateFactory;

/**
 * A {@link burlap.oomdp.stateserialization.SerializableStateFactory} for simple
 * string representations of
 * {@link burlap.domain.singleagent.frostbite.FrostbiteDomain} states.
 * 
 * @author James MacGlashan.
 */
public class SerializableFrostbiteStateFactory implements
		SerializableStateFactory {

	public static class FrostbiteStateParser implements StateParser {

		Domain domain;

		public FrostbiteStateParser(Domain domain) {
			this.domain = domain;
		}

		@Override
		public String stateToString(State s) {
			return SerializableFrostbiteStateFactory.stateToString(s);
		}

		@Override
		public State stringToState(String str) {
			return SerializableFrostbiteStateFactory.stringToState(domain, str);
		}
	}

	public static class SerializableFrostbiteState extends SerializableState {

		public String stringRep;

		public SerializableFrostbiteState() {
		}

		public SerializableFrostbiteState(State s) {
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
		StringBuffer buf = new StringBuffer(256);

		ObjectInstance agent = s.getObjectsOfClass(FrostbiteDomain.AGENTCLASS)
				.get(0);
		ObjectInstance igloo = s.getObjectsOfClass(FrostbiteDomain.IGLOOCLASS)
				.get(0);
		List<ObjectInstance> platforms = s
				.getObjectsOfClass(FrostbiteDomain.PLATFORMCLASS);

		// write agent
		buf.append(agent.getRealValForAttribute(FrostbiteDomain.XATTNAME))
				.append(" ");
		buf.append(agent.getRealValForAttribute(FrostbiteDomain.YATTNAME))
				.append("\n");

		// write pad
		buf.append(igloo
				.getRealValForAttribute(FrostbiteDomain.BUILDINGATTNAME));

		// write each obstacle
		for (ObjectInstance ob : platforms) {
			buf.append("\n")
					.append(ob.getRealValForAttribute(FrostbiteDomain.XATTNAME))
					.append(" ");
			buf.append(ob.getRealValForAttribute(FrostbiteDomain.YATTNAME))
					.append(" ");
			buf.append(ob.getRealValForAttribute(FrostbiteDomain.SIZEATTNAME))
					.append(" ");
			buf.append(ob
					.getRealValForAttribute(FrostbiteDomain.ACTIVATEDATTNAME));
		}

		return buf.toString();
	}

	public static State stringToState(Domain domain, String str) {

		str = str.trim();

		String[] lineComps = str.split("\n");
		String[] aComps = lineComps[0].split(" ");
		String[] pComps = lineComps[1].split(" ");

		State s = FrostbiteDomain.getCleanState(domain);

		FrostbiteDomain.setAgent(s, Integer.parseInt(aComps[0]),
				Integer.parseInt(aComps[1]));
		FrostbiteDomain.setIgloo(s, Integer.parseInt(pComps[0]));

		for (int i = 2; i < lineComps.length; i++) {
			String[] oComps = lineComps[i].split(" ");
			FrostbiteDomain.setPlatform(s, i - 2, Integer.parseInt(oComps[0]),
					Integer.parseInt(oComps[1]), Integer.parseInt(oComps[2]),
					Boolean.parseBoolean(oComps[3]));
		}
		return s;
	}

	@Override
	public Class<?> getGeneratedClass() {
		return SerializableFrostbiteState.class;
	}

	@Override
	public SerializableState serialize(State s) {
		return new SerializableFrostbiteState(s);
	}

}
