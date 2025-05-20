package org.example.flowmod.engine;

/**
 * Specification for a single row hole.
 *
 * @param rowIndex        zero-based row index along the header
 * @param holeDiameterMm  drill diameter in millimetres
 * @param angleDeg        angle of the hole in degrees
 */
public record HoleSpec(int rowIndex, double holeDiameterMm, double angleDeg) {
}
