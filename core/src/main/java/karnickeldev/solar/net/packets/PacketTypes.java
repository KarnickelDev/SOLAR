package karnickeldev.solar.net.packets;

import karnickeldev.solar.net.network.core.Deserializer;

public enum PacketTypes {

    PING(PingPacket.class, PingPacket::deserialize),
    PONG(PingPongPacket.class, PingPongPacket::deserialize),
    ENTITY_LIFECYCLE(EntityLifecyclePacket.class, EntityLifecyclePacket::deserialize),
    WORLD_UPDATE(WorldUpdatePacket.class, WorldUpdatePacket::deserialize),
    ECS_UPDATE(ECSUpdatePacket.class, ECSUpdatePacket::deserialize),
    SERVER_PERFORMANCE_METRICS(ServerPerformanceMetricsPacket.class, ServerPerformanceMetricsPacket::deserialize),
    FULL_SNAPSHOT_REQUEST(FullSnapshotRequestPacket.class, FullSnapshotRequestPacket::deserialize),
    FULL_SNAPSHOT(FullSnapshotPacket.class, FullSnapshotPacket::deserialize),
    ;

    private final short type;
    private final Class<? extends Packet> packetClass;
    private final Deserializer<? extends Packet> deserializer;
    PacketTypes(Class<? extends Packet> packetClass, Deserializer<? extends Packet> deserializer) {
        this.type = (short) this.ordinal();
        this.packetClass = packetClass;
        this.deserializer = deserializer;
    }

    public short getType() {
        return this.type;
    }

    public Deserializer<? extends Packet> getDeserializer() {
        return deserializer;
    }

    public Class<? extends Packet> getPacketClass() {
        return this.packetClass;
    }

    public static void registerAll() {
        for(PacketTypes type: PacketTypes.values()) {
            PacketRegistry.register(type.getType(), type.getDeserializer());
        }
    }
}
