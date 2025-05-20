package org.example.flowmod.engine;

/**
 * Thrown when an optimizer fails to converge on a design solution.
 */
public class DesignNotConvergedException extends RuntimeException {
    public DesignNotConvergedException(String message) {
        super(message);
    }
}
