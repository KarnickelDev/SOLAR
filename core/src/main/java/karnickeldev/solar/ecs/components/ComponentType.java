package karnickeldev.solar.ecs.components;

import karnickeldev.solar.ecs.registries.SnapshotRegistry;
import karnickeldev.solar.net.network.core.Deserializer;

/**
 * @author : KarnickelDev
 * @since : 21.06.2025
 **/
public enum ComponentType {

    POSITION(HCSPositionComponent.class, HCSPositionSnapshot.class, HCSPositionSnapshot::deserialize),
    MASS(MassComponent.class, MassSnapshot.class, MassSnapshot::deserialize),
    NAME(NameComponent.class),
    RADIUS(RadiusComponent.class, RadiusSnapshot.class, RadiusSnapshot::deserialize),
    TAG(TagComponent.class, TagSnapshot.class, TagSnapshot::deserialize),
    ;

    private final Class<? extends Component> clazz;
    private final Class<? extends ComponentSnapshot> snapshotClazz;
    private final Deserializer<ComponentSnapshot> deserializer;
    ComponentType(Class<? extends Component> clazz, Class<? extends ComponentSnapshot> snapshot, Deserializer<ComponentSnapshot> deserializer) {
        this.clazz = clazz;
        this.snapshotClazz = snapshot;
        this.deserializer = deserializer;
    }
    ComponentType(Class<?extends Component> clazz) {
        this.clazz = clazz;
        this.snapshotClazz = null;
        this.deserializer = null;
    }

    public short getTypeId() {
        return (short) this.ordinal();
    }

    public Class<? extends Component> getComponentClass() {
        return clazz;
    }

    public Class<? extends ComponentSnapshot> getSnapshotClass() {
        return snapshotClazz;
    }

    public Deserializer<? extends ComponentSnapshot> getDeserializer() {
        return deserializer;
    }

    public static void registerSnapshotDeserializers() {
        for(ComponentType componentType: values()) {
            if(componentType.snapshotClazz != null && componentType.deserializer != null) {
                SnapshotRegistry.register(componentType.getTypeId(), componentType.snapshotClazz, componentType.deserializer);
            }
        }
    }

}
