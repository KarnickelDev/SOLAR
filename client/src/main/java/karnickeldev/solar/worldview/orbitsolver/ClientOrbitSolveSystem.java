package karnickeldev.solar.worldview.orbitsolver;

import com.badlogic.gdx.Gdx;
import jdk.incubator.vector.DoubleVector;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.worldview.orbitsolver.backends.PersistentOrbitWorkerBackend;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public class ClientOrbitSolveSystem implements OrbitSolver {

    private static final int DEFAULT_CHUNK_SIZE;
    static {
        int vectorLen = DoubleVector.SPECIES_PREFERRED.length();
        int perEntityBytes = 128; // estimate bytes-per-entity in hot arrays
        int cacheSize = 256 * 1024; // estimated cache size (either 32 for L1 or 256 for L2 seem to work well)
        DEFAULT_CHUNK_SIZE = Math.max(vectorLen, ((cacheSize / perEntityBytes) / vectorLen) * vectorLen); // round to vector multiple
    }

    private final OrbitExecutionBackend backend;

    private final OrbitLocalFrame frameA;
    private final OrbitLocalFrame frameB;

    public final ClientOrbitPrecisionManager precisionManager;

    private OrbitLocalFrame currentFrame;
    private OrbitLocalFrame nextFrame;

    public ClientOrbitSolveSystem(int capacity, ClientThreadLayout layout) {
        this.frameA = new OrbitLocalFrame();
        this.frameB = new OrbitLocalFrame();
        this.currentFrame = frameA;
        this.nextFrame = frameB;

        this.backend = new PersistentOrbitWorkerBackend(layout, DEFAULT_CHUNK_SIZE);

        this.precisionManager = new ClientOrbitPrecisionManager(capacity);
    }

    public ClientOrbitSolveSystem(int capacity, int workerCount) {
        this.frameA = new OrbitLocalFrame();
        this.frameB = new OrbitLocalFrame();
        this.currentFrame = frameA;
        this.nextFrame = frameB;

        this.backend = new PersistentOrbitWorkerBackend(workerCount, DEFAULT_CHUNK_SIZE);

        this.precisionManager = new ClientOrbitPrecisionManager(capacity);
    }

    public void beginNextFrame(OrbitSolveInput input) {
        precisionManager.updateContext(input);
        OrbitJob[] jobs = precisionManager.buildJobs(Gdx.graphics.getDeltaTime());

        backend.startExecute(jobs, nextFrame, input.simTimeMicros());
    }

    @Override
    public void onOrbitGraphRebuild(OrbitSolveInput input) {
        precisionManager.rebuild(input);
    }

    public void finishFrame() {
        backend.waitUntilFinished();

        OrbitLocalFrame tmp = currentFrame;
        currentFrame = nextFrame;
        nextFrame = tmp;

        // TODO: THIS VERY BAD!!!
        System.arraycopy(currentFrame.sectorX, 0, nextFrame.sectorX, 0, currentFrame.sectorX.length);
        System.arraycopy(currentFrame.localX, 0, nextFrame.localX, 0, currentFrame.localX.length);
        System.arraycopy(currentFrame.sectorY, 0, nextFrame.sectorY, 0, currentFrame.sectorY.length);
        System.arraycopy(currentFrame.localY, 0, nextFrame.localY, 0, currentFrame.localY.length);
    }

    public void shutdown() {
        backend.shutdown();
    }

    public OrbitLocalFrame getCurrentFrame() {
        return currentFrame;
    }
}
