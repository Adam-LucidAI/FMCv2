package org.example.flowmod.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container describing the arrangement of holes.
 */
public class HoleLayout {
    private final List<HoleSpec> holes = new ArrayList<>();

    /**
     * Construct a layout with uniformly spaced rows at the provided positions.
     * The spacing between the first two positions is used as the nominal grid
     * spacing for all holes.
     */
    public static HoleLayout withRows(List<Double> positions, double holeDiameterMm) {
        HoleLayout layout = new HoleLayout();
        if (positions.isEmpty()) {
            return layout;
        }
        double spacing = positions.size() > 1 ? positions.get(1) - positions.get(0) : positions.get(0);
        for (int i = 0; i < positions.size(); i++) {
            layout.addHole(new HoleSpec(i, holeDiameterMm, 0.0, spacing));
        }
        return layout;
    }

    public void addHole(HoleSpec spec) {
        holes.add(spec);
    }

    public List<HoleSpec> getHoles() {
        return Collections.unmodifiableList(holes);
    }
}
