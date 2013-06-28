package edu.brown.cs.ai.behavior.oomdp.planning.deterministc;

import java.util.List;

import edu.umbc.cs.maple.oomdp.GroundedProp;
import edu.umbc.cs.maple.oomdp.PropositionalFunction;
import edu.umbc.cs.maple.oomdp.State;
import edu.umbc.cs.maple.oomdp.TerminalFunction;

public class SinglePFTF extends TerminalFunction {

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
