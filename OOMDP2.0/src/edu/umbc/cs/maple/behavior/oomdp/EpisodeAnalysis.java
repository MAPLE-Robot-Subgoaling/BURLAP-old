package edu.umbc.cs.maple.behavior.oomdp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.umbc.cs.maple.oomdp.Action;
import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.GroundedAction;
import edu.umbc.cs.maple.oomdp.NullAction;
import edu.umbc.cs.maple.oomdp.State;
import edu.umbc.cs.maple.oomdp.StateParser;


public class EpisodeAnalysis {

	public List<State>									stateSequence;
	public List<GroundedAction>							actionSequence;
	public List<Double>									rewardSequence;
	
	public EpisodeAnalysis(){
		stateSequence = new ArrayList<State>();
		actionSequence = new ArrayList<GroundedAction>();
		rewardSequence = new ArrayList<Double>();
	}
	
	public void addState(State s){
		stateSequence.add(s);
	}
	
	public void addAction(GroundedAction ga){
		actionSequence.add(ga);
	}
	
	public void addReward(double r){
		rewardSequence.add(r);
	}
	
	public void record(State next, GroundedAction a, double r){
		stateSequence.add(next);
		actionSequence.add(a);
		rewardSequence.add(r);
	}
	
	public State getState(int i){
		return stateSequence.get(i);
	}
	
	public GroundedAction getAction(int i){
		return actionSequence.get(i);
	}
	
	public double getReward(int i){
		return rewardSequence.get(i);
	}
	
	public int numTimeSteps(){
		return stateSequence.size(); //state sequence will always have the most because of initial state and terminal state
	}
	
	
	
	public void writeToFile(String path, StateParser sp){
		
		try{
			
			String str = this.parseIntoString(sp);
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			out.write(str);
			out.close();
			
			
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
	
	
	public String parseIntoString(StateParser sp){
		
		StringBuffer sbuf = new StringBuffer(256);
		
		for(int i = 0; i < stateSequence.size(); i++){
			
			sbuf.append("#EL#\n").append(sp.stateToString(stateSequence.get(i))).append("\n#ES#\n");
			if(i < stateSequence.size()-1){
				sbuf.append(getSpaceDelimGAString(actionSequence.get(i))).append("\n").append(rewardSequence.get(i)).append("\n");
			}
			
		}
		
		
		return sbuf.toString();
		
	}
	
	
	public static EpisodeAnalysis parseFileIntoEA(String path, Domain d, StateParser sp){
		
		//read whole file into string first
		String fcont = null;
		try{
			fcont = new Scanner(new File(path)).useDelimiter("\\Z").next();
		}catch(Exception E){
			System.out.println(E);
		}
		
		return parseStringIntoEA(fcont, d, sp);
	}
	
	
	public static EpisodeAnalysis parseStringIntoEA(String str, Domain d, StateParser sp){
		
		EpisodeAnalysis ea = new EpisodeAnalysis();
		
		String [] elComps = str.split("#EL#\n");
		
		//System.out.println("--" + elComps[0] + "--");
		
		for(int i = 1; i < elComps.length; i++){
			
			String spToken = "\n#ES#";
			if(!elComps[i].endsWith(spToken)){
				spToken += "\n";
			}
			
			String [] parts = elComps[i].split(spToken);
			
			State s = sp.stringToState(parts[0]);
			if(i < elComps.length-1){
				String [] ars = parts[1].split("\n");
				ea.record(s, getGAFromSpaceDelimGASTring(d, ars[0]), Double.parseDouble(ars[1]));
			}
			else{
				ea.addState(s);
			}
		}
		
		
		return ea;
	}
	
	
	private static GroundedAction getGAFromSpaceDelimGASTring(Domain d, String str){
		
		String [] scomps = str.split(" ");
		Action a = d.getAction(scomps[0]);
		if(a == null){
			//the domain does not have a reference, so create a null action in its place
			a = new NullAction(scomps[0]);
		}
		String [] params = new String[scomps.length-1];
		for(int i = 1; i < scomps.length; i++){
			params[i-1] = scomps[i];
		}
		
		return new GroundedAction(a, params);
	}
	
	private static String getSpaceDelimGAString(GroundedAction ga){
		StringBuffer sbuf = new StringBuffer(30);
		sbuf.append(ga.action.getName());
		for(int i = 0; i < ga.params.length; i++){
			sbuf.append(" ").append(ga.params[i]);
		}
		
		return sbuf.toString();
	}
	
	
	
}
