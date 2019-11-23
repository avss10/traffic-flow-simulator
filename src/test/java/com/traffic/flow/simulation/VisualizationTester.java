package com.traffic.flow.simulation;

import com.traffic.flow.simulation.model.Link;
import com.traffic.flow.simulation.model.StepReport;
import com.traffic.flow.simulation.openstreetmap.ReportHandler;
import com.traffic.flow.simulation.openstreetmap.RoadNetworkReader;
import com.traffic.flow.simulation.trafficUI.TrafficPanel;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class VisualizationTester extends TrafficFlowSimulatorTestBase {
    static JavaSparkContext sc;
    static SparkSession ss;
    static String resources;

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("Visualization").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        ss = SparkSession.builder().config(sc.getConf()).getOrCreate();
        resources = System.getProperty("user.dir") + "/src/test/resources";
    }

    @AfterClass
    public static void tearDown() {
        sc.stop();
    }

    @Test
    public void visualization() {
        String path = resources + "/samples";
        RoadNetworkReader networkReader = new RoadNetworkReader(ss, path);
        Dataset<Link> edges = networkReader.readEdgeJson();
        ReportHandler reportHandler = new ReportHandler(ss, path, 10);
        Dataset<StepReport> reports = reportHandler.readReportJson();
        TrafficPanel traffic = new TrafficPanel("TrafficFlowSim Test");
        traffic.run(edges, reports.collectAsList());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
