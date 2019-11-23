package com.traffic.flow.simulation.interaction.components;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jxmapviewer.painter.Painter;

/**
 * Selection painter
 */
@ToString
@EqualsAndHashCode
public class SelectionPainter implements Painter<Object> {
    Rectangle2D s = new Rectangle2D.Double();

    private SelectionAdapter adapter;

    /** @param adapter the selection adapter */
    public SelectionPainter(SelectionAdapter adapter) {
        this.adapter = adapter;
    }

    public void paint(Graphics2D g, Object t, int width, int height) {

        Graphics2D g2 = (Graphics2D) g;

        Rectangle2D[] points = this.adapter.getPoints();
        g.setColor(Color.RED);
        g.setStroke(new BasicStroke(3));

        for (int i = 0; i < points.length; i++) {
            g2.fill(points[i]);
        }

        s.setRect(
                points[0].getCenterX(),
                points[0].getCenterY(),
                Math.abs(points[1].getCenterX() - points[0].getCenterX()),
                Math.abs(points[1].getCenterY() - points[0].getCenterY()));

        g2.draw(s);
    }
}
