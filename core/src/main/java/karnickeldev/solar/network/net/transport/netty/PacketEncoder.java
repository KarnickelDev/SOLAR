package karnickeldev.solar.network.net.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import karnickeldev.solar.logging.LogTag;
import karnickeldev.solar.logging.Logger;
import karnickeldev.solar.network.net.transport.NetworkTracker;
import karnickeldev.solar.network.packets.Packet;

/**
 * @author KarnickelDev
 * @since 24.06.2025
 **/
public class PacketEncoder extends MessageToByteEncoder<Packet> {

    private final NetworkTracker networkTracker;

    public PacketEncoder() {
        this(null);
    }

    public PacketEncoder(NetworkTracker networkTracker) {
        this.networkTracker = networkTracker;
    }

    public NetworkTracker getNetworkTracker() {
        return networkTracker;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf out) throws Exception {
        if(packet == null) {
            Logger.get(LogTag.NETWORK).error("Tried to send null packet");
        } else {
            int before = out.writerIndex();

            packet.write(out);

            int written = out.writerIndex() - before;

            if(networkTracker != null) networkTracker.onPacketWrite(channelHandlerContext.channel().id(), packet.getType(), written);
        }
    }
}
