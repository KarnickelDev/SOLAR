package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.FullSnapshotPacket;

/**
 * @author KarnickelDev
 * @since 03.07.2025
 **/
public class FullSnapshotHandler implements PacketHandler<FullSnapshotPacket> {

    private final Logger logger;

    public FullSnapshotHandler() {
        logger = Logger.get(LogTag.ECS);
    }

    @Override
    public void handle(int clientId, FullSnapshotPacket packet) {
        logger.info("Resyncing with server...");
        for(int i = 0; i < packet.packets.length; i++) {
            HandlerRegistry.getHandler(packet.packets[i]).handle(0, packet.packets[i]);
        }
        logger.info("Resynced with server");
    }

    @Override
    public Class<FullSnapshotPacket> getPacketClass() {
        return FullSnapshotPacket.class;
    }
}
