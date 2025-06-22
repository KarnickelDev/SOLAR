package karnickeldev.solar.net.packets;

import karnickeldev.solar.ecs.registries.SnapshotRegistry;
import karnickeldev.solar.ecs.components.ComponentSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ECSUpdatePacket implements Packet {

    private final ComponentSnapshot[] snapshots;
    private final long simTimeMicros;
    private final int worldId;
    public final float simSpeed;

    protected ECSUpdatePacket(int worldId, long simTimeMicros, float simSpeed, ComponentSnapshot[] snapshots) {
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.simSpeed = simSpeed;
        this.snapshots = Arrays.copyOf(snapshots, snapshots.length);
    }

    @Override
    public short getType() {
        return PacketTypes.ECS_UPDATE.getType();
    }

    @Override
    public int getWorldId() {
        return worldId;
    }

    @Override
    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    @Override
    public int getSequenceId() {
        return 0;
    }

    public ComponentSnapshot[] getSnapshots() {
        return snapshots;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(worldId);
        out.writeLong(simTimeMicros);
        out.writeInt(snapshots.length);
        out.writeFloat(simSpeed);
        for (int i = 0; i < snapshots.length; i++) {
            ComponentSnapshot snap = snapshots[i];
            int type = SnapshotRegistry.getTypeId(snap.getClass());
            out.writeInt(type);
            snap.serialize(out);
        }
    }

    public static Packet deserialize(DataInputStream in) throws IOException {
        int worldId = in.readInt();
        long simTime = in.readLong();
        int count = in.readInt();
        float simSpeed = in.readFloat();
        ComponentSnapshot[] snaps = new ComponentSnapshot[count];
        for (int i = 0; i < count; i++) {
            int type = in.readInt();
            snaps[i] = SnapshotRegistry.deserialize(type, in);
        }

        return new ECSUpdatePacket(worldId, simTime, simSpeed, snaps);
    }
}
