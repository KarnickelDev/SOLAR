package karnickeldev.solar.network.packets;

/**
 * @author KarnickelDev
 * @since 28.06.2025
 **/
public abstract class GameStatePacket extends Packet {

    protected GameStatePacket(short type, short sequenceId) {
        super(type, sequenceId);
    }

    public abstract long getSimTimeMicros();

}
