package org.example.flowmod.engine;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Utilities for computing simple flow equations used by optimizers.
 */
public final class FlowPhysics {
    private static final double RHO = 1000.0;       // kg/m^3
    private static final double MU = 0.001;         // Pa.s
    private static final double EPS = 4.5e-5;       // m, typical roughness (0.045 mm)

    public FlowPhysics() {
    }

    /** Empirical discharge coefficient as a function of Reynolds number. */
    private static double computeCd(double Re) {
        if (Re <= 0) {
            return 0.0;
        }
        return 0.611 - 0.075 * Math.pow(Re / 1.0e5, -0.2);
    }

    /** Volumetric flow through a circular orifice, L/s. */
    public static double orificeFlowLps(double dMm, double dp_kPa) {
        double area = Math.PI * dMm * dMm / 4e6;
        return 0.61 * area * Math.sqrt(2 * dp_kPa * 1000 / 1000);
    }

    /** Pressure drop (kPa) required to get Q L/s through orifice d mm. */
    public static double orificeDeltaP_kPa(double qLps, double dMm) {
        double q = qLps / 1000.0;
        double area = Math.PI * dMm * dMm / 4e6;
        return Math.pow(q / (0.61 * area), 2) / 2;
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

    /**
     * Compute per-row flow for a {@link HoleLayout} using the provided suction pressure.
     */
    public static List<Double> rowFlows(HoleLayout layout, FlowParameters p, double suctionKPa) {
        List<HoleSpec> holes = layout.getHoles();
        int rows = holes.size();
        if (rows == 0) {
            return List.of();
        }

        double idMm = p.pipeDiameterMm();
        double[] qh = new double[rows];

        double pipeFlow = p.flowLps();
        double localP = suctionKPa;

        for (int i = 0; i < rows; i++) {
            HoleSpec h = holes.get(i);
            double dp = -localP;
            qh[i] = orificeFlowLps(h.holeDiameterMm(), dp);

            pipeFlow -= qh[i];
            if (i < rows - 1) {
                HoleSpec next = holes.get(i + 1);
                double dx = next.axialPosMm() - h.axialPosMm();
                if (dx < 0) {
                    dx = 0.0;
                }
                localP -= frictionDrop_kPa(dx, idMm, Math.abs(pipeFlow));
            }
        }

        List<Double> flows = new ArrayList<>();
        for (double q : qh) {
            flows.add(q);
        }
        return flows;
    }

    /**
     * Find the suction pressure that balances total row flow with the target flow rate.
     */
    public static double findRequiredSuctionKPa(HoleLayout layout, FlowParameters p,
                                                double pMin, double pMax) {
        BrentSolver solver = new BrentSolver(1e-6);
        UnivariateFunction fn = x -> {
            List<Double> flows = rowFlows(layout, p, x);
            double total = flows.stream().mapToDouble(Double::doubleValue).sum();
            return total - p.flowLps();
        };

        double low = -200.0;
        double high = -0.5;
        double fHigh = fn.value(high);
        if (fHigh < 0) {
            throw new DesignNotConvergedException("Suction exceeds -0.5 kPa");
        }

        double root = solver.solve(100, fn, low, high);
        for (int i = 0; i < 20; i++) {
            double err = fn.value(root);
            if (Math.abs(err) < 1e-4) {
                break;
            }
            root = solver.solve(100, fn, root - 1.0, root + 1.0);
        }
        return root;
    }

    /** Return uniformity error (%CV) = 100*σ/μ across rows. */
    public static double computeUniformityError(HoleLayout layout, FlowParameters p) {
        double suction = findRequiredSuctionKPa(layout, p, -1000.0, -1.0);
        List<Double> flows = rowFlows(layout, p, suction);
        org.apache.commons.math3.stat.descriptive.DescriptiveStatistics stats = new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics();
        for (double q : flows) {
            stats.addValue(q);
        }
        double cvPct = 100 * stats.getStandardDeviation() / stats.getMean();
        return cvPct;
    }

    /** Alias for {@link #computeUniformityError(HoleLayout, FlowParameters)}. */
    public static double CV(HoleLayout layout, FlowParameters p) {
        return computeUniformityError(layout, p);
    }

    // balanceSupplyPressure() removed
}
