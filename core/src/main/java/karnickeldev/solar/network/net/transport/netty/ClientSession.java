package karnickeldev.solar.network.net.transport.netty;

import io.netty.channel.Channel;
import karnickeldev.solar.network.packets.Packet;

/**
 * @author : KarnickelDev
 * @since : 26.06.2025
 **/
public class ClientSession {

    private final int clientId;
    private final Channel channel;
    private boolean handshake = false;

    public ClientSession(int clientId, Channel channel) {
        this.clientId = clientId;
        this.channel = channel;
    }

    public int getClientId() {
        return clientId;
    }

    public boolean isHandshake() {
        return handshake;
    }

    public void completeHandshake() {
        handshake = true;
    }

    public void send(Packet packet) {
        channel.eventLoop().execute(() -> {
            if(channel.isActive()) channel.writeAndFlush(packet);
        });
    }

    public void close() {
        channel.close();
    }

    public boolean isActive() {
        return channel.isActive() && handshake;
    }
}
