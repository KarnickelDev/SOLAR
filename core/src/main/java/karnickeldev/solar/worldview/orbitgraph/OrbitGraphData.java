package karnickeldev.solar.worldview.orbitgraph;

/**
 * @author KarnickelDev
 * @since 24.02.2026
 **/
public final class OrbitGraphData {

    int anchorCount;

    // mapping
    final int[] anchorToEntity;
    final int[] entityToAnchor;

    // hierarchy
    final int[] parent;
    final int[] firstChild;
    final int[] nextSibling;

    // traversal
    final int[] dfsOrder;

    // subtree ranges
    final int[] subtreeStart;
    final int[] subtreeSize;

    public OrbitGraphData(int capacity) {
        anchorToEntity = new int[capacity];
        entityToAnchor = new int[capacity];

        parent = new int[capacity];
        firstChild = new int[capacity];
        nextSibling = new int[capacity];

        dfsOrder = new int[capacity];

        subtreeStart = new int[capacity];
        subtreeSize = new int[capacity];
    }

    public int getAnchorCount() {
        return anchorCount;
    }

    public int[] getAnchorToEntity() {
        return anchorToEntity;
    }

    public int[] getEntityToAnchor() {
        return entityToAnchor;
    }

    public int[] getParent() {
        return parent;
    }

    public int[] getDfsOrder() {
        return dfsOrder;
    }

    public int[] getFirstChild() {
        return firstChild;
    }

    public int[] getNextSibling() {
        return nextSibling;
    }

    public int[] getSubtreeSize() {
        return subtreeSize;
    }

    public int[] getSubtreeStart() {
        return subtreeStart;
    }
}
