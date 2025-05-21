package org.example.flowmod.engine;

/**
 * Base optimizer that produces a simple graduated hole layout.
 */
public class GraduatedHoleOptimizer {

    protected final DrillSizePolicy drillSizePolicy;
    protected final FlowPhysics physics;

    public GraduatedHoleOptimizer(DrillSizePolicy drillSizePolicy, FlowPhysics physics) {
        this.drillSizePolicy = drillSizePolicy;
        this.physics = physics;
    }

    /**
     * Produce a layout for the provided parameters.
     */
    public HoleLayout optimize(FlowParameters params) {
        // Dummy implementation with a single hole
        HoleLayout layout = new HoleLayout();
        double spacing = params.headerLenMm();
        double size = drillSizePolicy.getDrillSize(new HoleSpec(0, 1.0, 0.0, spacing));
        layout.addHole(new HoleSpec(0, size, 0.0, spacing));
        return layout;
    }
}
