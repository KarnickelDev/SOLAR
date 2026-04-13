package karnickeldev.solar.network.net.handlers;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.packets.ChatMessagePacket;
import karnickeldev.solar.ui.layers.hud.HudLayer;
import karnickeldev.solar.ui.layers.hud.chat.ChatMessage;
import karnickeldev.solar.ui.layers.hud.chat.ChatMessageBuilder;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class ClientChatMessageHandler implements PacketHandler<ChatMessagePacket> {

    @Override
    public void handle(int clientId, ChatMessagePacket packet) {
        if(!GameContext.isSet()) {
            Logger.get(LogTag.NETWORK).warn("Tried to handle ChatMessagePacket with no game context set");
            return;
        }

        HudLayer.INSTANCE.renderer.addMessage(new ChatMessageBuilder(packet.getSender(), packet.getText()).build());
        Logger.get(LogTag.NETWORK).debug("Client received ChatMessagePacket");
    }

    @Override
    public Class<ChatMessagePacket> getPacketClass() {
        return ChatMessagePacket.class;
    }
}
