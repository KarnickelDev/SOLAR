package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.ecs.components.MassComponent;
import karnickeldev.solar.ecs.components.OrbitDataComponent;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;

/**
 * @author KarnickelDev
 * @since 22.02.2026
 **/
public record OrbitSolveInput(
    long simTimeMicros,
    OrbitGraphData orbitGraph,
    OrbitDataComponent orbitData,
    MassComponent mass
) {
    public int usedCapacity() {
        return orbitGraph.getAnchorCount();
    }
}
