package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

/**
 * @author : KarnickelDev
 * @since : 24.06.2025
 **/
public class NoHandler implements PacketHandler<Packet> {

    private static final NoHandler instance = new NoHandler();
    public static NoHandler getInstance() {
        return instance;
    }

    private NoHandler() {}

    @Override
    public void handle(int clientId, Packet packet) {
        Logger.error(Logger.NETWORK, "No Handler defined for packet type: " + packet.getType());
    }

    @Override
    public Class<Packet> getPacketClass() {
        return Packet.class;
    }
}
