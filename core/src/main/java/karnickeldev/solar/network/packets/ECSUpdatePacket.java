package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.ecs.registries.SnapshotRegistry;
import karnickeldev.solar.ecs.components.ComponentSnapshot;

import java.util.Arrays;

public class ECSUpdatePacket extends GameStatePacket {

    private ComponentSnapshot[] snapshots;
    private long simTimeMicros;
    private int worldId;

    private float currentSimSpeed;
    public byte targetSimSpeedIndex;

    protected ECSUpdatePacket(int worldId, long simTimeMicros, float currentSimSpeed, byte targetSimSpeedIndex, ComponentSnapshot[] snapshots) {
        super(PacketTypes.ECS_UPDATE.getType(), (short)0);
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.currentSimSpeed = currentSimSpeed;
        this.targetSimSpeedIndex = targetSimSpeedIndex;
        this.snapshots = Arrays.copyOf(snapshots, snapshots.length);
    }


    public int getWorldId() {
        return worldId;
    }

    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    public float getCurrentSimSpeed() {
        return currentSimSpeed;
    }

    public byte getTargetSimSpeedIndex() {
        return targetSimSpeedIndex;
    }

    public ComponentSnapshot[] getSnapshots() {
        return snapshots;
    }

    @Override
    public void writeBody(ByteBuf out) {
        out.writeInt(worldId);
        out.writeLong(simTimeMicros);
        out.writeFloat(currentSimSpeed);
        out.writeByte(targetSimSpeedIndex);
        out.writeInt(snapshots.length);
        for (ComponentSnapshot snap : snapshots) {
            short type = SnapshotRegistry.getTypeId(snap.getClass());
            out.writeShort(type);
            snap.serialize(out);
        }
    }

    @Override
    public void readBody(ByteBuf in) {
        worldId = in.readInt();
        simTimeMicros = in.readLong();
        currentSimSpeed = in.readFloat();
        targetSimSpeedIndex = in.readByte();
        int length = in.readInt();
        snapshots = new ComponentSnapshot[length];
        for (int i = 0; i < length; i++) {
            short type = in.readShort();
            snapshots[i] = SnapshotRegistry.deserialize(type, in);
        }
    }

    public static ECSUpdatePacket create(ByteBuf in) {
        ECSUpdatePacket p = new ECSUpdatePacket(0,0,0, (byte)0, new ComponentSnapshot[0]);
        p.readBody(in);
        return p;
    }
}
