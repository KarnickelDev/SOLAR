package karnickeldev.solar.util.threadlayout;

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

    private final short logicalCores;
    private final short physicalCores;
    private final short smtLevel;
    private final List<List<Integer>> siblings; // physical core -> list of logical cores

    public CpuLayout() {
        this.logicalCores = (short) Runtime.getRuntime().availableProcessors();

        // naive but surprisingly effective SMT grouping:
        // assume each physical core has N siblings where N = logical / physical.
        // try 2 first (common: HT), fallback if not divisible.
        this.smtLevel = (short) guessSMTLevel(logicalCores);

        this.physicalCores = (short) (logicalCores / smtLevel);
        this.siblings = buildSiblings(physicalCores, smtLevel);
    }

    private int guessSMTLevel(int logical) {
        if (logical % 2 == 0) return 2;
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

    public short getLogicalCores() {
        return logicalCores;
    }

    public short getPhysicalCores() {
        return physicalCores;
    }

    public short getSmtLevel() {
        return smtLevel;
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
