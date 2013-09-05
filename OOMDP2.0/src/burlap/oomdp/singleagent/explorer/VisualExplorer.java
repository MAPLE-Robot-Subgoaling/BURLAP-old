package burlap.oomdp.singleagent.explorer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.*;
import javax.swing.JFrame;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.visualizer.Visualizer;


public class VisualExplorer extends JFrame{

	private static final long serialVersionUID = 1L;
	
	
	private Domain									domain;
	private Map <String, String>					keyActionMap;
	private Map <String, SpecialExplorerAction>		keySpecialMap;
	State											baseState;
	State											curState;
	
	Visualizer 										painter;
	TextArea										propViewer;
	int												cWidth;
	int												cHeight;
	
	int												numSteps;
	
	
	public VisualExplorer(Domain domain, Visualizer painter, State baseState){
		
		this.init(domain, painter, baseState, 800, 800);
	}
	
	public VisualExplorer(Domain domain, Visualizer painter, State baseState, int w, int h){
		this.init(domain, painter, baseState, w, h);
	}
	
	public void init(Domain domain, Visualizer painter, State baseState, int w, int h){
		
		this.domain = domain;
		this.baseState = baseState;
		this.curState = baseState.copy();
		this.painter = painter;
		this.keyActionMap = new HashMap <String, String>();
		this.keySpecialMap = new HashMap <String, SpecialExplorerAction>();
		
		StateResetSpecialAction reset = new StateResetSpecialAction(this.baseState);
		this.addSpecialAction("`", reset);
		
		this.cWidth = w;
		this.cHeight = h;
		
		this.propViewer = new TextArea();
		this.propViewer.setEditable(false);
		
		this.numSteps = 0;
		
	}
	
	public StateResetSpecialAction getResetSpecialAction(){
		return (StateResetSpecialAction)keySpecialMap.get("`");
	}
	
	public void addKeyAction(String key, String action){
		keyActionMap.put(key, action);
	}
	
	public void addSpecialAction(String key, SpecialExplorerAction action){
		keySpecialMap.put(key, action);
	}
	
	public void initGUI(){
		
		painter.setPreferredSize(new Dimension(cWidth, cHeight));
		propViewer.setPreferredSize(new Dimension(cWidth, 100));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		getContentPane().add(propViewer, BorderLayout.SOUTH);
		getContentPane().add(painter, BorderLayout.CENTER);
	
		
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
		painter.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		propViewer.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {	
			}
			public void keyTyped(KeyEvent e) {
				handleKeyPressed(e);
			}

		});
		
		painter.updateState(baseState);
		
		pack();
		setVisible(true);
	}
	
	
	private void handleKeyPressed(KeyEvent e){
		
		String key = String.valueOf(e.getKeyChar());

		//otherwise this could be an action, see if there is an action mapping
		String mappedAction = keyActionMap.get(key);
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
			
			Action action = domain.getAction(actionName);
			if(action == null){
				System.out.println("Unknown action: " + actionName);
			}
			else{
				curState = action.performAction(curState, params);
				numSteps++;
			}
			
		}
		else{
			
			SpecialExplorerAction sea = keySpecialMap.get(key);
			if(sea != null){
				curState = sea.applySpecialAction(curState);
			}
			if(sea instanceof StateResetSpecialAction){
				System.out.println("Number of steps before reset: " + numSteps);
				numSteps = 0;
			}
		}
				
		
		//now paint the screen with the new state
		painter.updateState(curState);
		this.updatePropTextArea(curState);
		//System.out.println(curState_.getStateDescription());
		//System.out.println("-------------------------------------------");
		
		
	}
	
	private void updatePropTextArea(State s){
		
		StringBuffer buf = new StringBuffer();
		
		List <PropositionalFunction> props = domain.getPropFunctions();
		for(PropositionalFunction pf : props){
			List<GroundedProp> gps = s.getAllGroundedPropsFor(pf);
			for(GroundedProp gp : gps){
				if(gp.isTrue(s)){
					buf.append(gp.toString()).append("\n");
				}
			}
		}
		propViewer.setText(buf.toString());
		
		
	}
	
	
	

	
	
}
