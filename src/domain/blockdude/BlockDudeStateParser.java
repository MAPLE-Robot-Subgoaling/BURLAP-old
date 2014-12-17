package domain.blockdude;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class BlockDudeStateParser implements StateParser {

    protected Domain domain;

    public BlockDudeStateParser(Domain d) {
	this.domain = d;
    }

    @Override
    public String stateToString(State s) {
	StringBuffer sbuf = new StringBuffer(256);

	ObjectInstance a = s.getObjectsOfTrueClass(BlockDudeDomain.CLASSAGENT)
		.get(0);
	ObjectInstance e = s.getObjectsOfTrueClass(BlockDudeDomain.CLASSEXIT)
		.get(0);
	List<ObjectInstance> blocks = s
		.getObjectsOfTrueClass(BlockDudeDomain.CLASSBLOCK);
	List<ObjectInstance> platforms = s
		.getObjectsOfTrueClass(BlockDudeDomain.CLASSPLATFORM);

	String xa = BlockDudeDomain.ATTX;
	String ya = BlockDudeDomain.ATTY;
	String da = BlockDudeDomain.ATTDIR;
	String hda = BlockDudeDomain.ATTHOLD;
	String hea = BlockDudeDomain.ATTHEIGHT;

	// write the number of the multi-instance objects
	sbuf.append(blocks.size() + ", " + platforms.size() + ", ");

	// write the agent
	sbuf.append(a.getDiscValForAttribute(xa) + " ")
		.append(a.getDiscValForAttribute(ya) + " ")
		.append(a.getDiscValForAttribute(da) + " ")
		.append(a.getDiscValForAttribute(hda) + ", ");

	// write exit
	sbuf.append(e.getDiscValForAttribute(xa) + " ").append(
		e.getDiscValForAttribute(ya));

	// write blocks
	for (ObjectInstance b : blocks) {
	    sbuf.append(", ").append(b.getDiscValForAttribute(xa) + " ")
		    .append(b.getDiscValForAttribute(ya));
	}

	// write platforms
	for (ObjectInstance p : platforms) {
	    sbuf.append(", ").append(p.getDiscValForAttribute(xa) + " ")
		    .append(p.getDiscValForAttribute(hea));
	}

	return sbuf.toString();
    }

    @Override
    public State stringToState(String str) {

	String[] obcomps = str.split(", ");

	int nb = Integer.parseInt(obcomps[0]);
	// don't need the number of platforms explicitly

	// parse coordinates for platforms
	List<Integer> xpcomps = new ArrayList<Integer>();
	List<Integer> hpcomps = new ArrayList<Integer>();
	for (int i = 4 + nb; i < obcomps.length; i++) {
	    String[] pscomps = obcomps[i].split(" ");
	    xpcomps.add(Integer.parseInt(pscomps[0]));
	    hpcomps.add(Integer.parseInt(pscomps[1]));
	}

	State s = BlockDudeDomain.getCleanState(this.domain, xpcomps, hpcomps,
		nb);

	// parse agent
	String[] ascomps = obcomps[2].split(" ");
	BlockDudeDomain.setAgent(s, Integer.parseInt(ascomps[0]),
		Integer.parseInt(ascomps[1]), Integer.parseInt(ascomps[2]),
		Integer.parseInt(ascomps[3]));

	// parse exit
	String[] escomps = obcomps[3].split(" ");
	BlockDudeDomain.setExit(s, Integer.parseInt(escomps[0]),
		Integer.parseInt(escomps[1]));

	// parse each block
	for (int i = 4; i < 4 + nb; i++) {
	    String[] bscomps = obcomps[i].split(" ");
	    BlockDudeDomain.setBlock(s, i - 4, Integer.parseInt(bscomps[0]),
		    Integer.parseInt(bscomps[1]));
	}

	return s;
    }

}
