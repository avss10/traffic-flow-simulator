package com.traffic.flow.simulation.trafficUI.model;

import java.awt.Color;

public class CoordinatesPoint extends Point {
    private Color color = Color.black;

    public CoordinatesPoint(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public CoordinatesPoint(double latitude, double longitude, Color color) {
        super(latitude, longitude);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
