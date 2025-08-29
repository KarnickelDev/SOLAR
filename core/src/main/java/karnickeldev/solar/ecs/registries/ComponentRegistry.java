package karnickeldev.solar.ecs.registries;

import karnickeldev.solar.ecs.components.Component;
import karnickeldev.solar.ecs.components.ComponentSnapshot;
import karnickeldev.solar.ecs.components.ComponentSnapshotProvider;
import karnickeldev.solar.util.Logger;

import java.util.*;

public class ComponentRegistry {

    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final Map<Class<? extends ComponentSnapshot>, ComponentSnapshotProvider<? extends ComponentSnapshot>> snapshots = new HashMap<>();

    public <T extends Component> void register(Class<T> type, T component) {
        components.put(type, component);
    }

    public <S extends ComponentSnapshot> void registerSnapshot(Class<S> snapshotClass, ComponentSnapshotProvider<S> providerAndHandler) {
        snapshots.put(snapshotClass, providerAndHandler);
    }

    public <T extends Component> T get(Class<T> type) {
        try {
            return type.cast(components.get(type));
        } catch (ClassCastException e) {
            Logger.log("Class can't be cast to Component");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <P extends ComponentSnapshot, T extends ComponentSnapshotProvider<P>> ComponentSnapshotProvider<P> getSnapshotProvider(Class<P> type) {
        return (T) snapshots.get(type);
    }

    public Collection<Component> getAllComponents() {
        return components.values();
    }

    public List<ComponentSnapshot> createAllSnapshots(long simTimeMicros) {
        List<ComponentSnapshot> snaps = new ArrayList<>(snapshots.size());

        for (Class<? extends ComponentSnapshot> snap : snapshots.keySet()) {
            ComponentSnapshot s = getSnapshotProvider(snap).createSnapshot(simTimeMicros);
            if (s != null) snaps.add(s);
        }

        return snaps;
    }

    public List<ComponentSnapshot> createFullSnapshot(long simTimeMicros) {
        List<ComponentSnapshot> snaps = new ArrayList<>(snapshots.size());

        for (Class<? extends ComponentSnapshot> snap : snapshots.keySet()) {
            ComponentSnapshot s = getSnapshotProvider(snap).createFullSnapshot(simTimeMicros);
            if (s != null) snaps.add(s);
        }

        return snaps;
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
