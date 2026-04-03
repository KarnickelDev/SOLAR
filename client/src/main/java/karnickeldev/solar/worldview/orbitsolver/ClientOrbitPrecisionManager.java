package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.render.EntityRenderer;
import karnickeldev.solar.render.camera.FloatingOriginCamera;
import karnickeldev.solar.util.SplitCoordMath;
import karnickeldev.solar.util.WorldDelta;
import karnickeldev.solar.util.WorldPos;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitMathKernelSIMD;
import karnickeldev.solar.worldview.transform.WorldTransformData;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 02.04.2026
 **/
public final class ClientOrbitPrecisionManager implements OrbitPrecisionManager {

    private static final class OrbitTier {
        final OrbitDataSoA orbitSoA;
        final OrbitMathKernel kernel;

        double updatesPerSecond;
        double accumulator;
        int cursor;

        OrbitTier(int capacity, double updatesPerSecond, OrbitMathKernel kernel) {
            this.orbitSoA = new OrbitDataSoA(capacity);
            this.updatesPerSecond = updatesPerSecond;
            this.kernel = kernel;

            this.accumulator = 0;
            this.cursor = 0;
        }
    }

    private final Logger logger;

    private final OrbitTier[] tiers;

    private final int[] currentTier;

    private int tierUpdateCursor = 0;

    private boolean initialized = false;
    private boolean justRebuilt = false;

    public ClientOrbitPrecisionManager(int capacityPerTier) {
        this.logger = Logger.get(LogTag.ORBT_SLVR);

        this.currentTier = new int[capacityPerTier];
        Arrays.fill(this.currentTier, -1);

        this.tiers = new OrbitTier[]{
            new OrbitTier(capacityPerTier, -1, new OrbitMathKernelSIMD()),
            new OrbitTier(capacityPerTier, 90, new OrbitMathKernelSIMD(2)),
            new OrbitTier(capacityPerTier, 30, new OrbitMathKernelSIMD(2)),
        };
    }

    public void init(OrbitSolveInput input) {
        if(initialized) return;
        initialized = true;

        OrbitGraphData graph = input.orbitGraph();
        int[] dfs = graph.getDfsOrder();
        int count = input.usedCapacity();

        for (int i = 0; i < count; i++) {
            int anchor = dfs[i];

            int tier = computeTier(anchor, input);
            currentTier[anchor] = tier;

            tiers[tier].orbitSoA.addEntity(anchor, input);
        }
    }

    private void updateTier(int anchor, int newTier, OrbitSolveInput input) {
        int oldTier = currentTier[anchor];
        if(oldTier == newTier) return;

        if(oldTier >= 0) {
            tiers[oldTier].orbitSoA.removeEntity(anchor);
        }

        tiers[newTier].orbitSoA.addEntity(anchor, input);
        currentTier[anchor] = newTier;

        // TODO: immediate update to prevent starvation
    }

    @Override
    public void updateContext(OrbitSolveInput input) {
        init(input);
        if (justRebuilt) {
            justRebuilt = false;
            return;
        }

        OrbitGraphData graph = input.orbitGraph();
        int[] dfs = graph.getDfsOrder();
        int count = input.usedCapacity();

        // how many entities to re-evaluate this frame
        int batchSize = Math.max(512, count / 32);

        for (int i = 0; i < batchSize; i++) {
            int idx = (tierUpdateCursor + i) % count;
            int anchor = dfs[idx];

            if(currentTier[anchor] == -1) {
                logger.warn("Entity tier invalid");
                continue;
            }

            int newTier = computeTier(anchor, input);
            updateTier(anchor, newTier, input);
        }

        tierUpdateCursor = (tierUpdateCursor + batchSize) % count;
    }

    public void rebuild(OrbitSolveInput input) {
        if(!initialized) return;

        System.out.println("Rebuild:");

        // clear everything
        for (OrbitTier tier : tiers) {
            tier.orbitSoA.clear();
            tier.cursor = 0;
            tier.accumulator = 0;
        }

        Arrays.fill(currentTier, -1);

        // rebuild from scratch
        OrbitGraphData graph = input.orbitGraph();
        int[] dfs = graph.getDfsOrder();
        int count = input.usedCapacity();

        for (int i = 0; i < count; i++) {
            int anchor = dfs[i];

            int tier = computeTier(anchor, input);
            currentTier[anchor] = tier;

            tiers[tier].orbitSoA.addEntity(anchor, input);

            System.out.println("anchor " + anchor + " -> tier " + tier);
        }
        justRebuilt = true;
    }

