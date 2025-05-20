package org.example.flowmod.engine;

/**
 * Default implementation that simply returns the requested diameter.
 */
public class DefaultDrillSizePolicy implements DrillSizePolicy {
    @Override
    public double getDrillSize(HoleSpec spec) {
        if (spec == null) {
            throw new IllegalArgumentException("spec must not be null");
        }
        return spec.holeDiameterMm();
    }
}
