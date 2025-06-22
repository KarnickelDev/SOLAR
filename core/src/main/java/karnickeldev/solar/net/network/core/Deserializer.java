package karnickeldev.solar.net.network.core;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author : KarnickelDev
 * @since : 21.06.2025
 **/
@FunctionalInterface
public interface Deserializer<T> {
    T deserialize(DataInputStream in) throws IOException;
}
