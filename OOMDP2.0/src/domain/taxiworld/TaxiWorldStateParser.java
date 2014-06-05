package domain.taxiworld;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class TaxiWorldStateParser implements StateParser {
    @Override
    public String stateToString(State s) {

        StringBuffer sbuf = new StringBuffer(256);

        ObjectInstance a = s.getObjectsOfTrueClass(TaxiWorldDomain.CLASSAGENT).get(0);
        ObjectInstance g = s.getObjectsOfTrueClass(TaxiWorldDomain.CLASSGOAL).get(0);

        String xa = TaxiWorldDomain.ATTX;
        String ya = TaxiWorldDomain.ATTY;

        sbuf.append(a.getDiscValForAttribute(xa)).append(" ").append(a.getDiscValForAttribute(ya)).append(", ");
        sbuf.append(g.getDiscValForAttribute(xa)).append(" ").append(g.getDiscValForAttribute(ya)).append(", ");

        for (int i = 1; i <= TaxiWorldDomain.MAXPASS; i++) {
            ObjectInstance p = s.getObjectsOfTrueClass(TaxiWorldDomain.CLASSPASS + i).get(0);
            sbuf.append(p.getDiscValForAttribute(xa)).append(" ").append(p.getDiscValForAttribute(ya));
            if (i != TaxiWorldDomain.MAXPASS) {
                sbuf.append(", ");
            }
        }

        return sbuf.toString();
    }

    @Override
    public State stringToState(String str) {
        String [] obcomps = str.split(", ");

        String [] acomps = obcomps[0].split(" ");
        String [] gcomps = obcomps[1].split(" ");

        int ax = Integer.parseInt(acomps[0]);
        int ay = Integer.parseInt(acomps[1]);
        int gx = Integer.parseInt(gcomps[0]);
        int gy = Integer.parseInt(gcomps[1]);

        State s = TaxiWorldDomain.getCleanState();
        TaxiWorldDomain.setAgent(s, ax, ay);
        TaxiWorldDomain.setGoal(s, gx, gy);

        for (int i = 1; i <= TaxiWorldDomain.MAXPASS; i++) {
            String[] pcomps = obcomps[1 + i].split(" ");
            int px = Integer.parseInt(pcomps[0]);
            int py = Integer.parseInt(pcomps[1]);
            TaxiWorldDomain.setPassenger(s, i, px, py);
        }

        return s;
    }

}
