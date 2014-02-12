package burlap.behavior.PolicyBlock;

import java.util.List;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class TrajectoryPolicy extends Policy{

	public EpisodeAnalysis trajectory;
	
	public TrajectoryPolicy(EpisodeAnalysis trajectory){
		this.trajectory = trajectory;
	}
	
	@Override
	public GroundedAction getAction(State s) {
		// TODO Auto-generated method stub
		
		for(int i = 0; i < trajectory.stateSequence.size(); i++){
			if(s.equals(trajectory.stateSequence.get(i)))
					return trajectory.actionSequence.get(i);
		}
		
		return null;
	}
	
	public EpisodeAnalysis justDoIt(){
		EpisodeAnalysis result = new EpisodeAnalysis();
		
		int steps = 0;
		
		for(State s: trajectory.stateSequence){
			if(steps >= trajectory.stateSequence.size()-1)
				break;
			result.addState(trajectory.stateSequence.get(steps));
			result.addAction(trajectory.actionSequence.get(steps));
			result.addReward(1);
			steps++;
		}
		
		return result;
	}

	@Override
	public List<ActionProb> getActionDistributionForState(State s) {
		// TODO Auto-generated method stub
		return this.getDeterministicPolicy(s);
	}

	@Override
	public boolean isStochastic() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDefinedFor(State s) {
		
		for(State iter: trajectory.stateSequence){
			if(iter.equals(s))
				return true;
		}
		
		return false;
	}

}
