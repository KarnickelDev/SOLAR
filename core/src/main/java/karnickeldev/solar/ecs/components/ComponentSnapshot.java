package karnickeldev.solar.ecs.components;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ComponentSnapshot {

    ComponentSnapshot copy();

    int getChangedCount();

    void serialize(DataOutputStream out) throws IOException;
    ComponentSnapshot deserialize(DataInputStream in) throws IOException;

}
