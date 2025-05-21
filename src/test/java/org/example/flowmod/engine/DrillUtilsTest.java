package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrillUtilsTest {
    @Test
    public void testTaperWithRulesUniformity() {
        int rows = 10;
        HoleLayout blank = new HoleLayout();
        double spacing = 1200.0 / rows;
        for (int i = 0; i < rows; i++) {
            blank.addHole(new HoleSpec(i, 0.0, 0.0, spacing));
        }
        java.util.List<Double> drillSet = java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);
        FlowParameters params = new FlowParameters(150.0, 6.309, 1200.0, 100.0, HeaderType.PRESSURE);

        HoleLayout layout = DrillUtils.taperWithRules(blank, drillSet, params);
        assertEquals(rows, layout.getHoles().size());
        FlowParameters tuned = FlowPhysics.balanceSupplyPressure(layout, params);
        double err = FlowPhysics.computeUniformityError(layout, tuned);
        assertTrue(err <= 5.0, "Uniformity error too high: " + err);
    }

    @Test
    public void testTaperWithRulesMonotonicDiameters() {
        int rows = 10;
        HoleLayout blank = new HoleLayout();
        double spacing = 1200.0 / rows;
        for (int i = 0; i < rows; i++) {
            blank.addHole(new HoleSpec(i, 0.0, 0.0, spacing));
        }
        java.util.List<Double> drillSet = java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);
        FlowParameters params = new FlowParameters(150.0, 6.309, 1200.0, 100.0, HeaderType.PRESSURE);

        HoleLayout layout = DrillUtils.taperWithRules(blank, drillSet, params);
        double prev = 0.0;
        for (HoleSpec h : layout.getHoles()) {
            assertTrue(h.holeDiameterMm() >= prev, "Diameters should be non-decreasing along the header");
            prev = h.holeDiameterMm();
        }
    }

    @Test
    public void testTaperWithRulesNoAvailableReduction() {
        int rows = 5;
        HoleLayout blank = new HoleLayout();
        double spacing = 1200.0 / rows;
        for (int i = 0; i < rows; i++) {
            blank.addHole(new HoleSpec(i, 0.0, 0.0, spacing));
        }
        java.util.List<Double> drillSet = java.util.List.of(16.0);
        FlowParameters params = new FlowParameters(150.0, 6.309, 1200.0, 100.0, HeaderType.PRESSURE);

        HoleLayout layout = DrillUtils.taperWithRules(blank, drillSet, params);
        for (HoleSpec h : layout.getHoles()) {
            assertEquals(16.0, h.holeDiameterMm());
        }
        FlowParameters tuned2 = FlowPhysics.balanceSupplyPressure(layout, params);
        double err = FlowPhysics.computeUniformityError(layout, tuned2);
        assertTrue(err > 5.0);
    }

    @Test
    public void testTaperHighFlowMultipleSizes() {
        int rows = (int) Math.floor(1300.0 / DesignRules.DEFAULT_ROW_SPACING_MM);
        HoleLayout blank = new HoleLayout();
        double spacing = 1300.0 / rows;
        for (int i = 0; i < rows; i++) {
            blank.addHole(new HoleSpec(i, 0.0, 0.0, spacing));
        }
        java.util.List<Double> drillSet = java.util.List.of(16.0, 14.0, 12.0, 10.0, 8.0, 6.0, 4.0);

        double gpm = 120.0;
        double lps = gpm * 0.0631;
        FlowParameters params = new FlowParameters(200.0, lps, 1300.0, 100.0, HeaderType.PRESSURE);

        HoleLayout layout = DrillUtils.taperWithRules(blank, drillSet, params);
        java.util.Set<Double> used = new java.util.HashSet<>();
        for (HoleSpec h : layout.getHoles()) {
            used.add(h.holeDiameterMm());
        }
        assertTrue(used.size() >= 4, "Expected at least 4 drill sizes but got " + used.size());
        FlowParameters tuned3 = FlowPhysics.balanceSupplyPressure(layout, params);
        double err = FlowPhysics.computeUniformityError(layout, tuned3);
        assertTrue(err <= 5.0, "Uniformity error too high: " + err);
    }
}
