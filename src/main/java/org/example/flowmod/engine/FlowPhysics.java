package org.example.flowmod.engine;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

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
    public static double orificeFlowLps(double dMm, double deltaP_kPa) {
        double d = dMm / 1000.0;
        double area = Math.PI * d * d / 4.0;
        double dp = deltaP_kPa * 1000.0;

        double q = 0.0;
        double cd = 0.611;
        for (int i = 0; i < 10; i++) {
            q = cd * area * Math.sqrt(2.0 * dp / RHO);
            double v = q / area;
            double Re = RHO * v * d / MU;
            double newCd = computeCd(Re);
            if (Math.abs(newCd - cd) < 1e-6) {
                cd = newCd;
                break;
            }
            cd = newCd;
        }
        return q * 1000.0; // m^3/s to L/s
    }

    /** Pressure drop (kPa) required to get Q L/s through orifice d mm. */
    public static double orificeDeltaP_kPa(double qLps, double dMm) {
        double q = qLps / 1000.0;
        double d = dMm / 1000.0;
        double area = Math.PI * d * d / 4.0;
        double v = q / area;
        double Re = RHO * v * d / MU;
        double cd = computeCd(Re);
        double dp = Math.pow(q / (cd * area), 2.0) * RHO / 2.0;
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

        double spacing = p.headerLenMm() / (double) n;
        double idMm = p.pipeDiameterMm();
        double totalFlow = p.flowLps();

        // mass balance function: total flow from given supply pressure
        java.util.function.DoubleFunction<Double> totalFromPressure = (press) -> {
            double pipeFlow = totalFlow;
            double localP = press;
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                HoleSpec h = holes.get(i);
                double dp = p.headerType() == HeaderType.PRESSURE ? localP : -localP;
                double q = orificeFlowLps(h.holeDiameterMm(), Math.max(dp, 0.0));
                sum += q;
                pipeFlow -= q;
                if (i < n - 1) {
                    double drop = frictionDrop_kPa(spacing, idMm, Math.abs(pipeFlow));
                    if (p.headerType() == HeaderType.PRESSURE) {
                        localP -= drop;
                    } else {
                        localP += drop;
                    }
                }
            }
            return sum;
        };

        org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer optimizer =
                new org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer();

        org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction model =
                (org.apache.commons.math3.linear.RealVector point) -> {
                    double x = point.getEntry(0);
                    double f0 = totalFromPressure.apply(x) - totalFlow;
                    double eps = 1e-6;
                    double fp = totalFromPressure.apply(x + eps);
                    double fm = totalFromPressure.apply(x - eps);
                    double derivative = (fp - fm) / (2 * eps);
                    org.apache.commons.math3.linear.RealVector value =
                            new org.apache.commons.math3.linear.ArrayRealVector(new double[]{f0});
                    org.apache.commons.math3.linear.RealMatrix jac =
                            new org.apache.commons.math3.linear.Array2DRowRealMatrix(new double[][]{{derivative}});
                    return new org.apache.commons.math3.util.Pair<>(value, jac);
                };

        org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem problem =
                new org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder()
                        .start(new double[]{10.0})
                        .target(new double[]{0.0})
                        .model(model)
                        .maxIterations(50)
                        .maxEvaluations(50)
                        .build();

        double supplyPressure;
        try {
            org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum opt =
                    optimizer.optimize(problem);
            supplyPressure = opt.getPoint().getEntry(0);
        } catch (Exception ex) {
            throw new org.apache.commons.math3.exception.ConvergenceException(
                    org.apache.commons.math3.exception.util.LocalizedFormats.SIMPLE_MESSAGE,
                    ex.getMessage());
        }

        if (Double.isNaN(supplyPressure)) {
            throw new org.apache.commons.math3.exception.ConvergenceException(
                    org.apache.commons.math3.exception.util.LocalizedFormats.SIMPLE_MESSAGE,
                    "Solver returned NaN pressure");
        }

        double[] flowsArr = new double[n];
        double pipeFlow = totalFlow;
        double localP = supplyPressure;
        for (int i = 0; i < n; i++) {
            HoleSpec h = holes.get(i);
            double dp = p.headerType() == HeaderType.PRESSURE ? localP : -localP;
            double q = orificeFlowLps(h.holeDiameterMm(), Math.max(dp, 0.0));
            flowsArr[i] = q;
            pipeFlow -= q;
            if (i < n - 1) {
                double drop = frictionDrop_kPa(spacing, idMm, Math.abs(pipeFlow));
                if (p.headerType() == HeaderType.PRESSURE) {
                    localP -= drop;
                } else {
                    localP += drop;
                }
            }
        }

        List<Double> flows = new ArrayList<>();
        for (double q : flowsArr) {
            if (Double.isNaN(q)) {
                throw new org.apache.commons.math3.exception.ConvergenceException(
                        org.apache.commons.math3.exception.util.LocalizedFormats.SIMPLE_MESSAGE,
                        "Solver returned NaN flow");
            }
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
        SummaryStatistics stats = new SummaryStatistics();
        for (double q : flows) {
            stats.addValue(q);
        }
        double mean = stats.getMean();
        double sd = stats.getStandardDeviation();
        return mean == 0.0 ? 0.0 : (sd / mean) * 100.0;
    }
}
