package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;
import karnickeldev.solar.ecs.registries.SnapshotRegistry;
import karnickeldev.solar.ecs.components.ComponentSnapshot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ECSUpdatePacket extends GameStatePacket {

    private ComponentSnapshot[] snapshots;
    private long simTimeMicros;
    private int worldId;
    private float simSpeed;

    private float simSpeedChange;
    private long changeActivationTime;

    protected ECSUpdatePacket(int worldId, long simTimeMicros, float simSpeed, ComponentSnapshot[] snapshots) {
        this(worldId, simTimeMicros, simSpeed, -1, -1, snapshots);
    }

    protected ECSUpdatePacket(int worldId, long simTimeMicros, float simSpeed, float nextSimSpeed, long activationTime, ComponentSnapshot[] snapshots) {
        super(PacketTypes.ECS_UPDATE.getType(), (short)0);
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
        this.simSpeed = simSpeed;
        this.snapshots = Arrays.copyOf(snapshots, snapshots.length);

        this.simSpeedChange = nextSimSpeed;
        this.changeActivationTime = activationTime;
    }


    public int getWorldId() {
        return worldId;
    }

    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    public float getSimSpeed() {
        return simSpeed;
    }

    public float getSimSpeedChange() {
        return simSpeedChange;
    }

    public long getChangeActivationTime() {
        return changeActivationTime;
    }

    public ComponentSnapshot[] getSnapshots() {
        return snapshots;
    }

    @Override
    public void writeBody(ByteBuf out) {
        out.writeInt(worldId);
        out.writeLong(simTimeMicros);
        out.writeFloat(simSpeed);
        out.writeFloat(simSpeedChange);
        out.writeLong(changeActivationTime);
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
        simSpeed = in.readFloat();
        simSpeedChange = in.readFloat();
        changeActivationTime = in.readLong();
        int length = in.readInt();
        snapshots = new ComponentSnapshot[length];
        for (int i = 0; i < length; i++) {
            short type = in.readShort();
            snapshots[i] = SnapshotRegistry.deserialize(type, in);
        }
    }

    public static ECSUpdatePacket create(ByteBuf in) {
        ECSUpdatePacket p = new ECSUpdatePacket(0,0,0,new ComponentSnapshot[0]);
        p.readBody(in);
        return p;
    }
}
