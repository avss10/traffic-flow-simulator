package com.traffic.flow.simulation.tools;

import com.traffic.flow.simulation.shortestpath.Graph;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Random initialize vehicle by spatial data
 */
@ToString
@EqualsAndHashCode
public class SpatialRandom {

    private Random rand = new Random();
    private double minLongitude;
    private double minLatitude;
    private double maxLongitude;
    private double maxLatitude;
    private double minVehicleLength = 0.004; // 1.9km
    private double maxVehicleLength;
    private Graph graph;

    /**
     * Constructor
     * @param minLongitude minimum longitude
     * @param minLatitude minimum latitude
     * @param maxLongitude maximum longitude
     * @param maxLatitude maximum latitude
     * @param maxVehicleLength maximum vehicle length
     * @param graph Graph instance
     */
    public SpatialRandom(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude, double maxVehicleLength, Graph graph) {
        this.minLongitude = minLongitude;
        this.minLatitude = minLatitude;
        this.maxLongitude = maxLongitude;
        this.maxLatitude = maxLatitude;
        this.maxVehicleLength = maxVehicleLength;
        this.graph = graph;
    }

    private double randomRange(double low, double high) {
        return rand.nextDouble() * (high - low) + low;
    }

    /**
     * Random generate a node based on the boundary
     *
     * @return node
     */
    public Coordinate spatialRandomNode() {
        Coordinate node = new Coordinate(randomRange(minLatitude, maxLatitude), randomRange(minLongitude, maxLongitude));
        return node;
    }

    /**
     * Random generate travel length
     *
     * @return length
     */
    public double spatialRandomLen() {
        double r = Math.abs(rand.nextGaussian());
        double lenDiff = maxVehicleLength - minVehicleLength;
        double len = lenDiff <= 0 ? minVehicleLength : (r * (maxVehicleLength - minVehicleLength) + minVehicleLength);
        return len;
    }

    /**
     * Compute destination by Data-space oriented approach(DSO)
     *
     * @param source the source of vehicle
     * @param length the travel length of vehicle
     * @return destination
     */
    public Coordinate computeDestinationDSO(Coordinate source, double length) {
        double angle = ThreadLocalRandom.current().nextDouble(360);
        double lon = source.y + length * Math.sin(angle);
        double lat = source.x + length * Math.cos(angle);
        return new Coordinate(lat, lon);
    }

    /**
     * Compute source by Network-based approach(NB)
     *
     * @return source
     */
    public Coordinate computeSourceNB() {
        Coordinate src = spatialRandomNode();
        return graph.getClosestNode(src);
    }

    /**
     * Compute destination by Network-based approach(NB)
     *
     * @param source the source of vehicle
     * @param length the travel length of vehicle
     * @return destination
     */
    public Coordinate computeDestinationNB(Coordinate source, double length) {
        Coordinate dest = computeDestinationDSO(source, length);
        return graph.getClosestNode(dest);
    }
}
