package karnickeldev.solar.worldview.orbitgraph;

import java.util.Arrays;

/**
 * @author KarnickelDev
 * @since 24.02.2026
 **/
public final class OrbitGraphBuilder {

    public static void build(int anchorCount, int[] anchorToEntity, int[] entityToAnchor, int[] parents, OrbitGraphData out) {
        out.anchorCount = anchorCount;

        System.arraycopy(anchorToEntity, 0, out.anchorToEntity, 0, anchorCount);
        System.arraycopy(entityToAnchor, 0, out.entityToAnchor, 0, anchorCount);

        // build parent anchors
        for(int a = 0; a < anchorCount; a++) {
            int p = parents[a];
            out.parent[a] = p >= 0 ? out.entityToAnchor[p] : -1;
        }

        buildChildLists(out);
        buildDFS(out);
    }

    private static void buildChildLists(OrbitGraphData g) {
        Arrays.fill(g.firstChild, 0, g.anchorCount, -1);
        Arrays.fill(g.nextSibling, 0, g.anchorCount, -1);

        for(int a = 0; a < g.anchorCount; a++) {
            int p = g.parent[a];
            if(p == -1) continue;

            g.nextSibling[a] = g.firstChild[p];
            g.firstChild[p] = a;
        }
    }

    private static void buildDFS(OrbitGraphData g) {
        int[] dfsCursor = new int[1];

        for(int a = 0; a < g.anchorCount; a++) {
            if(g.parent[a] == -1) {
                dfs(g, a, dfsCursor);
            }
        }
    }

    private static void dfs(OrbitGraphData g, int a, int[] cursor) {
        int start = cursor[0];
        g.dfsOrder[cursor[0]++] = a;

        for(int c = g.firstChild[a]; c != -1; c = g.nextSibling[c]) {
            dfs(g, c, cursor);
        }

        g.subtreeStart[a] = start;
        g.subtreeSize[a] = cursor[0] - start;
    }
}
