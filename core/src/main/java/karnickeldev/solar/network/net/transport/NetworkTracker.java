package karnickeldev.solar.network.net.transport;

import io.netty.channel.ChannelId;

/**
 * @author KarnickelDev
 * @since 08.10.2025
 **/
public interface NetworkTracker {

    void onPacketRead(ChannelId clientId, short packetType, int bytes);

    void onPacketWrite(ChannelId clientId, short packetType, int bytes);

}
