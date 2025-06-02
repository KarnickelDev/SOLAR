package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.Component;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.ecs.systems.ECSSystem;

public abstract class ECSContext {

    private final EntityManager entityManager;
    private final ComponentRegistry componentRegistry;
    private final SystemRegistry systemRegistry;

    protected ECSContext() {
        entityManager = new EntityManager();
        componentRegistry = new ComponentRegistry();
        systemRegistry = new SystemRegistry();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }

    public SystemRegistry getSystemRegistry() {
        return systemRegistry;
    }

    public void registerSystem(ECSSystem system) {
        systemRegistry.registerSystem(system.getGroup(), system);
    }

    protected <T extends Component> void registerComponent(Class<T> type, T component) {
        componentRegistry.register(type, component);
    }

    public <S extends ComponentSnapshot> void registerHandler(Class<S> snapshotClass, ComponentSnapshotProvider<S> providerAndHandler) {
        componentRegistry.registerHandler(snapshotClass, providerAndHandler);
    }

    public <S extends ComponentSnapshot, T extends Component & ComponentSnapshotProvider<S>>
    void registerComponentAndHandler(Class<T> componentType, Class<S> snapshotType, T instance) {
        componentRegistry.register(componentType, instance);
        componentRegistry.registerHandler(snapshotType, instance);
    }

    public void update(long deltaTime) {
        for(ECSSystem system: systemRegistry.getSystemGroup(SystemGroup.UPDATE)) {
            system.update(deltaTime);
        }
    }

}
