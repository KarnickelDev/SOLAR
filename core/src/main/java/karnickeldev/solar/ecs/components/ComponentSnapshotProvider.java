package karnickeldev.solar.ecs.components;

public interface ComponentSnapshotProvider<S extends ComponentSnapshot> extends Component {

    S createSnapshot(long simTimeMicros);

    S createFullSnapshot(long simTimeMicros);

    void applySnapshot(S snapshot);

}
