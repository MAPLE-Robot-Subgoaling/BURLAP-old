/* Author: James MacGlashan
 * Description:
 * This class is defines the interface to which a painter that paints static properties of a
 * domain should adhere. That is, how properties of a domain that are not expressed in the
 * objects should be painted to the screen. Consider a Maze world with objects with which
 * the agents should interact. In this case the walls of the maze is not represented
 * by any of the instantiated objects, but should still be rendered to the screen
 * when visualizing this domain. A StaticPainter class should be sub classed that
 * defines how to do this
 */



package edu.umbc.cs.maple.oomdp.visualizer;

import java.awt.Graphics2D;
import edu.umbc.cs.maple.oomdp.*;


public abstract class StaticPainter {
	
	protected Domain 		domain_;
	
	public StaticPainter(Domain domain){
		domain_ = domain;
	}
	
	public void setDomain(Domain domain){
		domain_ = domain;
	}
	
	/* g2: 				graphics context to which the static data should be painted
	 * cWidth/cHeight:	dimensions of the canvas size
	 */
	public abstract void paint(Graphics2D g2, float cWidth, float cHeight);

}
