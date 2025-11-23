package karnickeldev.solar.network.net.core;

import io.netty.buffer.ByteBuf;

public interface Serializable {

    void serialize(ByteBuf out);

}
