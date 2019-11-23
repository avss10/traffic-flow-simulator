package com.traffic.flow.simulation.trafficUI;

import com.traffic.flow.simulation.model.SegmentNode;
import java.awt.Color;
import java.util.List;

import org.apache.spark.sql.Dataset;

import com.traffic.flow.simulation.model.Link;
import com.traffic.flow.simulation.model.StepReport;
import com.traffic.flow.simulation.trafficUI.model.CoordinatesPoint;
import com.traffic.flow.simulation.trafficUI.model.Point;
import com.traffic.flow.simulation.trafficUI.model.Segment;

public class TrafficPanel {

    private String appTitle;

    public TrafficPanel(final String appTitle) {
        this.appTitle = appTitle;
    }

    public void run(final Dataset<Link> edges, final List<StepReport> reports) {

        final MapWindow window = new MapWindow(appTitle);
        window.setVisible(true);

        final List<Link> links = edges.toJavaRDD().collect();

        for (int i = 0; i < links.size(); i++) {
            final Link l = links.get(i);
            final com.traffic.flow.simulation.model.SegmentNode head = l.getHead();
            final SegmentNode tail = l.getTail();

            final Point headCenter = new Point(head.coordinate().x, head.coordinate().y);
            final Point tailCenter = new Point(tail.coordinate().x, tail.coordinate().y);
            window.addSegment(new Segment(headCenter, tailCenter, Color.black));

            final String[] lanes = l.getLaneArray().split("#");
            for (final String lane : lanes) {
                if (lane == null || lane.length() == 0) {
                    continue;
                }
                final String[] lane_items = lane.split("\\|");
                final String[] headLine = lane_items[1].split(",");
                final String[] tailLine = lane_items[2].split(",");
                final Point laneHead = new Point(Double.parseDouble(headLine[0]), Double.parseDouble(headLine[1]));
                final Point laneTail = new Point(Double.parseDouble(tailLine[0]), Double.parseDouble(tailLine[1]));
                window.addSegment(new Segment(laneHead, laneTail, Color.black));
            }
        }

        for (final StepReport report : reports) {
            final int step = report.getStep();
            if (report.getVehicleId() == null) {
                final Color color = report.getSignal() == 0 ? Color.GREEN : report.getSignal() == 1 ? Color.yellow : Color.RED;
                window.addSignal(new CoordinatesPoint(report.getSignalLocation().x, report.getSignalLocation().y, color), step);
            } else {
                window.addVehicle(new Segment(new CoordinatesPoint(report.getVehicleFront().x, report.getVehicleFront().y), new CoordinatesPoint(report.getVehicleRear().x, report.getVehicleRear().y), Color.BLUE, 3),
                        step);
            }
        }

        window.runSimulation();
    }
}
