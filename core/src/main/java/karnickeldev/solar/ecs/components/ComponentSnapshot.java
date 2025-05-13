package karnickeldev.solar.ecs.components;

import karnickeldev.solar.net.network.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ComponentSnapshot extends Serializable<ComponentSnapshot> {

    int getChangedCount();

    void serialize(DataOutputStream out) throws IOException;

    ComponentSnapshot deserialize(DataInputStream in) throws IOException;

}
