package org.example.flowmod.engine;

/**
 * Parameters describing the fluid flow system.
 *
 * @param pipeDiameterMm internal pipe diameter in millimetres
 * @param flowLps        total volumetric flow supplied to the header in litres per second
 * @param headerLenMm    header length in millimetres
 */
public record FlowParameters(double pipeDiameterMm, double flowLps, double headerLenMm) {
}
