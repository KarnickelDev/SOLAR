package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.Component;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;

public abstract class ECSContext {

    private final EntityManager entityManager;
    private final ComponentRegistry componentRegistry;

    protected ECSContext() {
        entityManager = new EntityManager();
        componentRegistry = new ComponentRegistry();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
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
}
