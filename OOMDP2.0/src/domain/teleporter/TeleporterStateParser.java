package domain.teleporter;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class TeleporterStateParser implements StateParser {
    @Override
    public String stateToString(State s) {

        StringBuffer sbuf = new StringBuffer(256);

        ObjectInstance a = s.getObjectsOfTrueClass(TeleporterDomain.CLASSAGENT).get(0);
        ObjectInstance t = s.getObjectsOfTrueClass(TeleporterDomain.CLASSTELE).get(0);
        ObjectInstance g = s.getObjectsOfTrueClass(TeleporterDomain.CLASSGOAL).get(0);

        String xa = TeleporterDomain.ATTX;
        String ya = TeleporterDomain.ATTY;

        sbuf.append(a.getDiscValForAttribute(xa)).append(" ").append(a.getDiscValForAttribute(ya)).append(", ");
        sbuf.append(t.getDiscValForAttribute(xa)).append(" ").append(t.getDiscValForAttribute(ya)).append(", ");
        sbuf.append(g.getDiscValForAttribute(xa)).append(" ").append(g.getDiscValForAttribute(ya));

        return sbuf.toString();
    }

    @Override
    public State stringToState(String str) {
        String [] obcomps = str.split(", ");

        String [] acomps = obcomps[0].split(" ");
        String [] tcomps = obcomps[1].split(" ");
        String [] gcomps = obcomps[2].split(" ");

        int ax = Integer.parseInt(acomps[0]);
        int ay = Integer.parseInt(acomps[1]);
        int tx = Integer.parseInt(tcomps[0]);
        int ty = Integer.parseInt(tcomps[1]);
        int gx = Integer.parseInt(gcomps[0]);
        int gy = Integer.parseInt(gcomps[1]);

        State s = TeleporterDomain.getCleanState();
        TeleporterDomain.setAgent(s, ax, ay);
        TeleporterDomain.setTele(s, tx, ty);
        TeleporterDomain.setGoal(s, gx, gy);

        return s;
    }

}
