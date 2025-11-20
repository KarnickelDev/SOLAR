package karnickeldev.solar.util.threadlyout;

import java.util.ArrayList;
import java.util.List;

/**
 * Tries best-guessing the available physical / logical CPU cores
 * and builds a list of cores and SMT-Siblings
 * @apiNote try in future: <a href="https://github.com/oshi/oshi?tab=readme-ov-file">oshi</a>
 * @author KarnickelDev
 * @since 22.11.2025
 **/
public final class CpuLayout {

    private final int logicalCores;
    private final int physicalCores;
    private final List<List<Integer>> siblings; // physical core -> list of logical cores

    public CpuLayout() {
        this.logicalCores = Runtime.getRuntime().availableProcessors();

        // naive but surprisingly effective SMT grouping:
        // assume each physical core has N siblings where N = logical / physical.
        // try 2 first (common: HT), fallback if not divisible.
        int smtLevel = guessSMTLevel(logicalCores);

        this.physicalCores = logicalCores / smtLevel;
        this.siblings = buildSiblings(physicalCores, smtLevel);
    }

    private int guessSMTLevel(int logical) {
        if (logical % 2 == 0) return 2;
        if (logical % 4 == 0) return 4;
        return 1; // fallback value
    }

    private List<List<Integer>> buildSiblings(int physical, int smt) {
        List<List<Integer>> list = new ArrayList<>(logicalCores);
        int id = 0;
        for (int i = 0; i < physical; i++) {
            List<Integer> sib = new ArrayList<>();
            for (int j = 0; j < smt; j++) {
                sib.add(id++);
            }
            list.add(sib);
        }
        return list;
    }

    public int getLogicalCores() {
        return logicalCores;
    }

    public int getPhysicalCores() {
        return physicalCores;
    }

    public List<Integer> logicalCoresFlat() {
        return siblings.stream().flatMap(List::stream).toList();
    }

    public List<Integer> physicalCorePrimaryIds() {
        return siblings.stream().map(List::getFirst).toList();
    }

    public List<Integer> logicalSiblingsOf(int physicalCoreIndex) {
        return siblings.get(physicalCoreIndex);
    }
}
