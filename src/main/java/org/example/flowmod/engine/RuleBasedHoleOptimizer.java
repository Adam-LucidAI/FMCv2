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
        java.util.List<Double> drillSet = designRules.allowableDrillSizesMm();
        if (drillSet == null || drillSet.isEmpty()) {
            drillSet = java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);
        }

        double maxHole = drillSet.stream().max(Double::compareTo).orElse(4.0);

        for (double dx : DrillUtils.SPACING_CANDIDATES) {
            java.util.List<Double> rows = DrillUtils.generateCandidateRows(params.headerLenMm(), dx);
            HoleLayout layout = HoleLayout.withRows(rows, maxHole);
            layout = DrillUtils.taperWithRules(layout, drillSet, params);
            if (FlowPhysics.CV(layout, params) <= 5.0) {
                return layout;
            }
        }

        throw new DesignNotConvergedException("Cannot meet spec even at 50 mm grid");
    }

    public DesignRules getDesignRules() {
        return designRules;
    }
}
