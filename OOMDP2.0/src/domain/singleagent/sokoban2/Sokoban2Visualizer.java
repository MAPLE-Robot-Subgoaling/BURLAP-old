package domain.singleagent.sokoban2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

public class Sokoban2Visualizer {

	
	
	public static Visualizer getVisualizer(){
		
		Visualizer v = new Visualizer();
		v.addObjectClassPainter(Sokoban2Domain.CLASSROOM, new RoomPainter());
		v.addObjectClassPainter(Sokoban2Domain.CLASSDOOR, new DoorPainter());
		v.addObjectClassPainter(Sokoban2Domain.CLASSAGENT, new AgentPainter());
		v.addObjectClassPainter(Sokoban2Domain.CLASSBLOCK, new BlockPainter());
		
		return v;
		
	}
	
	public static StateRenderLayer getStateRenderLayer(){
		
		StateRenderLayer v = new StateRenderLayer();
		
		v.addObjectClassPainter(Sokoban2Domain.CLASSROOM, new RoomPainter());
		v.addObjectClassPainter(Sokoban2Domain.CLASSDOOR, new DoorPainter());
		v.addObjectClassPainter(Sokoban2Domain.CLASSAGENT, new AgentPainter());
		v.addObjectClassPainter(Sokoban2Domain.CLASSBLOCK, new BlockPainter());
		
		return v;
		
	}
	
	
	public static Visualizer getVisualizer(int maxX, int maxY){
		
		Visualizer v = new Visualizer();
		
		v.addObjectClassPainter(Sokoban2Domain.CLASSROOM, new RoomPainter(maxX, maxY));
		v.addObjectClassPainter(Sokoban2Domain.CLASSDOOR, new DoorPainter(maxX, maxY));
		v.addObjectClassPainter(Sokoban2Domain.CLASSAGENT, new AgentPainter(maxX, maxY));
		v.addObjectClassPainter(Sokoban2Domain.CLASSBLOCK, new BlockPainter(maxX, maxY));
		
		return v;
		
	}
	
	
	public static StateRenderLayer getStateRenderLayer(int maxX, int maxY){
		
		StateRenderLayer v = new StateRenderLayer();
		
		v.addObjectClassPainter(Sokoban2Domain.CLASSROOM, new RoomPainter(maxX, maxY));
		v.addObjectClassPainter(Sokoban2Domain.CLASSDOOR, new DoorPainter(maxX, maxY));
		v.addObjectClassPainter(Sokoban2Domain.CLASSAGENT, new AgentPainter(maxX, maxY));
		v.addObjectClassPainter(Sokoban2Domain.CLASSBLOCK, new BlockPainter(maxX, maxY));
		
		return v;
		
	}
	
	
	
	
	public static class RoomPainter implements ObjectPainter{

		protected int maxX = -1;
		protected int maxY = -1;
		
		public RoomPainter(){
			
		}
		
