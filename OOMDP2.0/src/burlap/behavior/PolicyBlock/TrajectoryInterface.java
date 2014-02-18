package burlap.behavior.PolicyBlock;

public interface TrajectoryInterface {

	/*
	 * Objects needed
	 * ArrayList<EpisodeAnalysis> episodes();
	 */
	
	/*	Functions needed for running TrajectoryInterface
	 * 	================================================*/
	
	/**
	 * createEpisodes() - for trajectory merging via PolicyBlocksOptionGenerator. This merges object  
	 * according to trajectories determined after running Q-Learning, and build options based off of that. 
	 * @param output - the filepath for printing the results
	 * @param number - number of episodes to create
	 * 
	 * Note:
	 * 		switch to a Planner such as Value Iteration...
	 */
	public void createEpisodes(String output, int number);
	
	/**
	 * writeTrajectory() - takes the converted trajectory policy object and writes it to the output
	 * @param t - the trajectory policy object
	 * @param output - the string output for writing the files
	 */
	public void writeTrajectory(TrajectoryPolicy t, String output);
	
	
}
