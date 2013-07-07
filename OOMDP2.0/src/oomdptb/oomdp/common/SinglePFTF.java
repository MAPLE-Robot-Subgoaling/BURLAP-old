package oomdptb.oomdp.common;

import java.util.List;

import oomdptb.oomdp.GroundedProp;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.State;
import oomdptb.oomdp.TerminalFunction;

public class SinglePFTF implements TerminalFunction {

	PropositionalFunction			pf;
	boolean							terminateOnTrue;
	
	public SinglePFTF(PropositionalFunction pf){
		this.pf = pf;
		terminateOnTrue = true;
	}
	
	public SinglePFTF(PropositionalFunction pf, boolean terminateOnTrue){
		this.pf = pf;
		this.terminateOnTrue = terminateOnTrue;
	}
	
	public void setTerminateOnTrue(boolean terminateOnTrue){
		this.terminateOnTrue = terminateOnTrue;
	}
	
	@Override
	public boolean isTerminal(State s) {
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		if(terminateOnTrue){
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					return true;
				}
			}
		}
		else{
			for(GroundedProp gp : gps){
				if(!gp.isTrue(s)){
					return true;
				}
			}
		}
		
		return false;
	}

}
