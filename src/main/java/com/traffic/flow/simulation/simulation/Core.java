package com.traffic.flow.simulation.simulation;

import com.traffic.flow.simulation.model.Intersect;
import com.traffic.flow.simulation.model.Link;
import com.traffic.flow.simulation.model.MOBILVehicle;
import com.traffic.flow.simulation.model.StepReport;
import com.traffic.flow.simulation.model.TrafficLight;
import com.traffic.flow.simulation.model.Vehicle;
import com.traffic.flow.simulation.openstreetmap.ReportHandler;
import com.traffic.flow.simulation.openstreetmap.RoadNetwork;
import com.traffic.flow.simulation.openstreetmap.RoadNetworkReader;
import com.traffic.flow.simulation.openstreetmap.RoadNetworkWriter;
import com.traffic.flow.simulation.openstreetmap.VehicleHandler;
import com.vividsolutions.jts.geom.Coordinate;
import com.traffic.flow.simulation.Microscopic;
import com.traffic.flow.simulation.generation.CreateVehicles;
import com.traffic.flow.simulation.model.SimConfig;
import com.traffic.flow.simulation.openstreetmap.*;
import com.traffic.flow.simulation.openstreetmap.OpenStreetMapLoader;
import com.traffic.flow.simulation.tools.Distance;
import com.traffic.flow.simulation.tools.HDFSUtil;
import com.traffic.flow.simulation.trafficUI.TrafficPanel;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import scala.collection.JavaConverters;
import scala.collection.Seq;

/**
 * Core class to do data pre-processing and simulation.
 */
@ToString
@EqualsAndHashCode
public class Core {
    private static final Logger CORE_LOGGER = Logger.getLogger(Core.class);
    private static final String resources = System.getProperty("user.dir") + "/src/test/resources";

    /**
     * Pre-process road network and generate vehicles.
     *
     * @param spark the spark session
     * @param simConfig the simulation configuration
     */
    public void preProcess(SparkSession spark, SimConfig simConfig) {

        CORE_LOGGER.info(simConfig.toString());

        HDFSUtil hdfs = new HDFSUtil(simConfig.getOutputPath());
        String name = "/trafficflowsim";
        hdfs.deleteDir(name);
        hdfs.mkdir(name);
        String output = simConfig.getOutputPath() + name;

        Coordinate coor1 = new Coordinate(simConfig.getLat1(), simConfig.getLon1());
        Coordinate coor2 = new Coordinate(simConfig.getLat2(), simConfig.getLon2());
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        Coordinate newCoor1 = new Coordinate(coor1.x + maxLen, coor1.y - maxLen);
        Coordinate newCoor2 = new Coordinate(coor2.x + maxLen, coor2.y - maxLen);

        OpenStreetMapLoader openStreetMapLoader = new OpenStreetMapLoader(newCoor1, newCoor2, output);
        openStreetMapLoader.parquet();
        openStreetMapLoader.osm();

        RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(spark, output);
        RoadNetworkWriter networkWriter = new RoadNetworkWriter(spark, roadNetwork, output);
        networkWriter.writeEdgeJson();
        networkWriter.writeSignalJson();
        networkWriter.writeIntersectJson();

        String osmPath = "datareader.file=" + output + "/map.osm";
        String[] vehParameters =
                new String[] {"config=" + resources + "/graphhopper/config.properties", osmPath};

        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = null;
        try {
            vehicleList = createVehicles.createVehicles(simConfig.getTotal(), simConfig.getType());
        } catch (InterruptedException | ExecutionException e) {
            CORE_LOGGER.error("Bad vehicle data generated, Try generating vehicle list again!", e);
        }

        VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
        vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));
    }

    /**
     * Take processed data, run the simulation and open visualization panel when the simulation is
     * finished.
     *
     * @param spark the spark session
     * @param simConfig the simulation configuration
     * @param appTitle the app title
     */
    public void simulation(SparkSession spark, SimConfig simConfig, String appTitle) {
        String path = simConfig.getOutputPath() + "/trafficlfowsim";
        VehicleHandler vehicleHandler = new VehicleHandler(spark, path);
        RoadNetworkReader networkReader = new RoadNetworkReader(spark, path);

        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();
        CORE_LOGGER.info(
                "Read: edge: "
                        + edges.count()
                        + ", signals: "
                        + signals.count()
                        + ", intersects: "
                        + intersects.count()
                        + ", vehicles: "
                        + vehicles.count());

        long t1 = System.currentTimeMillis();
        Microscopic.sim(
                spark,
                edges,
                signals,
                intersects,
                vehicles,
                path,
                simConfig.getStep(),
                simConfig.getTimestep(),
                simConfig.getPartition());
        long t2 = System.currentTimeMillis();
        CORE_LOGGER.info("Finished Simulation: " + (t2 - t1) / 1000);

        ReportHandler reportHandler = new ReportHandler(spark, path, simConfig.getPartition());
        Dataset<StepReport> reports = reportHandler.readReportJson();

        double area =
                new Distance()
                        .rectArea(
                                simConfig.getLat1(),
                                simConfig.getLon1(),
                                simConfig.getLat2(),
                                simConfig.getLon2());

        // Visualization restriction
        if (simConfig.getTotal() < 5000 && area < 8000000) {
            TrafficPanel traffic = new TrafficPanel(appTitle);
            traffic.run(edges, reports.collectAsList());
        } else {
            CORE_LOGGER.info(
                    "Because the number of vehicle is larger than 5000 or the area is larger than 800,0000, "
                            + "TrafficFlowSim will not show the traffic visualization! Please check output in "
                            + simConfig.getOutputPath());
        }
    }

    private static Seq<Vehicle> convertListToSeq(List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }
}
