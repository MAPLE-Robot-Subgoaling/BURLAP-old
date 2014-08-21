package domain.blockdude;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.Visualizer;

public class BlockDudeVisualizer {

    public static Visualizer getVisualizer(int minx, int maxx, int miny,
	    int maxy) {

	Visualizer v = new Visualizer();

	v.addObjectClassPainter(BlockDudeDomain.CLASSAGENT, new AgentPainter(minx,
		maxx, miny, maxy));
	v.addObjectClassPainter(BlockDudeDomain.CLASSBLOCK, new BlockPainter(minx,
		maxx, miny, maxy));
	v.addObjectClassPainter(BlockDudeDomain.CLASSEXIT, new ExitPainter(minx,
		maxx, miny, maxy));
	v.addObjectClassPainter(BlockDudeDomain.CLASSPLATFORM, new PlatformPainter(
		minx, maxx, miny, maxy));

	return v;

    }

    public static class AgentPainter implements ObjectPainter {

	public int minx;
	public int miny;

	public int maxx;
	public int maxy;

	public AgentPainter(int minx, int maxx, int miny, int maxy) {

	    this.minx = minx;
	    this.miny = miny;

	    this.maxx = maxx;
	    this.maxy = maxy;
	}

	@Override
	public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
		float cWidth, float cHeight) {

	    g2.setColor(Color.blue);

	    float domainXScale = (maxx + 1) - minx;
	    float domainYScale = (maxy + 1) - miny;

	    // determine then normalized width
	    float width = (1.0f / domainXScale) * cWidth;
	    float height = (1.0f / domainYScale) * cHeight;

	    float rx = ob.getDiscValForAttribute(BlockDudeDomain.ATTX) * width;
	    float ry = cHeight - height
		    - ob.getDiscValForAttribute(BlockDudeDomain.ATTY) * height;

	    g2.fill(new Rectangle2D.Float(rx, ry, width, height));

	    // draw eye for showing the direction of the agent
	    g2.setColor(Color.orange);
	    float eyeWidth = width * 0.25f;
	    float eyeHeight = height * 0.25f;

	    float ex = rx;
	    if (ob.getDiscValForAttribute(BlockDudeDomain.ATTDIR) == 1) {
		ex = (rx + width) - eyeWidth;
	    }

	    float ey = ry + 0.2f * height;

	    g2.fill(new Rectangle2D.Float(ex, ey, eyeWidth, eyeHeight));

	}

    }

    public static class BlockPainter implements ObjectPainter {

	public int minx;
	public int miny;

	public int maxx;
	public int maxy;

	public BlockPainter(int minx, int maxx, int miny, int maxy) {

	    this.minx = minx;
	    this.miny = miny;

	    this.maxx = maxx;
	    this.maxy = maxy;
	}

	@Override
	public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
		float cWidth, float cHeight) {

	    g2.setColor(Color.gray);

	    float domainXScale = (maxx + 1) - minx;
	    float domainYScale = (maxy + 1) - miny;

	    // determine then normalized width
	    float width = (1.0f / domainXScale) * cWidth;
	    float height = (1.0f / domainYScale) * cHeight;

	    float rx = ob.getDiscValForAttribute(BlockDudeDomain.ATTX) * width;
	    float ry = cHeight - height
		    - ob.getDiscValForAttribute(BlockDudeDomain.ATTY) * height;

	    g2.fill(new Rectangle2D.Float(rx, ry, width, height));

	}

    }

    public static class ExitPainter implements ObjectPainter {

	public int minx;
	public int miny;

	public int maxx;
	public int maxy;

	public ExitPainter(int minx, int maxx, int miny, int maxy) {

	    this.minx = minx;
	    this.miny = miny;

	    this.maxx = maxx;
	    this.maxy = maxy;
	}

	@Override
	public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
		float cWidth, float cHeight) {

	    g2.setColor(Color.black);

	    float domainXScale = (maxx + 1) - minx;
	    float domainYScale = (maxy + 1) - miny;

	    // determine then normalized width
	    float width = (1.0f / domainXScale) * cWidth;
	    float height = (1.0f / domainYScale) * cHeight;

	    float rx = ob.getDiscValForAttribute(BlockDudeDomain.ATTX) * width;
	    float ry = cHeight - height
		    - ob.getDiscValForAttribute(BlockDudeDomain.ATTY) * height;

	    g2.fill(new Rectangle2D.Float(rx, ry, width, height));

	}

    }

    public static class PlatformPainter implements ObjectPainter {

	public int minx;
	public int miny;

	public int maxx;
	public int maxy;

	public PlatformPainter(int minx, int maxx, int miny, int maxy) {

	    this.minx = minx;
	    this.miny = miny;

	    this.maxx = maxx;
	    this.maxy = maxy;
	}

	@Override
	public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
		float cWidth, float cHeight) {

	    g2.setColor(Color.green);

	    float domainXScale = (maxx + 1) - minx;
	    float domainYScale = (maxy + 1) - miny;

	    float px = ob.getDiscValForAttribute(BlockDudeDomain.ATTX);
	    float ph = ob.getDiscValForAttribute(BlockDudeDomain.ATTHEIGHT);

	    // determine the normalized width
	    float width = (1.0f / domainXScale) * cWidth;
	    float height = (1.0f / domainYScale) * cHeight;

	    float rx = px * width;
	    float ry = cHeight - height - ph * height;

	    g2.fill(new Rectangle2D.Float(rx, ry, width, height * (ph + 1)));

	}

    }

}
