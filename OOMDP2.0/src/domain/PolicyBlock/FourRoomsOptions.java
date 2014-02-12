package domain.PolicyBlock;

import java.util.ArrayList;
import domain.PolicyBlock.PolicyBlockDomain;
import burlap.behavior.PolicyBlock.TrajectoryGenerator;
import burlap.behavior.singleagent.options.Option;

public class FourRoomsOptions {

	/**
	 * Main Method that will run the simulation for Four Rooms
	 */
	public static void main(String[] args) {
		
		String trajectory_output = "Trajectory/";
		String original_output = "Original/";
		String options_output = "Options/";
		PolicyBlockDomain domain = new PolicyBlockDomain();
		TrajectoryGenerator trajectory = new TrajectoryGenerator(trajectory_output);
		
		//running the original domain via Q-Learning
		
		System.out.println("Running Q-learning");
		domain.QLearn(original_output);
		domain.visualize(original_output);
		
		//running trajectory generator with 3 generated policies
		System.out.println("Creating Trajectory Options...");
		trajectory.runSim(3);
		
		ArrayList<Option> options = trajectory.createOptions();
		System.out.println("Number of Options: " + options.size());
		domain.addOptions(options);
		
		//Running with Options
		System.out.println("\nRunning with Options");
		domain.QLearn(options_output);
		domain.visualize(options_output);
		
	}

}
