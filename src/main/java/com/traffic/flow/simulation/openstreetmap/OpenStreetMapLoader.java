package com.traffic.flow.simulation.openstreetmap;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.log4j.Logger;
import org.openstreetmap.osmosis.xml.v0_6.XmlDownloader;

/**
 * Road network loader Download road network data and compress it.
 */
@ToString
@EqualsAndHashCode
public class OpenStreetMapLoader {
    private Coordinate coordinate_1;
    private Coordinate geo2;
    private String path;
    private static final Logger LOG = Logger.getLogger(OpenStreetMapLoader.class);

    public OpenStreetMapLoader(Coordinate coordinate_1, Coordinate geo2, String path) {
        this.coordinate_1 = coordinate_1;
        this.geo2 = geo2;
        this.path = path;
    }

    public void parquet() {
        String osmUrl = "http://overpass-api.de/api";
        XmlDownloader xmlDownloader = new XmlDownloader(coordinate_1.y, geo2.y, coordinate_1.x, geo2.x, osmUrl);
        xmlDownloader.setSink(new OpenStreetMapParquetSink(path));
        xmlDownloader.run();
    }

    public void osm() {
        String OSM_URL = "http://overpass-api.de/api/map?bbox=";
        URL url = null;

        double left = Math.min(coordinate_1.y, geo2.y);
        double right = Math.max(coordinate_1.y, geo2.y);
        double top = Math.max(coordinate_1.x, geo2.x);
        double bottom = Math.min(coordinate_1.x, geo2.x);

        OSM_URL += left + "," + bottom + "," + right + "," + top;

        try {
            url = new URL(OSM_URL);
        } catch (MalformedURLException e) {
            LOG.warn("The OSM download URL is incorrect.", e);
        }

        String newFileName = String.format("%s/%s.osm", path, "map");

        try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(newFileName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
        } catch (IOException e) {
            LOG.warn("Error happens when downloading OSM.", e);
        }
    }
}
