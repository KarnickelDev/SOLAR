package karnickeldev.solar.ecs.components;

import karnickeldev.solar.network.net.core.Serializable;

public interface ComponentSnapshot extends Serializable {

    int getChangedCount();

}
