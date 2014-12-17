package domain.fourroomsdomain;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class FourRoomsStateParser implements StateParser {

    @Override
    public String stateToString(State s) {

	StringBuffer sbuf = new StringBuffer(256);

	ObjectInstance a = s.getObjectsOfTrueClass(FourRooms.CLASSAGENT).get(0);
	ObjectInstance g = s.getObjectsOfTrueClass(FourRooms.CLASSGOAL).get(0);

	String xa = FourRooms.ATTX;
	String ya = FourRooms.ATTY;

	sbuf.append(a.getDiscValForAttribute(xa)).append(" ")
		.append(a.getDiscValForAttribute(ya)).append(", ");
	sbuf.append(g.getDiscValForAttribute(xa)).append(" ")
		.append(g.getDiscValForAttribute(ya));

	return sbuf.toString();
    }

    @Override
    public State stringToState(String str) {

	String[] obcomps = str.split(", ");

	String[] acomps = obcomps[0].split(" ");
	String[] gcomps = obcomps[1].split(" ");

	int ax = Integer.parseInt(acomps[0]);
	int ay = Integer.parseInt(acomps[1]);

	int gx = Integer.parseInt(gcomps[0]);
	int gy = Integer.parseInt(gcomps[1]);

	State s = FourRooms.getCleanState();
	FourRooms.setAgent(s, ax, ay);
	FourRooms.setGoal(s, gx, gy);

	return s;
    }

}
