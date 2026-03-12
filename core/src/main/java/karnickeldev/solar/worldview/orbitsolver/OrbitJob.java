package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;

/**
 * @author KarnickelDev
 * @since 27.03.2026
 **/
public final class OrbitJob {

    public OrbitDataSoA soa;
    public OrbitMathKernel kernel;
    public int count;

    public OrbitJob(OrbitDataSoA soa, OrbitMathKernel kernel) {
        this.soa = soa;
        this.kernel = kernel;
        this.count = soa.getCount();
    }

}
