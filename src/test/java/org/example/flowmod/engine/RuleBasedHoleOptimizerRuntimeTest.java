package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RuleBasedHoleOptimizerRuntimeTest {

    @Test
    public void testOptimizerProducesLayoutWithinError() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10, java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        double gpm = 120.0;
        double lps = gpm * 0.0631;
        FlowParameters p = new FlowParameters(200.0, lps, 1300.0, 100.0, HeaderType.PRESSURE);

        HoleLayout layout = optimizer.optimize(p);
        assertNotNull(layout);
        assertTrue(layout.getHoles().size() > 0);

        FlowParameters tuned = FlowPhysics.balanceSupplyPressure(layout, p);
        double err = FlowPhysics.computeUniformityError(layout, tuned);
        assertTrue(err <= 5.0);
        HoleSpec last = layout.getHoles().get(layout.getHoles().size() - 1);
        double spacing = p.headerLenMm() / layout.getHoles().size();
        assertEquals(p.headerLenMm(), last.axialPosMm() + spacing, spacing * 0.01);
    }
}
