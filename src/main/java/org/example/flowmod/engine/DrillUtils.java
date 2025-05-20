package org.example.flowmod.engine;

/**
 * Collection of helper utilities for dealing with drill operations.
 */
public final class DrillUtils {
    private DrillUtils() {
    }

    public static HoleSpec createHole(double diameter, double position) {
        return new HoleSpec(diameter, position);
    }
}
