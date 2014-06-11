package domain.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;



public class BlocksVisualizer {

	
	public static Visualizer getVisualizer(){
		
		BlocksDomain bd = new BlocksDomain();
		Domain d = bd.generateDomain();
		Visualizer v = new Visualizer();
		
		v.addStaticPainter(new BlocksPainter(d));
		v.addObjectClassPainter(BlocksDomain.CLASSBLOCK, new CellPainter(d, Color.cyan));
		v.addObjectClassPainter(BlocksDomain.CLASSCLAW, new CellPainter(d, Color.red));
		
		return v;
	}
	
	
	public static class BlocksPainter implements StaticPainter{

		
		public BlocksPainter(Domain domain) {
			
		}

		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
			
			//draw the walls; make them black
			g2.setColor(Color.black);
			
			float domainXScale = (BlocksDomain.TABLESPOTS + 1);
			float domainYScale = (BlocksDomain.NUMBLOCKS + 2);
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
		}
	}
	
	public static class CellPainter implements ObjectPainter{

		Color col;
		
		public CellPainter(Domain domain, Color col) {
			this.col = col;
		}

		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
				float cWidth, float cHeight) {
			
			
			//draw the walls; make them black
			g2.setColor(this.col);
			
			float domainXScale = (BlocksDomain.TABLESPOTS + 1);
			float domainYScale = (BlocksDomain.NUMBLOCKS + 2);
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			float rx = ob.getDiscValForAttribute(BlocksDomain.ATTX)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(BlocksDomain.ATTY)*height;
			
			g2.fill(new Rectangle2D.Float(rx + 5, ry + 5, width - 5, height - 5));

			g2.setColor(Color.black);
			g2.setFont(new Font("Arial", Font.BOLD, 22));
			g2.drawString(ob.getName().replace(ob.getTrueClassName(), ""), rx + width / 2, ry + height / 2);
			
		}
	}
}
