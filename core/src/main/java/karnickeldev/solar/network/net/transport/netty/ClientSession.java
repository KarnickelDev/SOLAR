package karnickeldev.solar.network.net.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

/**
 * @author KarnickelDev
 * @since 26.06.2025
 **/
public class ClientSession {

    private final int clientId;
    public final Channel channel;
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
            //Logger.log("bytes: " + channel.unsafe().outboundBuffer().totalPendingWriteBytes());
            if(channel.isActive()) {
                channel.writeAndFlush(packet).addListener((ChannelFuture future) -> {
                    if (future.isSuccess()) {
                        Logger.debug(Logger.NETWORK, "writeAndFlush SUCCESS for packet " + packet);
                    } else {
                        Logger.error(Logger.NETWORK, "writeAndFlush FAILED for packet " + packet, future.cause());
                    }
                });
            } else {
                Logger.error(Logger.NETWORK, "Packet dropped (channel not active): " + packet);
            }
        });
    }

    public void close() {
        channel.close();
    }

    public boolean isActive() {
        return channel.isActive();
    }
}
