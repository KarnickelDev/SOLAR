package karnickeldev.solar.ecs.components;

public interface ComponentSnapshotProvider<S extends ComponentSnapshot> extends Component {

    S createSnapshot(long tick);

    void applySnapshot(S snapshot);

}
