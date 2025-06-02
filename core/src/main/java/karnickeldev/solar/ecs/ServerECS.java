package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.ecs.components.server.HCSServerSystem;
import karnickeldev.solar.ecs.systems.KeplerianOrbitSystem;
import karnickeldev.solar.world.ServerWorld;

public class ServerECS extends ECSContext {

    private final ServerWorld world;

    public final HCSServerSystem hcs;

    public ServerECS(ServerWorld world) {
        this.world = world;

        registerComponentAndHandler(TagComponent.class, TagSnapshot.class, new TagComponent());

        registerComponentAndHandler(RadiusComponent.class, RadiusSnapshot.class, new RadiusComponent());
        registerComponentAndHandler(MassComponent.class, MassSnapshot.class, new MassComponent());

        registerComponent(NameComponent.class, new NameComponent());
        registerComponent(SphereOfInfluenceComponent.class, new SphereOfInfluenceComponent());
        registerComponent(OrbitDataComponent.class, new OrbitDataComponent());

        hcs = new HCSServerSystem();

        registerSystem(new KeplerianOrbitSystem<ServerWorld>(world));
    }

}