    @Override
    public OrbitJob[] buildJobs(float dt) {
        OrbitJob[] jobs = new OrbitJob[tiers.length * 2];
        int jobCount = 0;

        // scheduled updates
        for(OrbitTier tier : tiers) {
            int count = tier.orbitSoA.getCount();
            if(count <= 0) continue;
            if(tier.updatesPerSecond == 0) continue;

            int start, end;

            // allow per frame updates
            if(tier.updatesPerSecond < 0) {
                start = 0;
                end = count;
            } else {
                tier.accumulator += tier.updatesPerSecond * dt;

                int entitiesToUpdate = (int) (tier.accumulator * count);
                if(entitiesToUpdate <= 0) continue;

                tier.accumulator -= (double) entitiesToUpdate / count;

                start = tier.cursor;
                end = start + entitiesToUpdate;
            }

            if(end <= count) {
                jobs[jobCount++] = new OrbitJob(tier.orbitSoA, tier.kernel, start, end);
            } else {
                jobs[jobCount++] = new OrbitJob(tier.orbitSoA, tier.kernel, start, count);
                jobs[jobCount++] = new OrbitJob(tier.orbitSoA, tier.kernel, 0, end % count);
            }

            tier.cursor = end % count;
        }

        return Arrays.copyOf(jobs, jobCount);
    }

    private final WorldPos posA = new WorldPos();
    private final WorldPos posB = new WorldPos();
    private final WorldDelta delta = new WorldDelta();

    private int computeTier(int anchor, OrbitSolveInput input) {
        WorldTransformData transform = GameContext.get().getWorldManager().getActiveWorld().getWorldTransform();
        FloatingOriginCamera camera = GameContext.get().getWorldManager().getActiveWorld().getCamera();

        int track = input.orbitGraph().getEntityToAnchor()[EntityRenderer.TRACK];

        posA.setFromArray(transform.getAnchorSX(), transform.getAnchorLX(), transform.getAnchorSY(), transform.getAnchorLY(), anchor);
        posB.set(camera.getOrigin());
        SplitCoordMath.addInPlace(posB, transform.getAnchorSX()[track], transform.getAnchorLX()[track], transform.getAnchorSY()[track], transform.getAnchorLY()[track]);
        WorldDelta.delta(delta, posA, posB);

        double zoom = camera.getZoom();
        double invZoom = 1.0 / zoom;
        double sx = delta.toDoubleX() * invZoom;
        double sy = delta.toDoubleY() * invZoom;

        double dist2 = sx*sx + sy*sy;

        double screenWidth = camera.getViewportWidth();
        double screenHeight = camera.getViewportHeight();
        double ref = Math.max(screenHeight, screenWidth);

        double nearRadius = ref * 0.6;
        double midRadius = ref * 1.8;

        double near2 = nearRadius * nearRadius;
        double mid2 = midRadius * midRadius;

        // hysteresis (in screen space)
        int current = currentTier[anchor];
        double nearH = (current == 0) ? 1.3 : 1.0;
        double midH  = (current == 0) ? 0.8 : 1.0;

        if(zoom > 3e6) {
            if(dist2 < mid2) return 1;
            return 2;
        }

        if(dist2 < near2 * nearH) return 0;
        if(dist2 < mid2 * midH) return 1;
        return 2;
    }

    @Override
    public void validate() {
        for (int t = 0; t < tiers.length; t++) {
            OrbitDataSoA soa = tiers[t].orbitSoA;

            for (int i = 0; i < soa.getCount(); i++) {
                int anchor = soa.anchorIds[i];

                if (currentTier[anchor] != t) {
                    throw new IllegalStateException("OrbitTier desync! " + currentTier[anchor] + " != " + t);
                }
            }
        }
    }

}
