package org.example.flowmod.engine;

/**
 * Utilities for computing simple flow equations used by optimizers.
 */
public class FlowPhysics {

    /**
     * Dummy pressure drop calculation.
     */
    public double computePressureDrop(FlowParameters params) {
        return params.flowRate() * 0.1;
    }
}
