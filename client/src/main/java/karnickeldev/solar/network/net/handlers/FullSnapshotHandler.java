package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.network.packets.FullSnapshotPacket;
import karnickeldev.solar.util.Logger;

/**
 * @author : KarnickelDev
 * @since : 03.07.2025
 **/
public class FullSnapshotHandler implements PacketHandler<FullSnapshotPacket> {
    @Override
    public void handle(int clientId, FullSnapshotPacket packet) {
        Logger.log("Resyncing with server...");
        for(int i = 0; i < packet.packets.length; i++) {
            HandlerRegistry.getHandler(packet.packets[i]).handle(0, packet.packets[i]);
        }
        Logger.log("Resynced with server");
    }

    @Override
    public Class<FullSnapshotPacket> getPacketClass() {
        return FullSnapshotPacket.class;
    }
}