		public RoomPainter(int maxX, int maxY){
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			float domainXScale = Sokoban2Domain.maxRoomXExtent(s) + 1f;
			float domainYScale = Sokoban2Domain.maxRoomYExtent(s) + 1f;
			
			if(maxX != -1){
				domainXScale = maxX;
				domainYScale = maxY;
			}
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			int top = ob.getDiscValForAttribute(Sokoban2Domain.ATTTOP);
			int left = ob.getDiscValForAttribute(Sokoban2Domain.ATTLEFT);
			int bottom = ob.getDiscValForAttribute(Sokoban2Domain.ATTBOTTOM);
			int right = ob.getDiscValForAttribute(Sokoban2Domain.ATTRIGHT);
			
			Color rcol = colorForName(ob.getStringValForAttribute(Sokoban2Domain.ATTCOLOR));
			
			for(int i = left; i <= right; i++){
				for(int j = bottom; j <= top; j++){
					
					float rx = i*width;
					float ry = cHeight - height - j*height;
					
					if(i == left || i == right || j == bottom || j == top){
						if(Sokoban2Domain.doorContainingPoint(s, i, j) == null){
							g2.setColor(Color.black);
							g2.fill(new Rectangle2D.Float(rx, ry, width, height));
						}
					}
					else{
						g2.setColor(rcol);
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));
					}
				}
			}
			
		}
		
	}
	
	
	public static class DoorPainter implements ObjectPainter{

		protected int maxX = -1;
		protected int maxY = -1;
		
		public DoorPainter(){
			
		}
		
		public DoorPainter(int maxX, int maxY){
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			float domainXScale = Sokoban2Domain.maxRoomXExtent(s) + 1f;
			float domainYScale = Sokoban2Domain.maxRoomYExtent(s) + 1f;
			
			if(maxX != -1){
				domainXScale = maxX;
				domainYScale = maxY;
			}
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			int top = ob.getDiscValForAttribute(Sokoban2Domain.ATTTOP);
			int left = ob.getDiscValForAttribute(Sokoban2Domain.ATTLEFT);
			int bottom = ob.getDiscValForAttribute(Sokoban2Domain.ATTBOTTOM);
			int right = ob.getDiscValForAttribute(Sokoban2Domain.ATTRIGHT);
			
			g2.setColor(Color.white);
			
			for(int i = left; i <= right; i++){
				for(int j = bottom; j <= top; j++){
					
					float rx = i*width;
					float ry = cHeight - height - j*height;
					g2.fill(new Rectangle2D.Float(rx, ry, width, height));
					
				}
			}
			
			
		}

	}
	
	
	
	public static class AgentPainter implements ObjectPainter{

		protected int maxX = -1;
		protected int maxY = -1;
		
		public AgentPainter(){
			
		}
		
		public AgentPainter(int maxX, int maxY){
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			g2.setColor(Color.darkGray);
			
			float domainXScale = Sokoban2Domain.maxRoomXExtent(s) + 1f;
			float domainYScale = Sokoban2Domain.maxRoomYExtent(s) + 1f;
			
			if(maxX != -1){
				domainXScale = maxX;
				domainYScale = maxY;
			}
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			int x = ob.getDiscValForAttribute(Sokoban2Domain.ATTX);
			int y = ob.getDiscValForAttribute(Sokoban2Domain.ATTY);
			
			float rx = x*width;
			float ry = cHeight - height - y*height;
			
			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			
		}
		
		
		
	}
	
	
	public static class BlockPainter implements ObjectPainter{

		protected int maxX = -1;
		protected int maxY = -1;
		
		public BlockPainter(){
			
		}
		
		public BlockPainter(int maxX, int maxY){
			this.maxX = maxX;
			this.maxY = maxY;
		}
		
		@Override
		public void paintObject(Graphics2D g2, State s, ObjectInstance ob, float cWidth, float cHeight) {
			
			float domainXScale = Sokoban2Domain.maxRoomXExtent(s) + 1f;
			float domainYScale = Sokoban2Domain.maxRoomYExtent(s) + 1f;
			
			if(maxX != -1){
				domainXScale = maxX;
				domainYScale = maxY;
			}
			
			//determine then normalized width
			float width = (1.0f / domainXScale) * cWidth;
			float height = (1.0f / domainYScale) * cHeight;
			
			int x = ob.getDiscValForAttribute(Sokoban2Domain.ATTX);
			int y = ob.getDiscValForAttribute(Sokoban2Domain.ATTY);
			
			float rx = x*width;
			float ry = cHeight - height - y*height;
			
			Color col = colorForName(ob.getStringValForAttribute(Sokoban2Domain.ATTCOLOR)).darker();
			
			g2.setColor(col);
			
			//TODO: handle different shapes differently
			
			g2.fill(new Rectangle2D.Float(rx, ry, width, height));
			
		}
		
		
		
	}
	
	
	
	protected static Color colorForName(String colName){
		
		Color col = Color.darkGray; //default color
		
		Field field;
		try {
			field = Class.forName("java.awt.Color").getField(colName);
			col = (Color)field.get(null);

		} catch (Exception e) {
		}
		
		return col;
	}
	
}
