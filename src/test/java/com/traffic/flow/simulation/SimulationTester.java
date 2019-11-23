package com.traffic.flow.simulation;

import com.traffic.flow.simulation.model.*;
import com.traffic.flow.simulation.openstreetmap.*;
import com.traffic.flow.simulation.tools.FileOps;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimulationTester extends TrafficFlowSimulatorTestBase {
    static JavaSparkContext sc;
    static SparkSession ss;
    static String resources;
    static FileOps fileOps = new FileOps();

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("Simulation").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
        fileOps.createDirectory(resources + "/java-test");
    }

    @AfterClass
    public static void tearDown() {
        fileOps.deleteDirectory(resources + "/java-test");
        sc.stop();
    }

    @Test
    public void simulation() {
        String path = resources + "/samples";
        VehicleHandler vehicleHandler = new VehicleHandler(ss, path);
        RoadNetworkReader networkReader = new RoadNetworkReader(ss, path);

        Dataset<Link> edges = networkReader.readEdgeJson();
        Dataset<TrafficLight> signals = networkReader.readSignalJson();
        Dataset<Intersect> intersects = networkReader.readIntersectJson();
        Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();

        Microscopic.sim(
                ss, edges, signals, intersects, vehicles, resources + "/java-test", 600, 1, 10);

        ReportHandler reportHandler = new ReportHandler(ss, resources + "/java-test", 1);
        Dataset<StepReport> reports = reportHandler.readReportJson();
        reports.show();
    }
}
