package karnickeldev.solar.network.packets;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author : KarnickelDev
 * @since : 28.06.2025
 **/
public class HandshakePacket extends Packet {

    private String playerName;
    private String password;

    public HandshakePacket(String playerName, String password) {
        super(PacketTypes.HANDSHAKE.getType(), (short)0);

        this.playerName = playerName;
        this.password = password;
    }


    public String getPlayerName() {
        return playerName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean isFastHandled() {
        return true;
    }

    @Override
    protected void writeBody(ByteBuf out) {
        out.writeInt(playerName.length());
        out.writeCharSequence(playerName, StandardCharsets.UTF_8);

        out.writeInt(password.length());
        out.writeCharSequence(password, StandardCharsets.UTF_8);
    }

    @Override
    protected void readBody(ByteBuf in) {
        int length = in.readInt();
        playerName = in.readString(length, StandardCharsets.UTF_8);

        length = in.readInt();
        password = in.readString(length, StandardCharsets.UTF_8);
    }


    public static HandshakePacket create(ByteBuf in) {
        HandshakePacket pkt = new HandshakePacket("", "");
        pkt.readBody(in);
        return pkt;
    }

}
