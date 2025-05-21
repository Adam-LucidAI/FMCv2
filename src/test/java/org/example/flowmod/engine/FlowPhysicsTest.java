package org.example.flowmod.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FlowPhysicsTest {
    @Test
    public void testOrificeRoundTrip() {
        double d = 10.0;
        double q = 0.5;
        double dp = FlowPhysics.orificeDeltaP_kPa(q, d);
        double q2 = FlowPhysics.orificeFlowLps(d, dp);
        assertEquals(q, q2, q * 0.01);
    }

    @Test
    public void testFrictionDrop() {
        double drop = FlowPhysics.frictionDrop_kPa(1000.0, 150.0, 6.0);
        assertEquals(0.0084, drop, 0.0005);
    }

    @Test
    public void testOrificeFlowKnownCase() {
        double q = FlowPhysics.orificeFlowLps(10.0, 20.0);
        assertEquals(0.303, q, 0.005);
    }

    @Test
    public void testComputeReynolds() {
        double gpm = 100.0;
        double lps = gpm * 0.0631;
        FlowParameters p = new FlowParameters(150.0, lps, 1000.0);
        double Re = FlowPhysics.computeReynolds(p);
        assertEquals(1.9e5, Re, 2e4);
    }

    @Test
    public void testOrificeMonotonic() {
        double q1 = FlowPhysics.orificeFlowLps(40.0, 1.0);
        double q100 = FlowPhysics.orificeFlowLps(40.0, 100.0);
        assertTrue(q1 * 10 <= q100);
    }
}
