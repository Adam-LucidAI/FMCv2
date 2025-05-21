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
        FlowParameters p = new FlowParameters(200.0, lps, 1300.0);

        HoleLayout layout = optimizer.optimize(p);
        assertNotNull(layout);
        assertTrue(layout.getHoles().size() > 0);

        double err = FlowPhysics.computeUniformityError(layout, p);
        assertTrue(err <= 5.0);
    }

    @Test
    public void testSuctionSolverResults() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10, java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        double gpm = 120.0;
        double lps = gpm * 0.0631;
        FlowParameters p = new FlowParameters(200.0, lps, 1300.0);

        HoleLayout layout = optimizer.optimize(p);
        java.util.Set<Double> used = new java.util.HashSet<>();
        for (HoleSpec h : layout.getHoles()) {
            used.add(h.holeDiameterMm());
        }
        assertTrue(used.size() >= 3, "Expected at least 3 drill sizes");

        double err = FlowPhysics.computeUniformityError(layout, p);
        assertTrue(err <= 5.0, "Uniformity too high: " + err);

        double suction = FlowPhysics.findRequiredSuctionKPa(layout, p, -100.0, -1.0);
        assertTrue(suction <= -1.0 && suction >= -100.0, "suction " + suction);
    }

    @Test
    public void testSmallPipeSpacingFallback() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10, java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        double gpm = 10.0;
        double lps = gpm * 0.0631;
        FlowParameters p = new FlowParameters(50.0, lps, 500.0);

        HoleLayout layout = optimizer.optimize(p);
        double err = FlowPhysics.computeUniformityError(layout, p);
        assertTrue(err <= 5.0, "uniformity " + err);
        if (layout.getHoles().size() > 1) {
            double spacing = layout.getHoles().get(1).axialPosMm() - layout.getHoles().get(0).axialPosMm();
            assertTrue(spacing <= 60.0 + 1e-6);
        }
    }
}
