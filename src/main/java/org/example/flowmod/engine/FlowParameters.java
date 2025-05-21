package org.example.flowmod.engine;

/**
 * Parameters describing the fluid flow system.
 *
 * @param pipeDiameterMm internal pipe diameter in millimetres
 * @param flowLps        total volumetric flow supplied to the header in litres per second
 * @param headerLenMm    header length in millimetres
 * @param headerType     operating mode of the header
 */
public record FlowParameters(double pipeDiameterMm,
                             double flowLps,
                             double headerLenMm,
                             HeaderType headerType) {
}
