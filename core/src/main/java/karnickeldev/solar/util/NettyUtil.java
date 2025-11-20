package karnickeldev.solar.util;

import io.netty.buffer.ByteBuf;

/**
 * @author KarnickelDev
 * @since 12.10.2025
 **/
public class NettyUtil {


    public static void writeVarInt(ByteBuf out, int value) {
        while((value & 0xFFFFFF80) != 0L) {                // while > 7 bits remain
            out.writeByte((value & 0x7F) | 0x80);   // write low 7 bits with continuation flag
            value >>>= 7;                                 // logical 7 bit left shift
        }
        out.writeByte(value & 0x7F);
    }

    public static int readVarInt(ByteBuf in) {
        int numRead = 0;
        int result = 0;
        byte read;

        do {
            read = in.readByte();
            int value = (read & 0x7F);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) throw new RuntimeException("VarInt too big");
        } while ((read & 0x80) != 0);

        return result;
    }

}
