package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;

/**
 * @author KarnickelDev
 * @since 04.03.2026
 **/
public interface OrbitMathKernel {

    void computeRange(OrbitDataSoA soa, OrbitLocalFrame out, long simTimeMicros, int low, int high);

}
