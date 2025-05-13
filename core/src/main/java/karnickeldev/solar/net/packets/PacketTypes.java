package karnickeldev.solar.net.packets;

public enum PacketTypes {

    ENTITY_LIFECYCLE(0),
    ECS_UPDATE(1),
    SERVER_PERFORMANCE_METRICS(2),
    ;

    private final short type;

    PacketTypes(int type) {
        this.type = (short) type;
    }

    public short getType() {
        return type;
    }
}
