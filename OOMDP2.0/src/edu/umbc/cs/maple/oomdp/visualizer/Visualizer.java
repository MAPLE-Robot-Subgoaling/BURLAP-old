package edu.umbc.cs.maple.oomdp.visualizer;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;
import edu.umbc.cs.maple.oomdp.*;

public class Visualizer extends Canvas{

	private static final long serialVersionUID = 1L; //needed for Canvas extension

	
	private State							curState_;					//the current state to be painted next
	
	private List <StaticPainter>			staticPainters_;			//list of static painters that pain static non-object defined properties of the domain
	private Map <String, ObjectPainter>		objectClassPainters_;		//Map of painters that define how to paint each object class
	private Map <String, ObjectPainter>		specificObjectPainters_;	//Map of painters that define how to paint specific objects; if an object it appears in both specific and general lists, the specific painter is used
	
	private Color							bgColor_;					//the background color of the canvas
	
	
	
	public Visualizer(){
		
		curState_ = null;
		
		staticPainters_ = new ArrayList <StaticPainter>();
		objectClassPainters_ = new HashMap <String, ObjectPainter>();
		specificObjectPainters_ = new HashMap <String, ObjectPainter>();
		
		bgColor_ = Color.white;
		
	}
	
	public void setBGColor(Color c){
		bgColor_ = c;
	}
	
	public void addStaticPainter(StaticPainter sp){
		staticPainters_.add(sp);
	}
	
	public void addObjectClassPainter(String className, ObjectPainter op){
		objectClassPainters_.put(className, op);
	}
	
	public void addSpecificObjectPainter(String objectName, ObjectPainter op){
		specificObjectPainters_.put(objectName, op);
	}
	
	public void updateState(State st){
		curState_ = st;
		repaint();
	}
	
	public void paint(Graphics g){
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(bgColor_);
		g2.fill(new Rectangle(this.getWidth(), this.getHeight()));
		
		if(curState_ == null){
			return ;
		}
		
		float cWidth = (float)this.getWidth();
		float cHeight = (float)this.getHeight();
		
		//draw the static properties
		for(StaticPainter sp : staticPainters_){
			sp.paint(g2, cWidth, cHeight);
		}
		
		//draw each object if there is a painter to do so
		List <ObjectInstance> objects = curState_.getAllObjects();
		for(ObjectInstance o : objects){
			
			//is there a specific object painter for this object?
			if(specificObjectPainters_.containsKey(o.getName())){
				specificObjectPainters_.get(o.getName()).paintObject(g2, o, cWidth, cHeight);
			}
			else{ //otherwise see if we have a painter for this object's class
				
				//try the parameterized class first
				if(objectClassPainters_.containsKey(o.getPseudoClass())){
					objectClassPainters_.get(o.getPseudoClass()).paintObject(g2, o, cWidth, cHeight);
				}
				else if(objectClassPainters_.containsKey(o.getTrueClassName())){ //try true class if no entry for the parameterized class
					objectClassPainters_.get(o.getTrueClassName()).paintObject(g2, o, cWidth, cHeight);
				}
				
			}
			
		}
		
	}
	
	
}
