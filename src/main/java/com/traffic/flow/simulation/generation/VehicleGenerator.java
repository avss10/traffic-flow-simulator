package com.traffic.flow.simulation.generation;

import com.traffic.flow.simulation.shortestpath.Graph;
import com.vividsolutions.jts.geom.Coordinate;
import com.traffic.flow.simulation.model.Vehicle;
import com.traffic.flow.simulation.tools.SpatialRandom;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Vehicle generator.
 */
@ToString
@EqualsAndHashCode
public class VehicleGenerator {

    public Vehicle[] vehicleGeneration(double minLongitude, double minLatitude, double maxLongitude, double maxLatitude, Graph graph, double maxLeVehicleLength, String typeOfVehicle, int totalNumOfVehicles) {

        SpatialRandom spatialRandom = new SpatialRandom(minLongitude, minLatitude, maxLongitude, maxLatitude, maxLeVehicleLength, graph);
        Vehicle[] vehicles = new Vehicle[totalNumOfVehicles];

        for (int vehicleNum = 0; vehicleNum < totalNumOfVehicles; vehicleNum++) {

            vehicles[vehicleNum] = computeVehicle(typeOfVehicle, spatialRandom, vehicleNum, graph);
        }

        return vehicles;
    }

    private Vehicle computeVehicle(String typeOfVehicle, SpatialRandom specialRandom, int sid, Graph graph) {

        double len = specialRandom.spatialRandomLen();

        Coordinate source = null;
        Coordinate destination = null;

        if (typeOfVehicle.contains("DSO")) {
            source = specialRandom.spatialRandomNode();
            destination = specialRandom.computeDestinationDSO(source, len);
        } else if (typeOfVehicle.contains("NB")) {
            source = specialRandom.computeSourceNB();
            destination = specialRandom.computeDestinationNB(source, len);
        }

        Vehicle vehicle = graph.request(source, destination);

        if (vehicle == null) {
            return computeVehicle(typeOfVehicle, specialRandom, sid, graph);
        }

        Long[] path = vehicle.getEdgePath();

        if (path == null || path.length <= 1) {
            return computeVehicle(typeOfVehicle, specialRandom, sid, graph);
        }

        return vehicle;
    }
}