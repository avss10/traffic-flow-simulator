package com.traffic.flow.simulation.tools;

import java.io.File;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

@ToString
@EqualsAndHashCode
public class FileOps {
    private static final Logger FILE_OPS_LOGGER = Logger.getLogger(FileOps.class);

    public void createDirectory(String directory) {
        try {
            File f = new File(directory);
            if (f.isDirectory()) {
                FileUtils.cleanDirectory(f);
                FileUtils.forceDelete(f);
            }
            FileUtils.forceMkdir(f);
        } catch (IOException e) {
            FILE_OPS_LOGGER.warn("Error happens when creating trafficflowsim output folder.", e);
        }
    }

    public boolean deleteDirectory(String directory) {
        File f = new File(directory);
        if (f.isDirectory()) {
            try {
                FileUtils.cleanDirectory(f);
                FileUtils.forceDelete(f);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
}
