package karnickeldev.solar.net.packets;

import karnickeldev.solar.net.network.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet extends Serializable<Packet> {

    int MAX_SEQUENCE = 0xFFFF;

    short getType();

    int getWorldId();

    long getSimTimeMicros();

    int getSequenceId();

    default boolean isFastHandled() {
        return false;
    }

    void serialize(DataOutputStream out) throws IOException;

    Packet deserialize(DataInputStream in) throws IOException;

}
