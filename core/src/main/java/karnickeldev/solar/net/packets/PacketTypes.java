package karnickeldev.solar.net.packets;

public enum PacketTypes {

    PING(0),
    PONG(1),
    ENTITY_LIFECYCLE(2),
    WORLD_UPDATE(3),
    ECS_UPDATE(4),
    SERVER_PERFORMANCE_METRICS(5),
    ;

    private final short type;
    PacketTypes(int type) {
        this.type = (short) type;
    }

    public short getType() {
        return type;
    }
}
