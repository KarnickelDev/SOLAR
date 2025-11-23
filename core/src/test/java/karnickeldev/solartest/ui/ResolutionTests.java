package karnickeldev.solartest.ui;

public class ResolutionTests {


//    @Test
//    public void test_extractResolution() {
//        for (int i = 0; i < Resolution.values().length; i++) {
//            try {
//                Resolution extracted = Resolution.extractResolution(Resolution.SUPPORTED_RESOLUTIONS[i]);
//                assertEquals(extracted.toString(), Resolution.SUPPORTED_RESOLUTIONS[i]);
//            } catch (Exception e) {
//                fail("extractResolution() should never fail here!");
//            }
//        }
//
//        assertThrows(IllegalArgumentException.class, () -> Resolution.extractResolution(null));
//        assertThrows(IllegalArgumentException.class, () -> Resolution.extractResolution("abcde"));
//        assertThrows(IllegalArgumentException.class, () -> Resolution.extractResolution("1x1"));
//        assertThrows(IllegalArgumentException.class, () -> Resolution.extractResolution("1920+1080"));
//        assertThrows(IllegalArgumentException.class, () -> Resolution.extractResolution(
//            Resolution.resolutionToString(99_999, -1)
//        ));
//
//    }
//
//
//    @Test
//    public void test_matchResolution() {
//
//        for (Resolution res : Resolution.values()) {
//            assertEquals(res,
//                Resolution.matchResolution(res.getWidth(), res.getHeight()));
//        }
//
//        assertEquals(Resolution.R_1920_1080,
//            Resolution.matchResolution(Resolution.resolutionToString(1921, 1081)));
//
//        assertEquals(Resolution.R_1920_1080,
//            Resolution.matchResolution(Resolution.resolutionToString(2000, 1000)));
//
//        assertEquals(Resolution.R_1280_960,
//            Resolution.matchResolution(Resolution.resolutionToString(1000, 1000)));
//
//        assertEquals(Resolution.R_1440_1080,
//            Resolution.matchResolution(Resolution.resolutionToString(1400, 1100)));
//
//        assertEquals(Resolution.R_1280_960,
//            Resolution.matchResolution(Resolution.resolutionToString(1400, 900)));
//
//    }

}
