package com.traffic.flow.simulation.exception;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Out of vehicles {@Link Exception}
 */
@ToString
@EqualsAndHashCode
public class OutOfVehiclesException extends Exception {
    public OutOfVehiclesException(String message) {
        super(message);
    }
}