package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.systems.ECSSystem;

import java.util.*;

public class SystemRegistry {

    private final Map<SystemGroup, List<ECSSystem>> systemGroups = new HashMap<>();

    public SystemRegistry() {
        for(SystemGroup group: SystemGroup.values()) {
            systemGroups.put(group, new ArrayList<>());
        }
    }

    public List<ECSSystem> getSystemGroup(SystemGroup group) {
        return systemGroups.getOrDefault(group, new ArrayList<>(0));
    }

    public void registerSystem(SystemGroup group, ECSSystem system) {
        if(group == null || system == null) return;

        systemGroups.get(group).add(system);
        systemGroups.get(group).sort(Comparator.comparingInt(ECSSystem::priority));
    }

}
