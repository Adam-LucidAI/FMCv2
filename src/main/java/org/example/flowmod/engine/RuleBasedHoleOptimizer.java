package org.example.flowmod.engine;

/**
 * Optimizer that applies user provided rules to generate a hole layout.
 */
public class RuleBasedHoleOptimizer extends GraduatedHoleOptimizer {

    private final DesignRules designRules;

    public RuleBasedHoleOptimizer(DesignRules rules, DrillSizePolicy policy, FlowPhysics physics) {
        super(policy, physics);
        this.designRules = rules;
    }

    @Override
    public HoleLayout optimize(FlowParameters params) {
        // In a real implementation designRules would influence the layout.
        return super.optimize(params);
    }

    public DesignRules getDesignRules() {
        return designRules;
    }
}
