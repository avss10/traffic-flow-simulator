package com.traffic.flow.simulation.generation;

import com.traffic.flow.simulation.shortestpath.Graph;
import com.vividsolutions.jts.geom.Coordinate;
import com.traffic.flow.simulation.model.Vehicle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Create vehicles.
 */
@ToString
@EqualsAndHashCode
public class CreateVehicles {

    private double maxVehicleLength;
    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;

    private Graph graph;

    public CreateVehicles(String[] args, Coordinate coordinate_1, Coordinate coordinate_2, double maxLength) {

        this.graph = new Graph(args);
        this.maxVehicleLength = maxLength;

        minLatitude = Math.min(coordinate_1.x, coordinate_2.x);
        maxLatitude = Math.max(coordinate_1.x, coordinate_2.x);
        minLongitude = Math.min(coordinate_1.y, coordinate_2.y);
        maxLongitude = Math.max(coordinate_1.y, coordinate_2.y);
    }

    public List<Vehicle> createVehicles(int totalNumberOfVehicles, String type) throws InterruptedException, ExecutionException {

        int numberOfThreads = 8;
        List<Vehicle> trajectories = new ArrayList<>();
        Collection<Callable<Vehicle[]>> tasks = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            ThreadGeneration thread = new ThreadGeneration(minLongitude, minLatitude, maxLongitude, maxLatitude, graph, maxVehicleLength, totalNumberOfVehicles / numberOfThreads, type);
            tasks.add(thread);
        }

        List<Future<Vehicle[]>> results = executor.invokeAll(tasks);
        waitForTerminationAfterShutdown(executor);

        for (Future<Vehicle[]> vehicles : results) {
            if (!vehicles.isCancelled() && vehicles.isDone()) {
                trajectories.addAll(Arrays.asList(vehicles.get()));
            }
        }

        return trajectories;
    }

    private void waitForTerminationAfterShutdown(ExecutorService threadPool) {

        threadPool.shutdown();

        try {

            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {

            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}