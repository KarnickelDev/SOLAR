package karnickeldev.solar.ecs.systems;

import karnickeldev.solar.ecs.SystemGroup;

public interface ECSSystem {

    void update(long deltaTime);

    default byte priority() {
        return 0;
    }

    SystemGroup getGroup();

}
