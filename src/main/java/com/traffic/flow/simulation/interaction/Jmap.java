package com.traffic.flow.simulation.interaction;

import com.traffic.flow.simulation.interaction.controller.MainController;
import com.traffic.flow.simulation.interaction.controller.InputController;
import com.traffic.flow.simulation.interaction.controller.OutputController;
import java.awt.BorderLayout;
import javax.swing.*;
import org.apache.log4j.Logger;
import org.apache.spark.sql.SparkSession;
import org.jxmapviewer.JXMapViewer;

/**
 * Java Map object
 */
public class Jmap {
    private static final Logger LOG = Logger.getLogger(Jmap.class);
    private final String appTitle;

    public Jmap(String appTitle) {
        this.appTitle = appTitle;
    }

    public void runUI(SparkSession spark) {
        int width = 1200;
        int height = 800;
        MainController cc = new MainController(width, height);
        final JXMapViewer jXMapViewer = cc.mapViewer;
        OutputController rc = new OutputController();
        InputController ic = new InputController(cc, rc, spark, appTitle);

        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.add(jXMapViewer, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(ic.inputPanel, BorderLayout.NORTH);
        rightPanel.add(rc.resultPanel, BorderLayout.SOUTH);
        JScrollPane scrollRight = new JScrollPane(rightPanel);

        frame.add(scrollRight, BorderLayout.EAST);

        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);

        jXMapViewer.addPropertyChangeListener("zoom", evt -> updateWindowTitle(frame, jXMapViewer));

        jXMapViewer.addPropertyChangeListener(
                "center", evt -> updateWindowTitle(frame, jXMapViewer));

        updateWindowTitle(frame, jXMapViewer);
    }

    protected void updateWindowTitle(JFrame frame, JXMapViewer jXMapViewer) {
        double lat = jXMapViewer.getCenterPosition().getLatitude();
        double lon = jXMapViewer.getCenterPosition().getLongitude();
        int zoom = jXMapViewer.getZoom();

        frame.setTitle(String.format(appTitle + " (%.2f / %.2f) - Zoom: %d", lat, lon, zoom));
    }
}
