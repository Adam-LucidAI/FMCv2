package org.example.flowmod.engine;

/**
 * Collection of helper utilities for dealing with drill operations.
 */
public final class DrillUtils {
    private DrillUtils() {
    }

    /** Candidate row spacings (mm) to try in descending order. */
    public static final double[] SPACING_CANDIDATES =
            {150.0, 120.0, 100.0, 80.0, 60.0, 50.0};

    /**
     * Generate uniform row positions for the provided spacing.
     *
     * @param headerLenMm total header length in millimetres
     * @param dxMm        desired spacing between holes
     * @return list of axial positions for the hole centres
     */
    public static java.util.List<Double> generateCandidateRows(double headerLenMm,
                                                               double dxMm) {
        java.util.List<Double> rows = new java.util.ArrayList<>();
        if (dxMm <= 0.0 || headerLenMm <= 0.0) {
            return rows;
        }
        for (double x = 0.0; x + dxMm <= headerLenMm; x += dxMm) {
            rows.add(x);
        }
        return rows;
    }

    public static HoleSpec createHole(int rowIndex, double diameterMm, double angleDeg, double spacingMm) {
        return new HoleSpec(rowIndex, diameterMm, angleDeg, spacingMm);
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

        java.util.List<Double> sizes = new java.util.ArrayList<>(drillSet);
        sizes.sort(java.util.Comparator.reverseOrder());

        double largest = sizes.get(0);
        for (int i = 0; i < holes.size(); i++) {
            HoleSpec h = holes.get(i);
            holes.set(i, new HoleSpec(h.rowIndex(), largest, h.angleDeg(), h.spacingMm()));
        }

        HoleLayout layout = toLayout(holes);
        final double target = 5.0;

        while (true) {
            double suction = FlowPhysics.findRequiredSuctionKPa(layout, p, -100.0, -1.0);
            java.util.List<Double> flows = FlowPhysics.rowFlows(layout, p, suction);
            org.apache.commons.math3.stat.descriptive.DescriptiveStatistics stats =
                    new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics();
            for (double q : flows) {
                stats.addValue(q);
            }
            double err = 100 * stats.getStandardDeviation() / stats.getMean();
            if (err <= target) {
                break;
            }

            int idx = -1;
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < flows.size(); i++) {
                double f = flows.get(i);
                if (f > max) {
                    max = f;
                    idx = i;
                }
            }

            if (idx < 0) {
                break;
            }

            HoleSpec h = holes.get(idx);
            int pos = sizes.indexOf(h.holeDiameterMm());
            if (pos < sizes.size() - 1) {
                // shrink to next smaller drill size
                holes.set(idx, new HoleSpec(h.rowIndex(), sizes.get(pos + 1), h.angleDeg(), h.spacingMm()));
            } else {
                // cannot shrink further -> remove the row
                holes.remove(idx);
                if (holes.isEmpty()) {
                    break;
                }
            }
            layout = toLayout(holes);
        }

        return layout;
    }

    private static HoleLayout minimiseDrillChanges(HoleLayout layout) {
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
    }

    private static HoleLayout toLayout(java.util.List<HoleSpec> specs) {
        HoleLayout layout = new HoleLayout();
        for (HoleSpec h : specs) {
            layout.addHole(h);
        }
        return layout;
    }
}
