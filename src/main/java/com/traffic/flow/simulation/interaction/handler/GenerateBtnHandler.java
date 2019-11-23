package com.traffic.flow.simulation.interaction.handler;

import com.traffic.flow.simulation.openstreetmap.OsmConverter;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComboBox;
import javax.swing.JTextArea;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.viewer.GeoPosition;

import com.traffic.flow.simulation.Microscopic;
import com.traffic.flow.simulation.generation.CreateVehicles;
import com.traffic.flow.simulation.interaction.components.AttentionDialog;
import com.traffic.flow.simulation.interaction.components.SelectionAdapter;
import com.traffic.flow.simulation.model.Intersect;
import com.traffic.flow.simulation.model.Link;
import com.traffic.flow.simulation.model.MOBILVehicle;
import com.traffic.flow.simulation.model.StepReport;
import com.traffic.flow.simulation.model.TrafficLight;
import com.traffic.flow.simulation.model.Vehicle;
import com.traffic.flow.simulation.openstreetmap.OpenStreetMapLoader;
import com.traffic.flow.simulation.openstreetmap.ReportHandler;
import com.traffic.flow.simulation.openstreetmap.RoadNetwork;
import com.traffic.flow.simulation.openstreetmap.RoadNetworkReader;
import com.traffic.flow.simulation.openstreetmap.RoadNetworkWriter;
import com.traffic.flow.simulation.openstreetmap.VehicleHandler;
import com.traffic.flow.simulation.tools.Distance;
import com.traffic.flow.simulation.tools.HDFSUtil;
import com.vividsolutions.jts.geom.Coordinate;

import scala.collection.JavaConverters;
import scala.collection.Seq;

@ToString
@EqualsAndHashCode
public class GenerateBtnHandler implements ActionListener {
    private final Logger LOG = Logger.getLogger(GenerateBtnHandler.class);
    private SelectionAdapter sa;
    private SimulationBtnHandler sbHandler;
    private TextField numTxt;
    private TextField simTxt;
    private TextField stepTxt;
    private TextField pathTxt;
    private JTextArea textArea;
    private JComboBox genList;
    private SparkSession spark;

