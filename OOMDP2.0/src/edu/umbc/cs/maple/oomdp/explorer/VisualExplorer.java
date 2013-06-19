package edu.umbc.cs.maple.oomdp.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.*;
import javax.swing.JFrame;
import edu.umbc.cs.maple.oomdp.*;
//import edu.umbc.cs.maple.oomdp.logical.LogicalExpression;
//import edu.umbc.cs.maple.oomdp.logical.Scope;
import edu.umbc.cs.maple.oomdp.visualizer.*;

public class VisualExplorer extends JFrame{

	private static final long serialVersionUID = 1L;
	
	
	private Domain									domain_;
	private Map <String, String>					keyActionMap_;
	private Map <String, SpecialExplorerAction>		keySpecialMap_;
	State											baseState_;
	State											curState_;
	
	Visualizer 										painter_;
	TextArea										propViewer_;
	int												cWidth_;
	int												cHeight_;
	
	int												numSteps;
	
	
	public VisualExplorer(Domain domain, Visualizer painter, State baseState){
		
		this.init(domain, painter, baseState, 800, 800);
	}
	
	public VisualExplorer(Domain domain, Visualizer painter, State baseState, int w, int h){
		this.init(domain, painter, baseState, w, h);
	}
	
	public void init(Domain domain, Visualizer painter, State baseState, int w, int h){
		
		domain_ = domain;
		baseState_ = baseState;
		curState_ = baseState.copy();
		painter_ = painter;
		keyActionMap_ = new HashMap <String, String>();
		keySpecialMap_ = new HashMap <String, SpecialExplorerAction>();
		
		StateResetSpecialAction reset = new StateResetSpecialAction(baseState_);
		this.addSpecialAction("`", reset);
		
		cWidth_ = w;
		cHeight_ = h;
		
		propViewer_ = new TextArea();
		propViewer_.setEditable(false);
		
		numSteps = 0;
		
	}
	
	public StateResetSpecialAction getResetSpecialAction(){
		return (StateResetSpecialAction)keySpecialMap_.get("`");
	}
	
	public void addKeyAction(String key, String action){
		keyActionMap_.put(key, action);
	}
	
	public void addSpecialAction(String key, SpecialExplorerAction action){
		keySpecialMap_.put(key, action);
	}
	
	public void initGUI(){
		
		painter_.setPreferredSize(new Dimension(cWidth_, cHeight_));
		propViewer_.setPreferredSize(new Dimension(cWidth_, 100));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().add(propViewer_, BorderLayout.SOUTH);
		getContentPane().add(painter_, BorderLayout.CENTER);
	
		
		addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		//also add key listener to the painter in case the focus is changed
		painter_.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		propViewer_.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		painter_.updateState(baseState_);
		
		pack();
		setVisible(true);
		
		
		
		
	}
	
	
	private void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());
		

		//otherwise this could be an action, see if there is an action mapping
		String mappedAction = keyActionMap_.get(key);
		if(mappedAction != null){
			
			//then we have a action for this key
			//split the string up into components
			String [] comps = mappedAction.split(" ");
			String actionName = comps[0];
			
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
			
			Action action = domain_.getAction(actionName);
			if(action == null){
				System.out.println("Unknown action: " + actionName);
			}
			else{
				curState_ = action.performAction(curState_, params);
				numSteps++;
			}
			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap_.get(key);
			if(sea != null){
				curState_ = sea.applySpecialAction(curState_);
			}
			if(sea instanceof StateResetSpecialAction){
				System.out.println("Number of steps before reset: " + numSteps);
				numSteps = 0;
			}
		}
				
			

		
		//now paint the screen with the new state
		painter_.updateState(curState_);
		this.updatePropTextArea(curState_);
		//System.out.println(curState_.getStateDescription());
		//System.out.println("-------------------------------------------");
		
		
	}
	
	private void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
		List <PropositionalFunction> props = domain_.getPropFunctions();
		for(PropositionalFunction pf : props){
			List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		

		propViewer_.setText(buf.toString());
		
		
	}
	
	
	

	
	
}
