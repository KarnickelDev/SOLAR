package karnickeldev.solar.network.net.core;

import io.netty.buffer.ByteBuf;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author KarnickelDev
 * @since 21.06.2025
 **/
@FunctionalInterface
public interface Deserializer<T> {
    T deserialize(ByteBuf in);
}
