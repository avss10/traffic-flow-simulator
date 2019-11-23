package com.traffic.flow.simulation.exception;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Out of edges {@link Exception}
 */
@ToString
@EqualsAndHashCode
public class OutOfEdgesException extends Exception {
    public OutOfEdgesException(String message) {
        super(message);
    }
}