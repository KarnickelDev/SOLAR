package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author KarnickelDev
 * @since 05.04.2026
 **/
public class ChatMessagePacket extends Packet{

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private String sender;
    private String text;

    public ChatMessagePacket(String sender, String text) {
        super(PacketTypes.CHAT_MESSAGE.getType(), (short)0);
        this.sender = sender;
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        byte[] senderBytes = sender.getBytes(CHARSET);
        out.writeShort(senderBytes.length);
        out.writeBytes(senderBytes);

        byte[] textBytes = text.getBytes(CHARSET);
        out.writeShort(textBytes.length);
        out.writeBytes(textBytes);
    }

    @Override
    protected void readBody(ByteBuf in) {
        short senderLength = in.readShort();
        byte[] senderBytes = new byte[senderLength];
        in.readBytes(senderBytes);
        sender = new String(senderBytes, CHARSET);

        short textLength = in.readShort();
        byte[] textBytes = new byte[textLength];
        in.readBytes(textBytes);
        text = new String(textBytes, CHARSET);
    }

    public static ChatMessagePacket create(ByteBuf in) {
        ChatMessagePacket pkt = new ChatMessagePacket("", "");
        pkt.readBody(in);
        return pkt;
    }
}
