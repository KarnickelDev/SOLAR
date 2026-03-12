package karnickeldev.solar.worldview.orbitsolver;

import jdk.incubator.vector.DoubleVector;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.worldview.orbitsolver.backends.PersistentOrbitWorkerBackend;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitMathKernelSIMDCritical;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitMathKernelSIMDSlow;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 03.03.2026
 **/
public class ClientOrbitSolveSystem implements OrbitSolver{

    private static final int DEFAULT_CHUNK_SIZE;
    static {
        int vectorLen = DoubleVector.SPECIES_PREFERRED.length();
        int perEntityBytes = 128; // estimate bytes-per-entity in hot arrays
        int cacheSize = 256 * 1024; // estimated cache size (either 32 for L1 or 256 for L2 seem to work well)
        DEFAULT_CHUNK_SIZE = Math.max(vectorLen, ((cacheSize / perEntityBytes) / vectorLen) * vectorLen); // round to vector multiple
    }

    private final OrbitExecutionBackend backend;
    private final OrbitMathKernel kernel;

    private OrbitMathKernel slowKernel = new OrbitMathKernelSIMDSlow();

    private final OrbitDataSoA soa;
    private final OrbitLocalFrame frameA;
    private final OrbitLocalFrame frameB;

    private final OrbitPrecisionManager precisionManager;

    private OrbitLocalFrame currentFrame;
    private OrbitLocalFrame nextFrame;

    public ClientOrbitSolveSystem(int capacity, ClientThreadLayout layout) {
        this.soa = new OrbitDataSoA(capacity);

        this.frameA = new OrbitLocalFrame();
        this.frameB = new OrbitLocalFrame();
        this.currentFrame = frameA;
        this.nextFrame = frameB;

        this.backend = new PersistentOrbitWorkerBackend(layout, DEFAULT_CHUNK_SIZE);
        this.kernel = new OrbitMathKernelSIMDCritical();

        this.precisionManager = new OrbitPrecisionManager(EntityManager.MAX_ENTITIES);
    }

    public ClientOrbitSolveSystem(int capacity, int workerCount) {
        this.soa = new OrbitDataSoA(capacity);

        this.frameA = new OrbitLocalFrame();
        this.frameB = new OrbitLocalFrame();
        this.currentFrame = frameA;
        this.nextFrame = frameB;

        this.backend = new PersistentOrbitWorkerBackend(workerCount, DEFAULT_CHUNK_SIZE);
        this.kernel = new OrbitMathKernelSIMDCritical();

        this.precisionManager = new OrbitPrecisionManager(EntityManager.MAX_ENTITIES);
    }

    public void beginNextFrame(OrbitSolveInput input) {
        //OrbitDataSoA.build(input, soa);
        //OrbitJob[] jobs = new OrbitJob[]{new OrbitJob(soa, kernel, soa.getCount())};

        precisionManager.updateContext(input);
        OrbitJob[] jobs = new OrbitJob[]{
            new OrbitJob(precisionManager.getTiers()[0], kernel),
            new OrbitJob(precisionManager.getTiers()[1], slowKernel)
        };

        backend.startExecute(jobs, nextFrame, input.simTimeMicros());
    }

    public void finishFrame() {
        backend.waitUntilFinished();

        OrbitLocalFrame tmp = currentFrame;
        currentFrame = nextFrame;
        nextFrame = tmp;
    }

    public void shutdown() {
        backend.shutdown();
    }

    public OrbitLocalFrame getCurrentFrame() {
        return currentFrame;
    }
}
