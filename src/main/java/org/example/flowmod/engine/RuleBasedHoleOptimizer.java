package org.example.flowmod.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Optimizer that applies user provided rules to generate a hole layout.
 */
public class RuleBasedHoleOptimizer extends GraduatedHoleOptimizer {

    private final DesignRules designRules;
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleBasedHoleOptimizer.class);

    public RuleBasedHoleOptimizer(DesignRules rules, DrillSizePolicy policy, FlowPhysics physics) {
        super(policy, physics);
        this.designRules = rules;
    }

    @Override
    public HoleLayout optimize(FlowParameters params) {
        int rows = 10;
        HoleLayout blank = new HoleLayout();
        for (int i = 0; i < rows; i++) {
            blank.addHole(new HoleSpec(i, 0.0, 0.0));
        }

        java.util.List<Double> drillSet = java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);

        HoleLayout layout = DrillUtils.taperWithRules(blank, drillSet, params);
        double err = FlowPhysics.computeUniformityError(layout, params);
        LOGGER.debug("Computed uniformity: {} %CV", err);
        if (err > 5.0) {
            throw new DesignNotConvergedException(String.format("Uniformity %.2f%% exceeds target", err));
        }
        return layout;
    }

    public DesignRules getDesignRules() {
        return designRules;
    }
}
