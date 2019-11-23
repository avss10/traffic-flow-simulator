package com.traffic.flow.simulation;

import com.traffic.flow.simulation.interaction.Jmap;
import com.traffic.flow.simulation.model.SimConfig;
import com.traffic.flow.simulation.simulation.Core;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;
import picocli.CommandLine.Option;

@ToString
@EqualsAndHashCode
public class TrafficFlowSim implements Runnable {

    @Option(
            names = {"-lt1", "--lat1"},
            type = double.class,
            description = "Latitude 1. Default value: ${DEFAULT-VALUE}",
            defaultValue = "38.883792")
    private double lat1;

    @Option(
            names = {"-ln1", "--lon1"},
            type = double.class,
            description = "Longitude 1. Default value: ${DEFAULT-VALUE}",
            defaultValue = "-94.686740")
    private double lon1;

    @Option(
            names = {"-lt2", "--lat2"},
            type = double.class,
            description = "Latitude 2. Default value: ${DEFAULT-VALUE}",
            defaultValue = "38.839925")
    private double lat2;

    @Option(
            names = {"-ln2", "--lon2"},
            type = double.class,
            description = "Longitude 2. Default value: ${DEFAULT-VALUE}",
            defaultValue = "-94.649453")
    private double lon2;

    @Option(
            names = {"-n", "--num"},
            type = int.class,
            description = "The number of vehicles. Default value: ${DEFAULT-VALUE}",
            defaultValue = "10000")
    private int num;

    @Option(
            names = {"-s", "--step"},
            type = int.class,
            description = "The simulation steps. Default value: ${DEFAULT-VALUE}",
            defaultValue = "600")
    private int step;

    @Option(
            names = {"-t", "--timestep"},
            type = double.class,
            description = "Time per step. Default value: ${DEFAULT-VALUE}",
            defaultValue = "1")
    private double timestep;

    @Option(
            names = {"-f", "--fileoutput"},
            description = "Output file path.")
    private String output;

    @Option(
            names = {"-y", "--type"},
            description = "Vehicle generation type. (DSO or NB) Default value: ${DEFAULT-VALUE}",
            defaultValue = "DSO")
    private String type;

    @Option(
            names = {"-o", "--only"},
            description = "Only run the interactive interface.")
    private boolean only;

    @Option(
            names = {"-c", "--command"},
            description = "Run all parameters in command line (experiment).")
    private boolean command;

    @Option(
            names = {"-m", "--manuscript"},
            description = "Manuscript path.")
    private String manuscript;

    @Option(
            names = {"-h", "--help"},
            usageHelp = true,
            description = "Print usage help and exit.")
    private boolean usageHelpRequested;

    @Option(
            names = {"-V", "--version"},
            versionHelp = true,
            description = "Print version information and exit.")
    private boolean versionHelpRequested;

    private static final Logger LOG = Logger.getLogger(TrafficFlowSim.class);
    private static final Properties prop = new Properties();
    private static final String appTitle = "TrafficFlowSim v0.0.1";

    @Override
    public void run() {

        SparkSession spark =
                SparkSession.builder()
                        .master("local[*]") // Developing mode
                        .appName("TrafficFlowSim")
                        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
                        .config(
                                "spark.kryo.registrator",
                                "org.datasyslab.geospark.serde.GeoSparkKryoRegistrator")
                        .getOrCreate();

        SimConfig simConfig = new SimConfig();

        // User interface
        if (only) {
            Jmap jmap = new Jmap(appTitle);
            jmap.runUI(spark);
            // Command
        } else if (command) {
            if (output == null) {
                LOG.warn("Please enter the output path and rerun the command.");
            } else if (num < 1000) {
                LOG.warn("Please enter a vehicle number not smaller than 1000.");
            } else {
                simConfig =
                        new SimConfig(lat1, lon1, lat2, lon2, num, output, step, timestep, type);
                start(spark, simConfig);
            }

            // Manuscript
        } else if (manuscript != null) {
            InputStream is = null;
            try {
                is = new FileInputStream(manuscript);
            } catch (FileNotFoundException e) {
                LOG.error("Manuscript can't be found.", e);
            }

            // Load properties from manuscript
            try {
                prop.load(is);
            } catch (IOException e) {
                LOG.error("Error happens when loading properties from manuscript.", e);
            }

            simConfig.setLat1(Double.parseDouble(prop.getProperty("geo1.lat")));
            simConfig.setLon1(Double.parseDouble(prop.getProperty("geo1.lon")));
            simConfig.setLat2(Double.parseDouble(prop.getProperty("geo2.lat")));
            simConfig.setLon2(Double.parseDouble(prop.getProperty("geo2.lon")));
            simConfig.setType(prop.getProperty("vehicle.type"));
            simConfig.setTotal(Integer.parseInt(prop.getProperty("vehicle.num")));
            simConfig.setTimestep(Double.parseDouble(prop.getProperty("simulation.timestep")));
            simConfig.setStep(Integer.parseInt(prop.getProperty("simulation.step")));
            simConfig.setOutputPath(prop.getProperty("simulation.output"));

            start(spark, simConfig);

            // Default setting
        } else {
            start(spark, simConfig);
        }
    }

    public static void main(String[] args) {
        CommandLine.run(new TrafficFlowSim(), System.err, args);
    }

    /**
     * Start run TrafficFlowSim.
     *
     * @param spark the spark session
     * @param simConfig the simulation configuration
     */
    private void start(SparkSession spark, SimConfig simConfig) {
        Core core = new Core();
        core.preProcess(spark, simConfig);
        core.simulation(spark, simConfig, appTitle);
    }
}