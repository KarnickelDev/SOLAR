package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public interface OrbitExecutionBackend {

    void startExecute(OrbitJob[] jobs, OrbitLocalFrame out, long simTimeMicros);

    void waitUntilFinished();

    void shutdown();
}
