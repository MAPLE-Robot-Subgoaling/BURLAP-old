package edu.umbc.cs.maple.domain.oomdp.lunarlander;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import edu.umbc.cs.maple.oomdp.Domain;
import edu.umbc.cs.maple.oomdp.ObjectInstance;
import edu.umbc.cs.maple.oomdp.visualizer.ObjectPainter;
import edu.umbc.cs.maple.oomdp.visualizer.Visualizer;


public class LLVisualizer {

	
	public static Visualizer getVisualizer(){
		
		LunarLanderDomain lld = new LunarLanderDomain();
		Domain d = lld.generateDomain();
		Visualizer v = new Visualizer();
		
		
		v.addObjectClassPainter(LunarLanderDomain.AGENTCLASS, new AgentPainter(d));
		v.addObjectClassPainter(LunarLanderDomain.OBSTACLECLASS, new ObstaclePainter(d));
		v.addObjectClassPainter(LunarLanderDomain.PADCLASS, new PadPainter(d));
		
		return v;
	}
	
	
	
	public static class AgentPainter extends ObjectPainter{

		public AgentPainter(Domain domain) {
			super(domain);
		}

		@Override
		public void paintObject(Graphics2D g2, ObjectInstance ob, float cWidth, float cHeight) {
			
			g2.setColor(Color.red);
			
			
			
			double width = 30.;
			double height = 40.;
			
			double ox = ob.getRealValForAttribute(LunarLanderDomain.XATTNAME);
			double oy = ob.getRealValForAttribute(LunarLanderDomain.YATTNAME);
			
			double ang = ob.getRealValForAttribute(LunarLanderDomain.AATTNAME);
			
			double nx = (ox - LunarLanderDomain.XMIN) / (LunarLanderDomain.XMAX - LunarLanderDomain.XMIN);
			double ny = (oy - LunarLanderDomain.YMIN) / (LunarLanderDomain.YMAX - LunarLanderDomain.YMIN);
			
			
			double scx = (nx * cWidth);
			double scy = cHeight - (ny * (cHeight));
			
			
			
			double tl = -width/2.;
			double tr = width/2.;
			double tb = -height/2.;
			double tt = height/2.;
			
			
			//ang = Math.PI / 4.;
			
			double cosang = Math.cos(-ang);
			double sinang = Math.sin(-ang);
			
			//top left
			double x0 = (tl * cosang) - (tt * sinang);
			double y0 = (tt * cosang) + (tl * sinang);

			//top right
			double x1 = (tr * cosang) - (tt * sinang);
			double y1 = (tt * cosang) + (tr * sinang);

			//bottom right
			double x2 = (tr * cosang) - (tb * sinang);
			double y2 = (tb * cosang) + (tr * sinang);

			//bottom left
			double x3 = (tl * cosang) - (tb * sinang);
			double y3 = (tb * cosang) + (tl * sinang);
			
			
			double ty0 = -y0;
			double ty1 = -y1;
			double ty2 = -y2;
			double ty3 = -y3;
			
			
			double sx0 = x0 + scx;
			double sy0 = ty0 + scy;
			
			double sx1 = x1 + scx;
			double sy1 = ty1 + scy;
			
			double sx2 = x2 + scx;
			double sy2 = ty2 + scy;
			
			double sx3 = x3 + scx;
			double sy3 = ty3 + scy;
			
			
			Path2D.Double mypath = new Path2D.Double();
			mypath.moveTo(sx0, sy0);
			mypath.lineTo(sx1, sy1);
			mypath.lineTo(sx2, sy2);
			mypath.lineTo(sx3, sy3);
			mypath.lineTo(sx0, sy0);
			mypath.closePath();
			
			g2.fill(mypath);
			
			
			
		}
		
		
		
	}
	
	public static class ObstaclePainter extends ObjectPainter{

		public ObstaclePainter(Domain domain) {
			super(domain);
		}

		@Override
		public void paintObject(Graphics2D g2, ObjectInstance ob, float cWidth,
				float cHeight) {
			
			g2.setColor(Color.black);
			
			double ol = ob.getRealValForAttribute(LunarLanderDomain.LATTNAME);
			double or = ob.getRealValForAttribute(LunarLanderDomain.RATTNAME);
			double obb = ob.getRealValForAttribute(LunarLanderDomain.BATTNAME);
			double ot = ob.getRealValForAttribute(LunarLanderDomain.TATTNAME);
			
			double ow = or - ol;
			double oh = ot - obb;
			
			double xr = (LunarLanderDomain.XMAX - LunarLanderDomain.XMIN);
			double yr = (LunarLanderDomain.YMAX - LunarLanderDomain.YMIN);
			
			double nl = (ol - LunarLanderDomain.XMIN) / xr;
			double nt = (ot - LunarLanderDomain.YMIN) / yr;
			
			double nw = ow/xr;
			double nh = oh/yr;
			
			double sx = (nl*cWidth);
			double sy = cHeight - (nt*cHeight);
			
			double sw = nw*cWidth;
			double sh = nh*cHeight;
			
			g2.fill(new Rectangle2D.Double(sx, sy, sw, sh));
			
			
		}
		
		
	}
	
	
	
	public static class PadPainter extends ObjectPainter{

		public PadPainter(Domain domain) {
			super(domain);
		}

		@Override
		public void paintObject(Graphics2D g2, ObjectInstance ob, float cWidth,
				float cHeight) {
			
			g2.setColor(Color.blue);
			
			double ol = ob.getRealValForAttribute(LunarLanderDomain.LATTNAME);
			double or = ob.getRealValForAttribute(LunarLanderDomain.RATTNAME);
			double obb = ob.getRealValForAttribute(LunarLanderDomain.BATTNAME);
			double ot = ob.getRealValForAttribute(LunarLanderDomain.TATTNAME);
			
			double ow = or - ol;
			double oh = ot - obb;
			
			double xr = (LunarLanderDomain.XMAX - LunarLanderDomain.XMIN);
			double yr = (LunarLanderDomain.YMAX - LunarLanderDomain.YMIN);
			
			double nl = (ol - LunarLanderDomain.XMIN) / xr;
			double nt = (ot - LunarLanderDomain.YMIN) / yr;
			
			double nw = ow/xr;
			double nh = oh/yr;
			
			double sx = (nl*cWidth);
			double sy = cHeight - (nt*cHeight);
			
			double sw = nw*cWidth;
			double sh = nh*cHeight;
			
			g2.fill(new Rectangle2D.Double(sx, sy, sw, sh));
			
			
		}
		
		
	}
	
}