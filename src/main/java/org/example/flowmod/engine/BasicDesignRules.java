package org.example.flowmod.engine;

import java.util.List;

/**
 * Simple immutable implementation of {@link DesignRules}.
 */
public record BasicDesignRules(int rowCount, List<Double> allowableDrillSizesMm) implements DesignRules {
}
