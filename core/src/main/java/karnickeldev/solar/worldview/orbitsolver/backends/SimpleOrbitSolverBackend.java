package karnickeldev.solar.worldview.orbitsolver.backends;

import karnickeldev.solar.worldview.orbitsolver.OrbitExecutionBackend;
import karnickeldev.solar.worldview.orbitsolver.OrbitJob;
import karnickeldev.solar.worldview.orbitsolver.OrbitLocalFrame;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public class SimpleOrbitSolverBackend implements OrbitExecutionBackend {

    private static final int CHUNK_SIZE = 512;

    @Override
    public void waitUntilFinished() {
        // nop
    }

    @Override
    public void shutdown() {
        // nop
    }

    @Override
    public void startExecute(OrbitJob[] jobs, OrbitLocalFrame out, long simTimeMicros) {
        for (OrbitJob j : jobs) {
            for (int low = 0; low < j.soa.getCount(); low += CHUNK_SIZE) {
                int high = Math.min(j.soa.getCount(), low + CHUNK_SIZE);
                j.kernel.computeRange(j.soa, out, simTimeMicros, low, high);
            }
        }
    }

}
