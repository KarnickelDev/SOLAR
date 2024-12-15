package karnickeldev.solartest.ui;

import karnickeldev.solar.physics.Units;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;


public class PhysicsUnitTests {


    @Test
    public void testConversions() {
        assertEquals(Units.toSU(5f, Units.Length.KILOMETER), Units.toSU(5000f, Units.Length.METER));

        assertTrue(Units.assertEqualWithinError(Units.toSU(0.2, Units.Length.AU),
            Units.getInSU(0.2 * Units.ASTRONOMIC_UNIT, Units.Length.KILOMETER),
            0.00001));

        assertTrue(Units.assertEqualWithinError(Units.toSU(200, Units.Length.AU),
            Units.getInSU(200 * Units.ASTRONOMIC_UNIT, Units.Length.KILOMETER),
            0.00001));

    }

}
