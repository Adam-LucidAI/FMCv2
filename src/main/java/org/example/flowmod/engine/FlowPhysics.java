package org.example.flowmod.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for computing simple flow equations used by optimizers.
 */
public final class FlowPhysics {
    private static final double CD = 0.61;          // Discharge coefficient
    private static final double RHO = 1000.0;       // kg/m^3
    private static final double MU = 0.001;         // Pa.s
    private static final double EPS = 4.5e-5;       // m, typical roughness

    public FlowPhysics() {
    }

    /** Volumetric flow through a circular orifice, L/s. */
    public static double orificeFlowLps(double dMm, double deltaP_kPa) {
        double d = dMm / 1000.0;
        double area = Math.PI * d * d / 4.0;
        double dp = deltaP_kPa * 1000.0;
        double q = CD * area * Math.sqrt(2.0 * dp / RHO);
        return q * 1000.0; // m^3/s to L/s
    }

    /** Pressure drop (kPa) required to get Q L/s through orifice d mm. */
    public static double orificeDeltaP_kPa(double qLps, double dMm) {
        double q = qLps / 1000.0;
        double d = dMm / 1000.0;
        double area = Math.PI * d * d / 4.0;
        double v = q / (CD * area);
        double dp = (v * v) * RHO / 2.0;
        return dp / 1000.0;
    }

    /** Darcy–Weisbach friction head loss (kPa) for pipe section. */
    public static double frictionDrop_kPa(double lengthMm, double idMm, double flowLps) {
        double L = lengthMm / 1000.0;
        double D = idMm / 1000.0;
        double Q = flowLps / 1000.0;
        if (Q <= 0.0 || D <= 0.0 || L <= 0.0) {
            return 0.0;
        }
        double area = Math.PI * D * D / 4.0;
        double v = Q / area;
        double Re = RHO * v * D / MU;
        double f;
        if (Re <= 4000.0 && Re > 0) {
            f = 64.0 / Re;
        } else {
            double term = EPS / (3.7 * D) + 5.74 / Math.pow(Re, 0.9);
            f = 0.25 / Math.pow(Math.log10(term), 2.0);
        }
        double dp = f * (L / D) * (RHO * v * v / 2.0);
        return dp / 1000.0;
    }

    /** Compute per-row flow for a HoleLayout, returning List<Double> L/s. */
    public static List<Double> rowFlows(HoleLayout layout, FlowParameters p) {
        List<HoleSpec> holes = layout.getHoles();
        int n = holes.size();
        if (n == 0) {
            return List.of();
        }
        double spacing = p.headerLenMm() / (double) n;
        double qTotal = p.flowLps();
        double idMm = p.pipeDiameterMm();

        double pLow = 0.001;
        double pHigh = 1000.0;
        List<Double> flows = null;
        for (int iter = 0; iter < 40; iter++) {
            double mid = (pLow + pHigh) / 2.0;
            flows = computeFlowsForP0(mid, holes, spacing, idMm, qTotal);
            double sum = flows.stream().mapToDouble(Double::doubleValue).sum();
            if (sum > qTotal) {
                pHigh = mid;
            } else {
                pLow = mid;
            }
        }
        return flows == null ? List.of() : flows;
    }

    private static List<Double> computeFlowsForP0(double p0_kPa, List<HoleSpec> holes,
                                                  double spacingMm, double idMm, double totalFlow) {
        List<Double> result = new ArrayList<>();
        double remaining = totalFlow;
        double pressure = p0_kPa;
        for (int i = 0; i < holes.size(); i++) {
            if (i > 0) {
                pressure -= frictionDrop_kPa(spacingMm, idMm, remaining);
            }
            double q = orificeFlowLps(holes.get(i).holeDiameterMm(), Math.max(pressure, 0.0));
            q = Math.min(q, remaining);
            result.add(q);
            remaining -= q;
        }
        return result;
    }

    /** Return uniformity error (%CV) = 100*σ/μ across rows. */
    public static double computeUniformityError(HoleLayout layout, FlowParameters p) {
        List<Double> flows = rowFlows(layout, p);
        if (flows.isEmpty()) {
            return 0.0;
        }
        double mean = flows.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = flows.stream().mapToDouble(f -> (f - mean) * (f - mean)).sum() / flows.size();
        double sd = Math.sqrt(variance);
        return mean == 0.0 ? 0.0 : (sd / mean) * 100.0;
    }
}
