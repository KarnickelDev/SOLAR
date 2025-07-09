package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class WorldUpdatePacket extends GameStatePacket {

    private int worldId;
    private long simTimeMicros;

    protected WorldUpdatePacket(int worldId, long simTimeMicros) {
        super(PacketTypes.WORLD_UPDATE.getType(), (short)0);
        this.worldId = worldId;
        this.simTimeMicros = simTimeMicros;
    }

    public int getWorldId() {
        return worldId;
    }

    public long getSimTimeMicros() {
        return simTimeMicros;
    }

    @Override
    public void writeBody(ByteBuf out) {
        out.writeInt(worldId);
        out.writeLong(simTimeMicros);
    }

    @Override
    public void readBody(ByteBuf in) {
        worldId = in.readInt();
        simTimeMicros = in.readLong();
    }

    public static WorldUpdatePacket create(ByteBuf in) {
        WorldUpdatePacket pkt = new WorldUpdatePacket(0,0);
        pkt.readBody(in);
        return pkt;
    }
}
