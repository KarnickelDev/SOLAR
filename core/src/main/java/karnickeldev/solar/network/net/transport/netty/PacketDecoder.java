package karnickeldev.solar.network.net.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import karnickeldev.solar.network.net.transport.NetworkTracker;
import karnickeldev.solar.network.packets.Packet;

import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 24.06.2025
 **/
public class PacketDecoder extends ByteToMessageDecoder {

    private final NetworkTracker networkTracker;

    public PacketDecoder() {
        this(null);
    }

    public PacketDecoder(NetworkTracker networkTracker) {
        this.networkTracker = networkTracker;
    }

    public NetworkTracker getNetworkTracker() {
        return networkTracker;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) return; // minimum size: type (2) + sequenceId (2)

        // Use mark/reset to ensure full packet is available before decoding
        in.markReaderIndex();

        int before = in.readerIndex();

        try {
            Packet packet = Packet.read(in);
            out.add(packet);

            int bytesUsed = in.readerIndex() - before;

            if(networkTracker != null) networkTracker.onPacketRead(ctx.channel().id(), packet.getType(), bytesUsed);
        } catch (IndexOutOfBoundsException e) {
            in.resetReaderIndex(); // Wait for more data
        }
    }
}
