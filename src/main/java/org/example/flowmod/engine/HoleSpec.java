package org.example.flowmod.engine;

/**
 * Specification for a single row hole.
 *
 * @param rowIndex        zero-based row index along the header
 * @param holeDiameterMm  drill diameter in millimetres
 * @param angleDeg        angle of the hole in degrees
 * @param spacingMm       distance between rows in millimetres
 */
public record HoleSpec(int rowIndex,
                       double holeDiameterMm,
                       double angleDeg,
                       double spacingMm) {

    /**
     * Axial position of the hole centre along the header, millimetres.
     */
    public double axialPosMm() {
        return rowIndex * spacingMm;
    }
}
