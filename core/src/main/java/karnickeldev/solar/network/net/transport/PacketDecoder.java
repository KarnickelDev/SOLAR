package karnickeldev.solar.network.net.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import karnickeldev.solar.network.packets.Packet;

import java.util.List;

/**
 * @author : KarnickelDev
 * @since : 24.06.2025
 **/
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) return; // minimum size: type (2) + sequenceId (2)

        // Use mark/reset to ensure full packet is available before decoding
        in.markReaderIndex();

        try {
            Packet packet = Packet.read(in);
            out.add(packet);
        } catch (IndexOutOfBoundsException e) {
            in.resetReaderIndex(); // Wait for more data
        }
    }
}
