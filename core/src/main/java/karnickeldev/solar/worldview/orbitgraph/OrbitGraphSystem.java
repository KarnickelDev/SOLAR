package karnickeldev.solar.worldview.orbitgraph;

import karnickeldev.solar.ecs.ECSContext;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;

/**
 * @author KarnickelDev
 * @since 22.02.2026
 **/
public final class OrbitGraphSystem {

    private final OrbitGraphData graph = new OrbitGraphData(EntityManager.MAX_ENTITIES);

    private final int[] tmpParentEntity = new int[EntityManager.MAX_ENTITIES];

    private int anchorCount;

    private boolean rebuildNecessary = false;
    private boolean wasRebuild = false;

    public OrbitGraphData getOrbitGraph() {
        return graph;
    }

    public void notifyChange() {
        rebuildNecessary = true;
        Logger.get("OrbitGraph").debug("notifyChange()");
    }

    public boolean wasRebuildThisFrame() {
        return wasRebuild;
    }

    public void rebuildIfNecessary(ECSContext ecs) {
        wasRebuild = false;
        if(rebuildNecessary) {
            rebuildNecessary = false;
            rebuild(ecs);
        }
    }

    public void rebuild(ECSContext ecs) {
        collectAnchors(ecs);
        collectParents(ecs);

        OrbitGraphBuilder.build(anchorCount, graph.anchorToEntity, graph.entityToAnchor, tmpParentEntity, graph);
        wasRebuild = true;
        Logger.get("OrbitGraph").debug("rebuild()");
    }

    public void collectAnchors(ECSContext ecs) {
        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);
        anchorCount = 0;

        for(int ent = orbitData.hasComponent.nextSetBit(0);
            ent >= 0;
            ent = orbitData.hasComponent.nextSetBit(ent + 1)) {

            graph.anchorToEntity[anchorCount] = ent;
            graph.entityToAnchor[ent] = anchorCount;
            anchorCount++;
        }
    }

    public void collectParents(ECSContext ecs) {
        OrbitDataComponent orbitData = ecs.getComponentRegistry().get(OrbitDataComponent.class);

        for (int a = 0; a < anchorCount; a++) {
            int ent = graph.anchorToEntity[a];
            int p = orbitData.getCentralBody(ent);

            tmpParentEntity[a] = p == EntityManager.NO_ENTITY ? -1 : p;
        }
    }

}
