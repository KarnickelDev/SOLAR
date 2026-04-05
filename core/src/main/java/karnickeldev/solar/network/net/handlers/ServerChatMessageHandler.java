package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.ChatMessagePacket;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class ServerChatMessageHandler implements PacketHandler<ChatMessagePacket> {

    @Override
    public void handle(int clientId, ChatMessagePacket packet) {
        if(!ServerContext.isSet()) {
            Logger.get(LogTag.NETWORK).warn("Tried to handle ChatMessagePacket with no server context set");
            return;
        }

        ServerContext.get().getServer().getServerNetwork().broadcastExcept(clientId, packet);
        ServerContext.get().getServer().getServerNetwork().flush();
        Logger.get(LogTag.NETWORK).debug("Server received ChatMessagePacket");
    }

    @Override
    public Class<ChatMessagePacket> getPacketClass() {
        return ChatMessagePacket.class;
    }
}
