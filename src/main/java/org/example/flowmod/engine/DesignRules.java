package org.example.flowmod.engine;

/**
 * Configuration interface supplying constraints for the optimiser.
 */
public interface DesignRules {

    /**
     * Desired number of hole rows along the header.
     */
    int rowCount();

    /**
     * Allowed drill diameters in millimetres.
     */
    java.util.List<Double> allowableDrillSizesMm();
}
