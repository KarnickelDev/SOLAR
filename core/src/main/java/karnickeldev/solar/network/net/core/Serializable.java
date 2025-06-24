package karnickeldev.solar.network.net.core;

import io.netty.buffer.ByteBuf;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {

    void serialize(ByteBuf out);

}
