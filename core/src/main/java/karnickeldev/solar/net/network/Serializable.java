package karnickeldev.solar.net.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable<T> {

    void serialize(DataOutputStream out) throws IOException;

    T deserialize(DataInputStream in) throws IOException;

}
