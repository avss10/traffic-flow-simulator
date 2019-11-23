package com.traffic.flow.simulation.generation;

import com.traffic.flow.simulation.shortestpath.Graph;
import com.traffic.flow.simulation.model.Vehicle;
import java.util.concurrent.Callable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Thread generation.
 */
@ToString
@EqualsAndHashCode
public class ThreadGeneration implements Callable<Vehicle[]> {

    private Graph graph;
    private double maxVehicleLength;

    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;

    private VehicleGenerator vehicleGenerator = new VehicleGenerator();

    private int numberOfVehicles;
    private String typeOfVehicle;

    public ThreadGeneration(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude, Graph graph, double maxVehicleLength, int numberOfVehicles, String typeOfVehicle) {

        this.graph = graph;
        this.maxVehicleLength = maxVehicleLength;

        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;

        this.numberOfVehicles = numberOfVehicles;
        this.typeOfVehicle = typeOfVehicle;
    }

    @Override
    public Vehicle[] call() throws Exception {
        Vehicle[] vehicles = vehicleGenerator.vehicleGeneration(minLongitude, minLatitude, maxLongitude, maxLatitude, graph, maxVehicleLength, typeOfVehicle, numberOfVehicles);
        return vehicles;
    }
}