package karnickeldev.solar.ecs.components;

import io.netty.buffer.ByteBuf;

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
    public void serialize(ByteBuf out) {
        out.writeLong(tick);
        out.writeInt(count);
        for (int i = 0; i < count; i++) {
            out.writeInt(entities[i]);
            out.writeInt(tagMasks[i]);
        }
    }

    public static TagSnapshot deserialize(ByteBuf in) {
        long tick = in.readLong();
        int count = in.readInt();

        TagSnapshot snap = new TagSnapshot(count, tick);

        for (int i = 0; i < count; i++) {
            snap.addChange(in.readInt(), in.readInt());
        }

        return snap;
    }
}
