package karnickeldev.solar.net.packets;

public enum PacketTypes {

    ENTITY_LIFECYCLE(0),
    WORLD_UPDATE(1),
    ECS_UPDATE(2),
    SERVER_PERFORMANCE_METRICS(3),
    ;

    private final short type;
    PacketTypes(int type) {
        this.type = (short) type;
    }

    public short getType() {
        return type;
    }
}
