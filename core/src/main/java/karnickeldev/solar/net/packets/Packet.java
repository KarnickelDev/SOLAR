package karnickeldev.solar.net.packets;

import karnickeldev.solar.net.network.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Packet extends Serializable<Packet> {

    short getType();

    long getCreationTick();

    void serialize(DataOutputStream out) throws IOException;

    Packet deserialize(DataInputStream in) throws IOException;

}
