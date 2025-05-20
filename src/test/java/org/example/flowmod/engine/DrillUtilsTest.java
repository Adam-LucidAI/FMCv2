package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrillUtilsTest {
    @Test
    public void testTaperWithRulesUniformity() {
        int rows = 10;
        HoleLayout blank = new HoleLayout();
        for (int i = 0; i < rows; i++) {
            blank.addHole(new HoleSpec(i, 0.0, 0.0));
        }
        java.util.List<Double> drillSet = java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);
        FlowParameters params = new FlowParameters(150.0, 6.309, 1200.0);

        HoleLayout layout = DrillUtils.taperWithRules(blank, drillSet, params);
        assertEquals(rows, layout.getHoles().size());
        double err = FlowPhysics.computeUniformityError(layout, params);
        assertTrue(err <= 5.0, "Uniformity error too high: " + err);
    }
}
