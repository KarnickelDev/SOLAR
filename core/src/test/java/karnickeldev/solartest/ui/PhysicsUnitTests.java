package karnickeldev.solartest.ui;

import karnickeldev.solar.physics.Units;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;


public class PhysicsUnitTests {


    @Test
    public void testConversions() {
        assertEquals(Units.toSU(5f, Units.Length.KILOMETER), Units.toSU(5000f, Units.Length.METER));
    }

}
