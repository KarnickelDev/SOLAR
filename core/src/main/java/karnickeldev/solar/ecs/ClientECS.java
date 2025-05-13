package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.ecs.components.client.HCSClientSystem;
import karnickeldev.solar.ecs.components.server.HCSPositionSnapshot;

public class ClientECS extends ECSContext {

    public final HCSClientSystem hcs;

    public ClientECS() {
        registerComponentAndHandler(TagComponent.class, TagSnapshot.class, new TagComponent());

        registerComponentAndHandler(RadiusComponent.class, RadiusSnapshot.class, new RadiusComponent());
        registerComponentAndHandler(MassComponent.class, MassSnapshot.class, new MassComponent());

        registerComponent(NameComponent.class, new NameComponent());
        registerComponent(SphereOfInfluenceComponent.class, new SphereOfInfluenceComponent());
        registerComponent(OrbitDataComponent.class, new OrbitDataComponent());

        hcs = new HCSClientSystem();
        registerHandler(HCSPositionSnapshot.class, hcs);
    }

}
