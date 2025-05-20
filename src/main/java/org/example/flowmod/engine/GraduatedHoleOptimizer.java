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
        double size = drillSizePolicy.getDrillSize(new HoleSpec(1.0, 0.0));
        layout.addHole(new HoleSpec(size, 0.0));
        return layout;
    }
}
