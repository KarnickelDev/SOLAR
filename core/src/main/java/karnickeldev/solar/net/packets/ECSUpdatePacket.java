package karnickeldev.solar.net.packets;

import karnickeldev.solar.ecs.components.ComponentSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ECSUpdatePacket implements Packet {

    private final ComponentSnapshot[] snapshots;
    public final long tick;
    public final int worldId;

    protected ECSUpdatePacket(int worldId, long tick, ComponentSnapshot[] snapshots) {
        this.worldId = worldId;
        this.tick = tick;
        this.snapshots = Arrays.copyOf(snapshots, snapshots.length);
    }

    @Override
    public short getType() {
        return PacketTypes.ECS_UPDATE.getType();
    }

    public long getCreationTick() {
        return tick;
    }

    public ComponentSnapshot[] getSnapshots() {
        return snapshots;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(worldId);
        out.writeLong(tick);
        out.writeInt(snapshots.length);
        for (int i = 0; i < snapshots.length; i++) {
            ComponentSnapshot snap = snapshots[i];
            int type = ComponentSnapshotRegistry.getTypeId(snap.getClass());
            out.writeInt(type);
            snap.serialize(out);
        }
    }

    @Override
    public Packet deserialize(DataInputStream in) throws IOException {
        int worldId = in.readInt();
        long tick = in.readLong();
        int count = in.readInt();
        ComponentSnapshot[] snaps = new ComponentSnapshot[count];
        for (int i = 0; i < count; i++) {
            int type = in.readInt();
            snaps[i] = ComponentSnapshotRegistry.deserialize(type, in);
        }

        return new ECSUpdatePacket(worldId, tick, snaps);
    }
}
