package domain.gridworld;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import oomdptb.oomdp.Domain;
import oomdptb.oomdp.ObjectInstance;
import oomdptb.oomdp.State;
import oomdptb.oomdp.visualizer.ObjectPainter;
import oomdptb.oomdp.visualizer.StaticPainter;
import oomdptb.oomdp.visualizer.Visualizer;

public class GridWorldVisualizer {

	
	
	public static Visualizer getVisualizer(Domain d, int [][] map){
		
		Visualizer v = new Visualizer();
		
		v.addStaticPainter(new MapPainter(d, map));
		v.addObjectClassPainter(GridWorldDomain.CLASSLOCATION, new CellPainter(d, Color.blue, map));
		v.addObjectClassPainter(GridWorldDomain.CLASSAGENT, new CellPainter(d, Color.red, map));
		
		return v;
	}
	
	
	
	public static class MapPainter extends StaticPainter{

		protected int 				dwidth;
		protected int 				dheight;
		protected int [][] 			map;
		
		public MapPainter(Domain domain, int [][] map) {
			super(domain);
			this.dwidth = map.length;
			this.dheight = map[0].length;
			this.map = map;
		}

		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
			
			//draw the walls; make them black
			g2.setColor(Color.black);
			
			float domainXScale = this.dwidth;
			float domainYScale = this.dheight;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			//pass through each cell of the map and if it is a wall, draw it
			for(int i = 0; i < this.dwidth; i++){
				for(int j = 0; j < this.dheight; j++){
					
					if(this.map[i][j] == 1){
					
						float rx = i*width;
						float ry = cHeight - height - j*height;
					
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
						
					}
					
				}
			}
			
		}
		
		
	}
	
	
	
	public static class CellPainter extends ObjectPainter{

		protected Color			col;
		protected int			dwidth;
		protected int			dheight;
		protected int [][]		map;
		
		public CellPainter(Domain domain, Color col, int [][] map) {
			super(domain);
			this.col = col;
			this.dwidth = map.length;
			this.dheight = map[0].length;
			this.map = map;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			
			//set the color of the object
			g2.setColor(this.col);
			
			float domainXScale = this.dwidth;
			float domainYScale = this.dheight;
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(GridWorldDomain.ATTX)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(GridWorldDomain.ATTY)*height;
			
			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			
		}
		
		
		
		
	}
	
	
}
