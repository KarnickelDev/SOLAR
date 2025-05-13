package karnickeldev.solar.ecs.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TagSnapshot implements ComponentSnapshot {

    public final long tick;
    public final int[] entities;
    public final int[] tagMasks;
    private final int size;
    private int count = 0;

    public TagSnapshot(int size, long tick) {
        this.size = size;
        this.tick = tick;

        entities = new int[size];
        tagMasks = new int[size];
    }

    public void addChange(int entity, int tagMask) {
        if (count >= size) throw new RuntimeException("Snapshot too small");

        entities[count] = entity;
        tagMasks[count] = tagMask;

        count++;
    }

    @Override
    public int getChangedCount() {
        return count;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {

    }

    @Override
    public ComponentSnapshot deserialize(DataInputStream in) throws IOException {
        return null;
    }
}
