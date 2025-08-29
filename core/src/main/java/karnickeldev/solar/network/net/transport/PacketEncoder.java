package karnickeldev.solar.network.net.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

/**
 * @author : KarnickelDev
 * @since : 24.06.2025
 **/
public class PacketEncoder extends MessageToByteEncoder<Packet> {


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {
        if(packet == null) {
            Logger.error(Logger.NETWORK, "Tried to send null packet");
        } else {
            packet.write(byteBuf);
        }
    }
}
