package org.example.flowmod.engine;

/**
 * Strategy for determining drill size for a hole specification.
 */
public interface DrillSizePolicy {
    /**
     * Returns the drill diameter for the provided hole specification.
     */
    double getDrillSize(HoleSpec spec);
}
