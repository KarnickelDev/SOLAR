package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.Component;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.util.Logger;

import java.util.*;

public class ComponentRegistry {

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final Map<Class<? extends ComponentSnapshot>, ComponentSnapshotProvider<? extends ComponentSnapshot>> snapshots = new HashMap<>();

    public <T extends Component, P extends ComponentSnapshot> void register(Class<T> type, T component) {
        components.put(type, component);
    }

    public <S extends ComponentSnapshot> void registerHandler(Class<S> snapshotClass, ComponentSnapshotProvider<S> providerAndHandler) {
        snapshots.put(snapshotClass, providerAndHandler);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T get(Class<T> type) {
        return (T) components.get(type);
    }

    @SuppressWarnings("unchecked")
    public <P extends ComponentSnapshot, T extends ComponentSnapshotProvider<P>> ComponentSnapshotProvider<P> getSnapshotProvider(Class<P> type) {
        return (T) snapshots.get(type);
    }

    public Collection<Component> getAll() {
        return components.values();
    }

    public List<ComponentSnapshot> createAllSnapshots(long tick) {
        List<ComponentSnapshot> snapshots = new ArrayList<>(components.size());

        for (Component component : components.values()) {
            if (component instanceof ComponentSnapshotProvider) {
                ComponentSnapshot snap = ((ComponentSnapshotProvider<?>) component).createSnapshot(tick);
                if (snap != null) snapshots.add(snap);
            }
        }

        return snapshots;
    }

    @SuppressWarnings("unchecked")
    public void applyAllSnapshots(ComponentSnapshot... snaps) {
        if(snaps == null) {
            Logger.log("empty snapshot");
            return;
        }
        for (ComponentSnapshot snap : snaps) {
            ComponentSnapshotProvider<ComponentSnapshot> provider = (ComponentSnapshotProvider<ComponentSnapshot>) snapshots.get(snap.getClass());
            if (provider != null) {
                provider.applySnapshot(snap);
            }
        }
    }

}
