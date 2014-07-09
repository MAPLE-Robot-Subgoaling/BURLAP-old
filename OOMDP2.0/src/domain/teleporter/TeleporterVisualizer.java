package domain.teleporter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;

public class TeleporterVisualizer {
    public static Visualizer getVisualizer() {
        TeleporterDomain tpd = new TeleporterDomain();
        Domain d = tpd.generateDomain();
        Visualizer v = new Visualizer();

        v.addStaticPainter(new RoomsMapPainter(d));

        v.addObjectClassPainter(TeleporterDomain.CLASSGOAL, new CellPainter(d, Color.green));
        v.addObjectClassPainter(TeleporterDomain.CLASSAGENT, new CellPainter(d, Color.yellow));
        v.addObjectClassPainter(TeleporterDomain.CLASSTELE, new CellPainter(d, Color.red));

        return v;
    }

    public static class RoomsMapPainter implements StaticPainter {
        public RoomsMapPainter(Domain domain) { }

        @Override
        public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {
            //draw the walls; make them black
            g2.setColor(Color.black);

            float domainXScale = (TeleporterDomain.MAXX + 1);
            float domainYScale = (TeleporterDomain.MAXY + 1);

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            //pass through each cell of the map and if it is a wall, draw it
            for(int i = 0; i <= TeleporterDomain.MAXX; i++) {
                for(int j = 0; j <= TeleporterDomain.MAXY; j++) {

                    if(TeleporterDomain.MAP[i][j] == 1) {

                        float rx = i*width;
                        float ry = cHeight - height - j*height;

                        g2.fill(new Rectangle2D.Float(rx, ry, width, height));
                    }		
                }
            }		
        }
    }

    public static class CellPainter implements ObjectPainter {
        Color col;

        public CellPainter(Domain domain, Color col) {
            this.col = col;
        }

        @Override
        public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
                                float cWidth, float cHeight) {
            //draw the walls; make them black
            g2.setColor(this.col);

            float domainXScale = (TeleporterDomain.MAXX + 1);
            float domainYScale = (TeleporterDomain.MAXY + 1);

            //determine then normalized width
            float width = (1.0f / domainXScale) * cWidth;
            float height = (1.0f / domainYScale) * cHeight;

            float rx = ob.getDiscValForAttribute(TeleporterDomain.ATTX) * width;
            float ry = cHeight - height - ob.getDiscValForAttribute(TeleporterDomain.ATTY) * height;

            g2.fill(new Rectangle2D.Float(rx, ry, width, height));

        }		
    }
}
