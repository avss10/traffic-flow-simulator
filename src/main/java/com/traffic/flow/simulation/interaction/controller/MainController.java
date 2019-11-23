package com.traffic.flow.simulation.interaction.controller;

import com.traffic.flow.simulation.interaction.components.SelectionAdapter;
import com.traffic.flow.simulation.interaction.components.SelectionPainter;
import java.io.File;
import javax.swing.event.MouseInputListener;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * Main controller
 */
@ToString
@EqualsAndHashCode
public class MainController {
    private GeoPosition center = new GeoPosition(38.883792, -94.686740);
    private int zoom = 7;
    private int width;
    private int height;
    public SelectionAdapter selAdaper;
    public SelectionPainter selPainter;
    public JXMapViewer mapViewer;

    public MainController(int width, int height) {
        this.width = width;
        this.height = height;
        this.mapViewer = mapViewer();
    }

    public void setCenter(GeoPosition geo) {
        this.center = geo;
    }

    public void setZoom(int z) {
        this.zoom = z;
    }

    private JXMapViewer mapViewer() {
        // Create a TileFactoryInfo for OpenStreetMap
        JXMapViewer jXMapViewer = new JXMapViewer();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // Setup local file cache
        File cacheDir =
                new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

        // Setup JXMapViewer
        jXMapViewer.setTileFactory(tileFactory);

        // Set the focus
        jXMapViewer.setZoom(zoom);
        jXMapViewer.setAddressLocation(center);

        // Add interactions
        MouseInputListener mia = new PanMouseInputListener(jXMapViewer);
        jXMapViewer.addMouseListener(mia);
        jXMapViewer.addMouseMotionListener(mia);
        jXMapViewer.addMouseListener(new CenterMapListener(jXMapViewer));
        jXMapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(jXMapViewer));
        jXMapViewer.addKeyListener(new PanKeyListener(jXMapViewer));

        // Add a selection painter
        this.selAdaper = new SelectionAdapter(jXMapViewer, width, height);
        this.selPainter = new SelectionPainter(selAdaper);

        jXMapViewer.addMouseListener(selAdaper);
        jXMapViewer.addMouseMotionListener(selAdaper);

        return jXMapViewer;
    }
}
