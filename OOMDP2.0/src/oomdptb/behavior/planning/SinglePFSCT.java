package oomdptb.behavior.planning;

import java.util.List;

import oomdptb.oomdp.GroundedProp;
import oomdptb.oomdp.PropositionalFunction;
import oomdptb.oomdp.State;

public class SinglePFSCT implements StateConditionTest {

	PropositionalFunction pf;
	
	public SinglePFSCT(PropositionalFunction pf) {
		this.pf = pf;
	}

	@Override
	public boolean satisfies(State s) {
		
		List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
		
		for(GroundedProp gp : gps){
			if(gp.isTrue(s)){
				return true;
			}
		}
		
		return false;
		
	}

}
