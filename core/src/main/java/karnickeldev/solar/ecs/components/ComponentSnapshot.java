package karnickeldev.solar.ecs.components;

import karnickeldev.solar.net.network.core.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ComponentSnapshot extends Serializable {

    int getChangedCount();

}
