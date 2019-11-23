package com.traffic.flow.simulation;

import com.traffic.flow.simulation.generation.CreateVehicles;
import com.traffic.flow.simulation.model.Vehicle;
import com.traffic.flow.simulation.tools.Distance;
import com.traffic.flow.simulation.tools.FileOps;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VehicleGenerationTester extends TrafficFlowSimulatorTestBase {

    static JavaSparkContext sc;
    static String resources;
    static FileOps fileOps = new FileOps();
    static Coordinate coor1;
    static Coordinate coor2;
    static int total;
    static String type;
    static SparkSession ss;

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SparkConf conf =
                new SparkConf().setAppName("VehicleGenerationTester").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
        coor1 = new Coordinate(33.429165, -111.942323);
        coor2 = new Coordinate(33.413572, -111.924442);
        total = 1000;
        type = "DSO";
    }

    @AfterClass
    public static void tearDown() {
        fileOps.deleteDirectory(resources + "/samples/map-gh");
        sc.stop();
    }

    @Test
    public void vehicleGeneration() throws ExecutionException, InterruptedException {
        double maxLen = new Distance().euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
        String osmPath = "datareader.file=" + resources + "/samples/map.openstreetmap";

        String[] vehParameters =
                new String[] {"config=" + resources + "/graphhopper/config.properties", osmPath};
        CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
        List<Vehicle> vehicleList = createVehicles.createVehicles(total, type);
        Assert.assertEquals(vehicleList.size(), total);
    }
}
