package org.example.flowmod.engine;

/**
 * Configuration interface supplying constraints for the optimiser.
 */
public interface DesignRules {

    double DEFAULT_ROW_SPACING_MM = 120.0;
    double UNIFORMITY_TARGET_PCT = 5.0;

    /**
     * Desired number of hole rows along the header.
     *
     * @return maximum rows allowed
     */
    default int rowCount() {
        return 10;
    }

    /**
     * Allowed drill diameters in millimetres.
     *
     * @return list of permitted drill diameters
     */
    default java.util.List<Double> allowableDrillSizesMm() {
        return java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);
    }
}
