package com.traffic.flow.simulation.tools;

import com.vividsolutions.jts.geom.Coordinate;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Distance {

    private double euclideanCompute(double latitude1, double longitude1, double latitude2, double longitude2) {
        double deltaX = Math.abs(latitude1 - latitude2);
        double deltaY = Math.abs(longitude1 - longitude2);
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Calculates the distance between two points by haversine formula
     *
     * @param latitude1 Latitude of first coordinate
     * @param longitude1 Longitude of first coordinate
     * @param latitude2 Latitude of second coordinate
     * @param longitude2 Longitude of second coordinate
     * @return TrafficFlowSim.Distance between the two points, in meter
     */
    public double haversine(
            double latitude1, double longitude1, double latitude2, double longitude2) {
        latitude1 = Math.toRadians(latitude1);
        latitude2 = Math.toRadians(latitude2);
        longitude1 = Math.toRadians(longitude1);
        longitude2 = Math.toRadians(longitude2);

        double RADIUS_OF_EARTH = 6371;
        return (1000 * 2 * RADIUS_OF_EARTH
                * Math.asin(Math.sqrt( Math.sin((latitude2 - latitude1) / 2) * Math.sin((latitude2 - latitude1) / 2)
                + Math.cos(latitude1) * Math.cos(latitude2) * Math.sin((longitude2 - longitude1) / 2) * Math.sin((longitude2 - longitude1) / 2))));
    }

    /**
     * Calculates the distance between two points by haversine formula
     *
     * @param coordinate1 Coordinate 1
     * @param coordinate2 Coordinate 2
     * @return Distance in meter
     */
    public double haversine(Coordinate coordinate1, Coordinate coordinate2) {
        return haversine(coordinate1.x, coordinate1.y, coordinate2.x, coordinate2.y);
    }

    /**
     * Calculates the area between two coordinates
     *
     * @param latitude1 Latitude of first coordinate
     * @param longitude1 Longitude of first coordinate
     * @param latitude2 Latitude of second coordinate
     * @param longitude2 Longitude of second coordinate
     * @return the area of two coordinates
     */
    public double rectArea(double latitude1, double longitude1, double latitude2, double longitude2) {
        double length = haversine(new Coordinate(latitude1, longitude2), new Coordinate(latitude1, longitude1));
        double height = haversine(new Coordinate(latitude2, longitude1), new Coordinate(latitude2, longitude2));
        return (length * height);
    }

    /**
     * Calculates the distance between two points by euclidean distance
     *
     * @param node_A_coordinates, a node of coordinate
     * @param node_B_coordinates, a node of coordinate
     * @return the distance between A and B
     */
    public double euclidean(double[] node_A_coordinates, double[] node_B_coordinates) {
        return euclideanCompute(node_A_coordinates[0], node_A_coordinates[1], node_B_coordinates[0], node_B_coordinates[1]);
    }

    /**
     * Calculates the distance between two points by euclidean distance
     *
     * @param latitude1
     * @param latitude2
     * @param longitude1
     * @param longitude2
     * @return
     */
    public double euclidean(double latitude1, double latitude2, double longitude1, double longitude2) {
        return euclideanCompute(latitude1, longitude1, latitude2, longitude2);
    }
}
