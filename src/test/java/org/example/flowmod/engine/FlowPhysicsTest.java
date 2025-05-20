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
        assertTrue(Math.abs(drop - 0.35) / 0.35 < 0.1);
    }
}
