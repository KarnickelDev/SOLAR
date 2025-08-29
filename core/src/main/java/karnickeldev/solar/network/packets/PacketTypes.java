package karnickeldev.solar.network.packets;

import karnickeldev.solar.network.net.handlers.*;

public enum PacketTypes {

    PING(PingPacket.class, PingPacket::create, new PingHandler()),
    PONG(PingPongPacket.class, PingPongPacket::create),
    HANDSHAKE(HandshakePacket.class, HandshakePacket::create),
    HANDSHAKE_RESPONSE(HandshakeResponsePacket.class, HandshakeResponsePacket::create),
    DISCONNECT(DisconnectPacket.class, DisconnectPacket::create),
    ENTITY_LIFECYCLE(EntityLifecyclePacket.class, EntityLifecyclePacket::create),
    WORLD_UPDATE(WorldUpdatePacket.class, WorldUpdatePacket::create),
    ECS_UPDATE(ECSUpdatePacket.class, ECSUpdatePacket::create),
    SERVER_PERFORMANCE_METRICS(ServerPerformanceMetricsPacket.class, ServerPerformanceMetricsPacket::create),
    FULL_SNAPSHOT_REQUEST(FullSnapshotRequestPacket.class, FullSnapshotRequestPacket::create, new FullSnapshotRequestHandler()),
    FULL_SNAPSHOT(FullSnapshotPacket.class, FullSnapshotPacket::create),
    TEST_CAM(TestCamPacket.class, TestCamPacket::create, new TestCamHandler()),
    SIM_TIME_UPDATE_REQUEST(SimTimeUpdateRequestPacket.class, SimTimeUpdateRequestPacket::create, new SimTimeUpdateRequestHandler()),
    ;

    private final short type;
    private final Class<? extends Packet> packetClass;
    private final PacketRegistry.PacketFactory factory;
    private PacketHandler<? extends Packet> handler;
    PacketTypes(Class<? extends Packet> packetClass, PacketRegistry.PacketFactory factory, PacketHandler<? extends Packet> handler) {
        this.type = (short) this.ordinal();
        this.packetClass = packetClass;
        this.factory = factory;
        this.handler = handler;
    }
    PacketTypes(Class<? extends Packet> packetClass, PacketRegistry.PacketFactory factory) {
        this(packetClass, factory, NoHandler.getInstance());
    }

    public short getType() {
        return this.type;
    }

    public PacketRegistry.PacketFactory getFactory() {
        return factory;
    }

    public PacketHandler<? extends Packet> getHandler() {
        return handler;
    }

    public Class<? extends Packet> getPacketClass() {
        return this.packetClass;
    }

    public void registerHandler(PacketHandler<? extends Packet> handler) {
        this.handler = handler;
        HandlerRegistry.registerHandler(type, handler);
    }

    public void register() {
        if(factory != null) PacketRegistry.register(type, factory);
        if(handler != null) HandlerRegistry.registerHandler(type, handler);
    }

    private static boolean init = false;

    public static void registerCommon() {
        if(init) return;
        for(PacketTypes type: PacketTypes.values()) {
            type.register();
        }
        init = true;
    }
}
