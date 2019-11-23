package com.traffic.flow.simulation.model.parameters;

/**
 * Minimzing overall brake inducing by lane.
 */
public interface MOBIL {
    double politenessFactor = 0.3;
    double maximumSafeDeceleration = 4;
    double thresholdAcceleration = 0.4;
}
