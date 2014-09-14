package domain.sokoban;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.*;


/**
 * Javadoc test
 * @author Richard Adjogah
 * This is the visualizer for the Sokoban domain.
 *
 */
public class SokobanVisualizer {

	/**
	 * 
	 * @return the visualizer object 
	 */
	public static Visualizer getVisualizer(){

		SokobanDomain rd = new SokobanDomain();
		Domain d = rd.generateDomain();
		Visualizer v = new Visualizer();

		SokobanVisualizer rv = new SokobanVisualizer();

		v.addObjectClassPainter(SokobanDomain.ROOMCLASS, rv.new RoomPainter(d));
		v.addObjectClassPainter(SokobanDomain.AGENTCLASS, rv.new AgentPainter(d));
		v.addObjectClassPainter(SokobanDomain.BLOCKCLASS, rv.new BlockPainter(d));

		return v;
	}

	
	/**
	 * Note: Each individual object type should have its own painter class that determines what color to draw and where
	 * @author richard
	 *
	 */
	class RoomPainter implements ObjectPainter{

		public RoomPainter(Domain domain) {
		}

		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			float domainXScale = (SokobanDomain.MAXX + 1) - SokobanDomain.MINX;
			float domainYScale = (SokobanDomain.MAXY + 1) - SokobanDomain.MINY;
			
			//determines which java Color to use from a String. Defaults to Dark Gray if the string isn't a valid color
			String color = ob.getStringValForAttribute(SokobanDomain.COLORATTNAME);
			
			Field field;
			try {
				field = Class.forName("java.awt.Color").getField(color);
				g2.setColor((Color)field.get(null));

			} catch (Exception e) {
				g2.setColor(Color.darkGray);
			}

			float topX = ob.getDiscValForAttribute(SokobanDomain.TOPXATTNAME);
			float bottomX = ob.getDiscValForAttribute(SokobanDomain.BOTTOMXATTNAME);
			float topY = ob.getDiscValForAttribute(SokobanDomain.TOPYATTNAME);
			float bottomY = ob.getDiscValForAttribute(SokobanDomain.BOTTOMYATTNAME);
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			for(int i = SokobanDomain.MINX; i <= SokobanDomain.MAXX; i++){
				for(int j = SokobanDomain.MINY; j <= SokobanDomain.MAXY; j++){
					if(i <= bottomX && i >= topX && j >= bottomY && j <= topY){
						
						float rx = i*width;
						float ry = cHeight - height - j*height;

						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
					}			
				}
			}
			
			g2.setColor(Color.black);

			for(int i = SokobanDomain.MINX; i <= SokobanDomain.MAXX; i++){
				for(int j = SokobanDomain.MINY; j <= SokobanDomain.MAXY; j++){
					if(SokobanDomain.MAP[i][j] == 1){

						float rx = i*width;
						float ry = cHeight - height - j*height;

						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
					}
				}
			}
		}
	}

	/**
	 * Note: Each individual object type should have its own painter class that determines what color to draw and where
	 * @author richard
	 *
	 */
	class AgentPainter implements ObjectPainter{

		public AgentPainter(Domain domain) {
		}

		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {

			//make agent gray
			g2.setColor(Color.gray);

			float domainXScale = (SokobanDomain.MAXX + 1) - SokobanDomain.MINX;
			float domainYScale = (SokobanDomain.MAXY + 1) - SokobanDomain.MINY;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = ob.getDiscValForAttribute(SokobanDomain.XATTNAME)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(SokobanDomain.YATTNAME)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
		}
	}

	/**
	 * Note: Each individual object type should have its own painter class that determines what color to draw and where
	 * @author richard
	 *
	 */
	class GoalPainter implements ObjectPainter{

		public GoalPainter(Domain domain) {
		}

		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {

			g2.setColor(Color.blue);

			float domainXScale = (SokobanDomain.MAXX + 1) - SokobanDomain.MINX;
			float domainYScale = (SokobanDomain.MAXY + 1) - SokobanDomain.MINY;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = ob.getDiscValForAttribute(SokobanDomain.XATTNAME)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(SokobanDomain.YATTNAME)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
		}
	}

	class BlockPainter implements ObjectPainter{

		public BlockPainter (Domain domain) {
		}

		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {

			String color = ob.getStringValForAttribute(SokobanDomain.COLORATTNAME);
			
			Field field;
			try {
				field = Class.forName("java.awt.Color").getField(color);
				g2.setColor((Color)field.get(null));

			} catch (Exception e) {
				g2.setColor(Color.darkGray);
			}
			
			float domainXScale = (SokobanDomain.MAXX + 1) - SokobanDomain.MINX;
			float domainYScale = (SokobanDomain.MAXY + 1) - SokobanDomain.MINY;

			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;

			float rx = ob.getDiscValForAttribute(SokobanDomain.XATTNAME)*width;
			float ry = cHeight - height - ob.getDiscValForAttribute(SokobanDomain.YATTNAME)*height;

			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
		}
	}
}
