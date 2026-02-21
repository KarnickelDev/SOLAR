package karnickeldev.solar.render.orbitupdate;

import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.ecs.EntityManager;

/**
 * @author KarnickelDev
 * @since 22.11.2025
 **/
public interface OrbitUpdater {

    /**
     * Initiate computing Entity Positions from Orbit Elements
     * @param time current simulation time
     * @param ecs the ecs of the rendered world
     */
    void startCompute(long time, ClientECS ecs);

    /**
     * wait for orbit computation to finish
     * and update(swap) the output FrameData
     */
    void waitAndSwap();

    /**
     * shut down this object and all workers
     */
    void shutdown();

    /**
     * Retrieve current frame data for rendering
     * @return A FrameData object
     */
    FrameData getFrameData();

    /**
     * Prepare the OrbitUpdater
     * Used to mask initial page faults with loading screen
     * @param ecs ECS of the rendered world
     */
    void prepare(ClientECS ecs);

    /**
     * If the warmup phase is active
     */
    boolean isWarmupActive();

    /**
     * Returns the number of necessary warmup tasks
     */
    int warmupTarget();

    /**
     * Storage class holding data that is needed for computations every frame
     */
    final class FrameData {
        public final short[] sectorX = new  short[EntityManager.MAX_ENTITIES];
        public final double[] localX = new double[EntityManager.MAX_ENTITIES];
        public final short[] sectorY = new  short[EntityManager.MAX_ENTITIES];
        public final double[] localY = new double[EntityManager.MAX_ENTITIES];
        public final int[] entityIds = new int[EntityManager.MAX_ENTITIES];
        public final int[] parentIds = new int[EntityManager.MAX_ENTITIES];
        public int validCount = 0;
    }
}
