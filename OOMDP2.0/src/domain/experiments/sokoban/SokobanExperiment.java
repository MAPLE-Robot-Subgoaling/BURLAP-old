package domain.experiments.sokoban;

import java.util.ArrayList;
import domain.singleagent.sokoban2.Sokoban2Domain;
import domain.singleagent.sokoban2.Sokoban2Visualizer;
import domain.singleagent.sokoban2.Sokoban2Parser;
import burlap.behavior.PolicyBlock.TrajectoryGenerator;
import burlap.behavior.singleagent.options.Option;

public class SokobanExperiment {

	String configA = "RedChair/";
	String configB = "BlueBackpack/";
	String configC = "NoObject/";
	
	public static void main(String[] args){
		Sokoban2Domain dgen = new Sokoban2Domain();
		dgen.includeDirectionAttribute(true); 
		
	}
	
	
	
}
