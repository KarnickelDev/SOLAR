package karnickeldev.solar.worldview.orbitsolver;

import karnickeldev.solar.worldview.orbitgraph.OrbitGraphData;
import karnickeldev.solar.worldview.orbitsolver.mathkernel.OrbitDataSoA;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 25.03.2026
 **/
public class OrbitPrecisionManager {

    int[] baseTier;
    int[] currentTier;

    private OrbitDataSoA[] tiers;

    public OrbitPrecisionManager(int capacityPerTier) {
        baseTier = new int[capacityPerTier];
        currentTier = new int[capacityPerTier];
        Arrays.fill(currentTier, -1);

        tiers = new OrbitDataSoA[3];

        for(int i = 0; i < tiers.length; i++) {
            tiers[i] = new OrbitDataSoA(capacityPerTier);
        }
    }

    public OrbitDataSoA[] getTiers() {
        return tiers;
    }

    public void updateTier(int entity, int newTier, OrbitSolveInput input) {
        int oldTier = currentTier[entity];
        if(oldTier == newTier) return;

        if(oldTier >= 0) {
            tiers[oldTier].removeEntity(entity);
        }

        tiers[newTier].addEntity(entity, input);
        currentTier[entity] = newTier;
    }

    public void updateContext(OrbitSolveInput input) {
        OrbitGraphData graph = input.orbitGraph();
        int count = input.usedCapacity();

        for(int i = 0; i < count; i++) {
            int anchor = graph.getDfsOrder()[i];

            int newTier = computeTier(anchor, input);
            updateTier(anchor, newTier, input);
        }
    }

    private int computeTier(int anchor, OrbitSolveInput input) {
        // TEMP: simple split
        return (anchor <= 0.1 * input.usedCapacity()) ? 0 : 1;
    }

}
