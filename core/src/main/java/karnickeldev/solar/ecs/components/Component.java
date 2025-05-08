package karnickeldev.solar.ecs.components;

public interface Component {

//    interface ComponentSnapshot {
//
//    }
//
//    ComponentSnapshot buildSnapshot();

    void ensureCapacity(int entityId);

}
