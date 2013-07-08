package oomdptb.oomdp.explorer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

import oomdptb.oomdp.Action;
import oomdptb.oomdp.Domain;
import oomdptb.oomdp.State;

public class TerminalExplorer {
	
	private Domain					domain;
	private Map <String, String>	actionShortHand;
	
	public TerminalExplorer(Domain domain){
		this.domain = domain;
		this.setActionShortHand(new HashMap <String, String>());
	}
	
	public TerminalExplorer(Domain domain, Map <String, String> ash){
		this.domain = domain;
		this.setActionShortHand(ash);
	}
	
	public void setActionShortHand(Map <String, String> ash){
		this.actionShortHand = ash;
		List <Action> actionList = domain.getActions();
		for(Action a : actionList){
			this.addActionShortHand(a.getName(), a.getName());
		}
	}
	
	public void addActionShortHand(String shortHand, String action){
		actionShortHand.put(shortHand, action);
	}
	
	public void exploreFromState(State st){
		
		State src = st.copy();
		String actionPromptDelimiter = "-----------------------------------";
		
		while(true){
			
			this.printState(st);
			
			System.out.println(actionPromptDelimiter);
			
			BufferedReader in;
			String line;
			try{
			
				in = new BufferedReader(new InputStreamReader(System.in));
				line = in.readLine();
				
				if(line.equals("##reset##")){
					st = src;
				}
				else{
					
					//split the string up into components
					String [] comps = line.split(" ");
					String actionName = actionShortHand.get(comps[0]);
					
					if(actionName == null){
						actionName = comps[0];
					}
					
					//construct parameter list as all that remains
					String params[];
					if(comps.length > 1){
						params = new String[comps.length-1];
						for(int i = 1; i < comps.length; i++){
							params[i-1] = comps[i];
						}
					}
					else{
						params = new String[0];
					}
					
					Action action = domain.getAction(actionName);
					if(action == null){
						System.out.println("Unknown action: " + actionName);
					}
					else{
						st = action.performAction(st, params);
					}
					
				}
				
				System.out.println(actionPromptDelimiter);
				
			}
				
			catch(Exception e){
				System.out.println(e);
			}
			
		}
		
		
	}
	
	public void printState(State st){
		
		System.out.println(st.getStateDescription());
		
	}
	

}
