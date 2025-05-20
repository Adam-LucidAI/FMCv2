package org.example.flowmod.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Container describing the arrangement of holes.
 */
public class HoleLayout {
    private final List<HoleSpec> holes = new ArrayList<>();

    public void addHole(HoleSpec spec) {
        holes.add(spec);
    }

    public List<HoleSpec> getHoles() {
        return Collections.unmodifiableList(holes);
    }
}
