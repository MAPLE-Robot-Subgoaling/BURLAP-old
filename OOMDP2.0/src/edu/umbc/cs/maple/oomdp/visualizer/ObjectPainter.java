package edu.umbc.cs.maple.oomdp.visualizer;

import java.awt.Graphics2D;
import edu.umbc.cs.maple.oomdp.*;

public abstract class ObjectPainter {

	protected Domain 		domain_;
	
	
	public ObjectPainter(Domain domain){
		domain_ = domain;
	}
	
	public void setDomain(Domain domain){
		domain_ = domain;
	}
	
	
	
	/* g2: 				graphics context to which the object should be painted
	 * ob				the instantiated object to be painted
	 * cWidth/cHeight:	dimensions of the canvas size
	 */
	public abstract void paintObject(Graphics2D g2, ObjectInstance ob, float cWidth, float cHeight);
	
	
}
