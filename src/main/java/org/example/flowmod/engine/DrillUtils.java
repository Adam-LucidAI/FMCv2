package org.example.flowmod.engine;

/**
 * Collection of helper utilities for dealing with drill operations.
 */
public final class DrillUtils {
    private DrillUtils() {
    }

    public static HoleSpec createHole(int rowIndex, double diameterMm, double angleDeg) {
        return new HoleSpec(rowIndex, diameterMm, angleDeg);
    }

    /**
     * Largest-first taper algorithm.
     */
    public static HoleLayout taperWithRules(
            HoleLayout blank, java.util.List<Double> drillSet, FlowParameters p) {
        java.util.List<HoleSpec> holes = new java.util.ArrayList<>(blank.getHoles());
        if (holes.isEmpty()) {
            return blank;
        }
        drillSet = new java.util.ArrayList<>(drillSet);
        drillSet.sort(java.util.Comparator.reverseOrder());

        // assign largest drill to all rows
        double largest = drillSet.get(0);
        for (int i = 0; i < holes.size(); i++) {
            HoleSpec h = holes.get(i);
            holes.set(i, new HoleSpec(h.rowIndex(), largest, h.angleDeg()));
        }

        HoleLayout layout = toLayout(holes);
        double error = FlowPhysics.computeUniformityError(layout, p);
        int drillIdxMax = drillSet.size() - 1;

        // target 5% coefficient of variation
        final double target = 5.0;
        while (error > target) {
            boolean changed = false;
            for (int i = 0; i < holes.size() && error > target; i++) {
                HoleSpec h = holes.get(i);
                int pos = drillSet.indexOf(h.holeDiameterMm());
                if (pos < drillIdxMax) {
                    holes.set(i, new HoleSpec(h.rowIndex(), drillSet.get(pos + 1), h.angleDeg()));
                    layout = toLayout(holes);
                    error = FlowPhysics.computeUniformityError(layout, p);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }

        return minimiseDrillChanges(layout);
    }

    private static HoleLayout minimiseDrillChanges(HoleLayout layout) {
       
        return layout;

        java.util.List<HoleSpec> result = new java.util.ArrayList<>();
        java.util.List<HoleSpec> currentGroup = new java.util.ArrayList<>();
        Double prevDiameter = null;

        for (HoleSpec h : layout.getHoles()) {
            double d = h.holeDiameterMm();
            if (prevDiameter == null || Double.compare(prevDiameter, d) == 0) {
                currentGroup.add(h);
            } else {
                // flush previous group before starting a new one
                result.addAll(currentGroup);
                currentGroup.clear();
                currentGroup.add(h);
            }
            prevDiameter = d;
        }

        result.addAll(currentGroup);
        return toLayout(result);
      main
    }

    private static HoleLayout toLayout(java.util.List<HoleSpec> specs) {
        HoleLayout layout = new HoleLayout();
        for (HoleSpec h : specs) {
            layout.addHole(h);
        }
        return layout;
    }
}
