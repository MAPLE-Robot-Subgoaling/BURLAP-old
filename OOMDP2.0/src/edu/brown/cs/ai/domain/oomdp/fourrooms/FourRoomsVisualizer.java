package edu.brown.cs.ai.domain.oomdp.fourrooms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectInstance;
import edu.umbc.cs.maple.oomdp.visualizer.ObjectPainter;
import edu.umbc.cs.maple.oomdp.visualizer.StaticPainter;
import edu.umbc.cs.maple.oomdp.visualizer.Visualizer;

public class FourRoomsVisualizer {

	
	public static Visualizer getVisualizer(){
		
		FourRoomsDomain frd = new FourRoomsDomain();
		Domain d = frd.generateDomain();
		Visualizer v = new Visualizer();
		
		v.addStaticPainter(new RoomsMapPainter(d));
		v.addObjectClassPainter(FourRoomsDomain.CLASSGOAL, new CellPainter(d, Color.green));
		v.addObjectClassPainter(FourRoomsDomain.CLASSAGENT, new CellPainter(d, Color.red));
		
		return v;
	}
	
	
	public static class RoomsMapPainter extends StaticPainter{

		
		public RoomsMapPainter(Domain domain) {
			super(domain);
		}

		@Override
		public void paint(Graphics2D g2, float cWidth, float cHeight) {
			
			//draw the walls; make them black
			g2.setColor(Color.black);
			
			float domainXScale = (FourRoomsDomain.MAXX + 1);
			float domainYScale = (FourRoomsDomain.MAXY + 1);
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			//pass through each cell of the map and if it is a wall, draw it
			for(int i = 0; i <= FourRoomsDomain.MAXX; i++){
				for(int j = 0; j <= FourRoomsDomain.MAXY; j++){
					
					if(FourRoomsDomain.MAP[i][j] == 1){
					
						float rx = i*width;
						float ry = cHeight - height - j*height;
					
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
						
					}
					
				}
			}
			
		}
		
		
	}
	
	
	
	public static class CellPainter extends ObjectPainter{

		Color col;
		
		public CellPainter(Domain domain, Color col) {
			super(domain);
			this.col = col;
		}

		@Override
		public void paintObject(Graphics2D g2, ObjectInstance ob, float cWidth,
				float cHeight) {
			
			
			//draw the walls; make them black
			g2.setColor(this.col);
			
			float domainXScale = (FourRoomsDomain.MAXX + 1);
			float domainYScale = (FourRoomsDomain.MAXY + 1);
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(FourRoomsDomain.ATTX)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(FourRoomsDomain.ATTY)*height;
			
			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			
		}
		
		
		
		
	}
	
	
	
	

}
