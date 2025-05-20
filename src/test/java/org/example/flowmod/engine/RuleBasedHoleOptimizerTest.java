package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RuleBasedHoleOptimizerTest {

    @Test
    public void testOptimizeReturnsLayout() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new DesignRules() {};
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);
        HoleLayout layout = optimizer.optimize(new FlowParameters(10.0, 1.0));
        assertNotNull(layout);
        assertNotNull(layout.getHoles());
    }
}
