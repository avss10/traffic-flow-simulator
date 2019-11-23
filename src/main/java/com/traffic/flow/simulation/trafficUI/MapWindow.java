package com.traffic.flow.simulation.trafficUI;

import com.traffic.flow.simulation.trafficUI.model.CoordinatesPoint;
import com.traffic.flow.simulation.trafficUI.model.Segment;
import java.awt.*;
import javax.swing.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MapWindow extends JFrame {
    private final MapPanel map;

    /** Creates a new window. */
    public MapWindow(String appTitle) {
        super(appTitle);
        map = new MapPanel();
        setLayout(new BorderLayout());
        add(map, BorderLayout.CENTER);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Deletes all the registered segments and POIs.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Adds a segment to the list of segments to display.
     *
     * @param segment the segment to add
     */
    public void addSegment(Segment segment) {
        map.addSegment(segment);
    }

    public void addSignal(CoordinatesPoint signal, int step) {
        map.addSignal(signal, step);
    }

    public void addVehicle(Segment vehicle, int step) {
        map.addVehicle(vehicle, step);
    }

    public void runSimulation() {
        map.run();
    }
}
