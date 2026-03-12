package karnickeldev.solar.worldview.transform;

import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;
import karnickeldev.solar.worldview.orbitsolver.OrbitLocalFrame;

/**
 * @author KarnickelDev
 * @since 24.02.2026
 **/
public class WorldTransformSystem {

    public static void transformToGlobalPos(OrbitGraphData orbitGraph, OrbitLocalFrame orbitLocal, WorldTransformData out) {
        out.anchorCount = orbitGraph.getAnchorCount();

        // resolve absolute world pos
        for (int i = 0; i < orbitGraph.getAnchorCount(); i++) {
            int anchor = orbitGraph.getDfsOrder()[i];

            // local orbit position (relative to parent)
            out.anchorSX[anchor] = orbitLocal.sectorX[anchor];
            out.anchorLX[anchor] = orbitLocal.localX[anchor];
            out.anchorSY[anchor] = orbitLocal.sectorY[anchor];
            out.anchorLY[anchor] = orbitLocal.localY[anchor];

            int parentAnchor = orbitGraph.getParent()[anchor];
            if (parentAnchor != -1) {
                out.anchorSX[anchor] += out.anchorSX[parentAnchor];
                out.anchorLX[anchor] += out.anchorLX[parentAnchor];
                out.anchorSY[anchor] += out.anchorSY[parentAnchor];
                out.anchorLY[anchor] += out.anchorLY[parentAnchor];
            }
        }

    }

}
