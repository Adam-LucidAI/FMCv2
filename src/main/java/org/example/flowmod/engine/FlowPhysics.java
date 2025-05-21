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

    /** Compute Reynolds number for given flow parameters. */
    public static double computeReynolds(FlowParameters p) {
        double area = Math.PI * Math.pow(p.pipeDiameterMm() / 1000.0, 2) / 4.0;
        double v = p.flowLps() / 1000.0 / area;
        return 1000.0 * v * (p.pipeDiameterMm() / 1000.0) / 0.001;
    }

    /** Compute per-row flow for a HoleLayout, returning List<Double> L/s. */
    public static List<Double> rowFlows(HoleLayout layout, FlowParameters p) {
        List<HoleSpec> holes = layout.getHoles();
        int n = holes.size();
        if (n == 0) {
            return List.of();
        }

        double spacing = DesignRules.DEFAULT_ROW_SPACING_MM;
        double qTotal = p.flowLps();

        double spacing = p.headerLenMm() / (double) n;

        double idMm = p.pipeDiameterMm();

        double[] flowsArr = new double[n];
        double pipeFlow = 0.0;
        double pressure = 0.0;

        // iterate from blind end toward the supply end
        for (int i = n - 1; i >= 0; i--) {
            HoleSpec hole = holes.get(i);
            double holeFlow = orificeFlowLps(hole.holeDiameterMm(), -pressure);
            flowsArr[i] = holeFlow;
            pipeFlow += holeFlow;

            if (i > 0) {
                pressure -= frictionDrop_kPa(spacing, idMm, pipeFlow);
            }
        }

        List<Double> flows = new ArrayList<>();
        for (double q : flowsArr) {
            flows.add(q);
        }
        return flows;
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
