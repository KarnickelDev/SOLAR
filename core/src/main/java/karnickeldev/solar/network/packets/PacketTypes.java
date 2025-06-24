package karnickeldev.solar.network.packets;

public enum PacketTypes {

    PING(PingPacket.class, PingPacket::create),
    PONG(PingPongPacket.class, PingPongPacket::create),
    ENTITY_LIFECYCLE(EntityLifecyclePacket.class, EntityLifecyclePacket::create),
    WORLD_UPDATE(WorldUpdatePacket.class, WorldUpdatePacket::create),
    ECS_UPDATE(ECSUpdatePacket.class, ECSUpdatePacket::create),
    SERVER_PERFORMANCE_METRICS(ServerPerformanceMetricsPacket.class, ServerPerformanceMetricsPacket::create),
    FULL_SNAPSHOT_REQUEST(FullSnapshotRequestPacket.class, FullSnapshotRequestPacket::create),
    FULL_SNAPSHOT(FullSnapshotPacket.class, FullSnapshotPacket::create),
    ;

    private final short type;
    private final Class<? extends Packet> packetClass;
    private final PacketRegistry.PacketFactory factory;
    PacketTypes(Class<? extends Packet> packetClass, PacketRegistry.PacketFactory factory) {
        this.type = (short) this.ordinal();
        this.packetClass = packetClass;
        this.factory = factory;
    }

    public short getType() {
        return this.type;
    }

    public PacketRegistry.PacketFactory getFactory() {
        return factory;
    }

    public Class<? extends Packet> getPacketClass() {
        return this.packetClass;
    }

    public void register() {
        if(factory != null) PacketRegistry.register(type, factory);
    }

    public static void registerAll() {
        for(PacketTypes type: PacketTypes.values()) {
            type.register();
        }
    }
}
