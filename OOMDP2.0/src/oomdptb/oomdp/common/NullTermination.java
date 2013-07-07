package oomdptb.oomdp.common;

import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class NullTermination implements TerminalFunction {


	@Override
	public boolean isTerminal(State s) {
		return false;
	}
	

}
