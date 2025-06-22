package karnickeldev.solar.net.network.core;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {

    void serialize(DataOutputStream out) throws IOException;

}
