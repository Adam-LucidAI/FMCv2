package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RuleBasedHoleOptimizerTest {

    @Test
    public void testOptimizeReturnsLayout() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10,
                java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);
        FlowParameters p1 = new FlowParameters(150.0, 6.309, 1200.0);
        HoleLayout layout = optimizer.optimize(p1);
        assertNotNull(layout);
        assertNotNull(layout.getHoles());
        double err1 = FlowPhysics.computeUniformityError(layout, p1);
        assertTrue(err1 <= 5.0);
        for (HoleSpec h : layout.getHoles()) {
            assertTrue(rules.allowableDrillSizesMm().contains(h.holeDiameterMm()));
        }
        assertTrue(layout.getHoles().size() > 0);

        FlowParameters p2 = new FlowParameters(400.0, 31.5, 2000.0);
        HoleLayout layout2 = optimizer.optimize(p2);
        double err2 = FlowPhysics.computeUniformityError(layout2, p2);
        assertTrue(err2 <= 5.0);
        assertTrue(layout2.getHoles().size() > 0);
    }

    @Test
    public void testLargePipeFewRows() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10,
                java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        double gpm = 100.0;
        double lps = gpm * 0.0631;
        FlowParameters p = new FlowParameters(150.0, lps, 1200.0);

        HoleLayout layout = optimizer.optimize(p);
        double err = FlowPhysics.computeUniformityError(layout, p);
        assertTrue(err <= 5.0, "uniformity " + err);
        assertTrue(layout.getHoles().size() <= 10);

        java.util.Set<Double> used = new java.util.HashSet<>();
        for (HoleSpec h : layout.getHoles()) {
            used.add(h.holeDiameterMm());
        }
        assertTrue(used.size() >= 3);
    }

    @Test
    public void testHighFlowUniformity() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10,
                java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        double lps = 120.0 * 0.0631;
        FlowParameters p = new FlowParameters(200.0, lps, 1300.0);
        HoleLayout layout = optimizer.optimize(p);
        double err = FlowPhysics.computeUniformityError(layout, p);
        assertTrue(err <= 5.0, "Uniformity too high: " + err);
    }

    @Test
    public void testPathologicalCaseFailsQuickly() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10,
                java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        double lps = 1000.0 * 0.0631;
        FlowParameters p = new FlowParameters(50.0, lps, 100.0);

        java.time.Duration timeout = java.time.Duration.ofSeconds(5);
        org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(timeout, () ->
                assertThrows(DesignNotConvergedException.class, () -> optimizer.optimize(p)));
    }
}
