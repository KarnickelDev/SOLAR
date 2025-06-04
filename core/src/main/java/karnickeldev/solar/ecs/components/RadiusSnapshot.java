package karnickeldev.solar.ecs.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RadiusSnapshot implements ComponentSnapshot {

    public final long tick;
    public final int[] entities;
    public final float[] radius;
    private final int size;
    private int count = 0;

    public RadiusSnapshot(int size, long tick) {
        this.size = size;
        this.tick = tick;

        entities = new int[size];
        radius = new float[size];
    }

    @Override
    public int getChangedCount() {
        return count;
    }

    public void addChange(int entity, float radius) {
        if (count >= size) throw new RuntimeException("RadiusComponent too large");
        this.entities[count] = entity;
        this.radius[count] = radius;

        count++;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {

    }

    @Override
    public ComponentSnapshot deserialize(DataInputStream in) throws IOException {
        return null;
    }
}