    public GenerateBtnHandler(final SelectionAdapter sa, final TextField numTxt, final TextField simTxt, final TextField stepTxt, final TextField pathTxt, final SimulationBtnHandler sbHandler,
            final JTextArea textArea, final JComboBox genList, final SparkSession spark) {
        this.sa = sa;
        this.numTxt = numTxt;
        this.simTxt = simTxt;
        this.stepTxt = stepTxt;
        this.pathTxt = pathTxt;
        this.sbHandler = sbHandler;
        this.textArea = textArea;
        this.genList = genList;
        this.spark = spark;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (sa.getViewer().getOverlayPainter() == null || numTxt.getText() == null || numTxt.getText().length() == 0 || simTxt.getText() == null || simTxt.getText().length() == 0
                || stepTxt.getText() == null || stepTxt.getText().length() == 0 || pathTxt.getText() == null || pathTxt.getText().length() == 0) {
            final AttentionDialog dialog = new AttentionDialog("Attention", "You must select an area, enter the required parameters first!");
        } else {
            final Thread t = new Thread(() -> {
                textArea.append("Processing...\n");
                final JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

                final long startTime = System.currentTimeMillis();

                final Rectangle2D[] bounds = sa.getPoints();
                final Point pt1 = new Point();
                pt1.setLocation(bounds[0].getX(), bounds[0].getY());
                final Point pt2 = new Point();
                pt2.setLocation(bounds[1].getX(), bounds[1].getY());

                final GeoPosition geo1 = sa.getViewer().convertPointToGeoPosition(pt1);
                final GeoPosition geo2 = sa.getViewer().convertPointToGeoPosition(pt2);

                final String type = genList.getSelectedIndex() == 0 ? "DSO" : "NB";
                final int total = Integer.parseInt(numTxt.getText());
                final int step = Integer.parseInt(simTxt.getText());
                final int timestep = Integer.parseInt(stepTxt.getText());
                final String outputPath = pathTxt.getText();

                LOG.warn(String.format("Selected rectangle, p1: %s, p2: %s", geo1, geo2));
                textArea.append("Selected rectangle... \n");
                textArea.append("p1: " + geo1 + "\n");
                textArea.append("p2: " + geo2 + "\n");
                textArea.append("Parameters... \n");
                textArea.append(String.format("Type: %s, Total: %s, Steps: %s, TimeStep: %s. \n", type, total, step, timestep));

                final Coordinate coor1 = new Coordinate(geo1.getLatitude(), geo1.getLongitude());
                final Coordinate coor2 = new Coordinate(geo2.getLatitude(), geo2.getLongitude());
                final Distance distance = new Distance();

                final double maxLen = distance.euclidean(coor1.x, coor2.x, coor1.y, coor2.y) / 10;
                final Coordinate newCoor1 = new Coordinate(coor1.x + maxLen, coor1.y - maxLen);
                final Coordinate newCoor2 = new Coordinate(coor2.x + maxLen, coor2.y - maxLen);

                textArea.append("Downloaded OSM file...\n");

                final HDFSUtil hdfs = new HDFSUtil(outputPath);
                final String name = "/TrafficFlowSim";
                hdfs.deleteDir(name);
                hdfs.mkdir(name);
                final String output = outputPath + name;

                final OpenStreetMapLoader openStreetMapLoader = new OpenStreetMapLoader(newCoor1, newCoor2, output);
                openStreetMapLoader.parquet();
                openStreetMapLoader.osm();

                textArea.append("Output Path: " + output + "\n");

                final RoadNetwork roadNetwork = OsmConverter.convertToRoadNetwork(spark, output);
                textArea.append("Processing OSM...\n");
                final RoadNetworkWriter networkWriter = new RoadNetworkWriter(spark, roadNetwork, output);
                networkWriter.writeEdgeJson();
                textArea.append("Write edge into json. \n");
                networkWriter.writeSignalJson();
                textArea.append("Write signal into json. \n");
                networkWriter.writeIntersectJson();
                textArea.append("Write intersection into json. \n");

                final String osmPath = "datareader.file=" + output + "/map.osm";
                final String ghConfig = "config=" + System.getProperty("user.dir") + "/src/test/resources/graphhopper/config.properties";
                final String[] vehParameters = new String[] { ghConfig, osmPath };

                textArea.append("Generating vehicles...\n");
                final CreateVehicles createVehicles = new CreateVehicles(vehParameters, coor1, coor2, maxLen);
                List<Vehicle> vehicleList = null;
                try {
                    vehicleList = createVehicles.createVehicles(total, type);
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                }

                final VehicleHandler vehicleHandler = new VehicleHandler(spark, output);
                vehicleHandler.writeVehicleTrajectoryJson(convertListToSeq(vehicleList));
                textArea.append("Write vehicle into json. \n");
                final long endTime = System.currentTimeMillis();
                textArea.append("Finished pre-processing! Total time: " + (endTime - startTime) / 1000 + " seconds\n");

                textArea.append("Begin Simulation...\n");
                final RoadNetworkReader networkReader = new RoadNetworkReader(spark, output);
                final Dataset<Link> edges = networkReader.readEdgeJson();
                final Dataset<TrafficLight> signals = networkReader.readSignalJson();
                final Dataset<Intersect> intersects = networkReader.readIntersectJson();
                final Dataset<MOBILVehicle> vehicles = vehicleHandler.readVehicleTrajectoryJson();
                textArea.append("Read edges, signals and vehicles...\n");

                List<StepReport> res;
                final long simBegin = System.currentTimeMillis();

                LOG.warn("Running in spark");
                Microscopic.sim(spark, edges, signals, intersects, vehicles, output, step, timestep, total / 70);
                final ReportHandler reportHandler = new ReportHandler(spark, output, 50);
                final Dataset<StepReport> reports = reportHandler.readReportJson();
                res = reports.collectAsList();

                final long simEnd = System.currentTimeMillis();
                textArea.append("Finished Simulation! Total time: " + (simEnd - simBegin) / 1000 + " seconds\n");

                textArea.append("Saved simulation reports into json file. \n");
                sbHandler.setEdges(edges);
                sbHandler.setReports(res);
                textArea.append("You are ready for traffic visualization! \n");

                final double area = distance.rectArea(coor1.x, coor1.y, coor2.x, coor2.y);
                if (total < 5000 && area < 15000000) {
                    sbHandler.setUi(true);
                } else {
                    sbHandler.setUi(false);
                    LOG.warn("Because the number of vehicle is larger than 5000 or the area is larger than 15,000,000, "
                            + "TrafficFlowSim will not show the traffic visualization! Please check output in " + output);
                }
            });

            t.start();
        }
    }

    private static Seq<Vehicle> convertListToSeq(final List<Vehicle> inputList) {
        return JavaConverters.asScalaIteratorConverter(inputList.iterator()).asScala().toSeq();
    }
}
