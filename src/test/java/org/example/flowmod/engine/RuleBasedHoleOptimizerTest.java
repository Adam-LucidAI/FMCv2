package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
 main

public class RuleBasedHoleOptimizerTest {

    @Test
    public void testOptimizeReturnsLayout() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new BasicDesignRules(10,
                java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0));
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);
        HoleLayout layout = optimizer.optimize(new FlowParameters(150.0, 6.309, 1200.0));
        assertNotNull(layout);
        assertNotNull(layout.getHoles());
        assertEquals(10, layout.getHoles().size());
        double err1 = FlowPhysics.computeUniformityError(layout, new FlowParameters(150.0, 6.309, 1200.0));
        assertTrue(err1 <= 5.0);

        assertTrue(layout.getHoles().size() <= rules.rowCount());
        for (HoleSpec h : layout.getHoles()) {
            assertTrue(rules.allowableDrillSizesMm().contains(h.holeDiameterMm()));
        }

        HoleLayout layout2 = optimizer.optimize(new FlowParameters(400.0, 31.5, 2000.0));
        assertEquals(10, layout2.getHoles().size());
        double err2 = FlowPhysics.computeUniformityError(layout2, new FlowParameters(400.0, 31.5, 2000.0));
        assertTrue(err2 <= 5.0);
    }

    @Test
    public void testOptimizePreservesRowCount() {
        DrillSizePolicy policy = new DefaultDrillSizePolicy();
        FlowPhysics physics = new FlowPhysics();
        DesignRules rules = new DesignRules() {};
        RuleBasedHoleOptimizer optimizer = new RuleBasedHoleOptimizer(rules, policy, physics);

        HoleLayout layout = optimizer.optimize(new FlowParameters(200.0, 10.0, 1500.0));
        assertNotNull(layout);
        assertEquals(10, layout.getHoles().size());
    }
}
