package karnickeldev.solar.ecs.systems;

import karnickeldev.solar.ecs.SystemGroup;

public interface ECSSystem {

    void update(long deltaTime);

    byte priority();

    SystemGroup getGroup();

}
