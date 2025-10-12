package karnickeldev.solar.ecs;

import karnickeldev.solar.ecs.components.*;
import karnickeldev.solar.ecs.systems.HCSClientSystem;
import karnickeldev.solar.physics.Vector2D;

public class ClientECS extends ECSContext {

    public final HCSClientSystem hcs;

    public ClientECS() {
        registerComponentAndHandler(TagComponent.class, TagSnapshot.class, new TagComponent());

        registerComponentAndHandler(RadiusComponent.class, RadiusSnapshot.class, new RadiusComponent());
        registerComponentAndHandler(MassComponent.class, MassSnapshot.class, new MassComponent());

        registerComponent(NameComponent.class, new NameComponent());
        registerComponent(SphereOfInfluenceComponent.class, new SphereOfInfluenceComponent());
        registerComponentAndHandler(OrbitDataComponent.class, OrbitDataSnapshot.class, new OrbitDataComponent());

        // textures
        RenderComponent renderComponent = new RenderComponent();
        registerComponent(RenderComponent.class, renderComponent);
        registerHandler(AppearanceSnapshot.class, renderComponent);

        hcs = new HCSClientSystem();
        registerHandler(HCSPositionSnapshot.class, hcs);
    }

    public Vector2D toWorldSpace(Vector2D vec, int entity, double alpha) {
        int current = entity;

        while(current != EntityManager.NO_ENTITY) {
            vec.add(hcs.getInterpolatedX(current, alpha), hcs.getInterpolatedY(current, alpha));

            int parent = hcs.getCurrent().getParent(current);
            if(current == parent) break;

            current = parent;
        }

        return vec;
    }

    public Vector2D toWorldSpace(int entity, double alpha) {
        return toWorldSpace(new Vector2D(), entity, alpha);
    }

    public Vector2D toLocalSpace(int entity, double alpha) {
        Vector2D local = new Vector2D();
        int current = entity;

        while (current != EntityManager.NO_ENTITY) {
            local.subtract(hcs.getInterpolatedX(current, alpha), hcs.getInterpolatedY(current, alpha));
            current = hcs.getCurrent().getParent(current);
        }

        return local;
    }

    public Vector2D toRelativeSpace(int entity, int referenceEntity, double alpha) {
        return toWorldSpace(entity, alpha).subtract(toWorldSpace(referenceEntity, alpha));
    }

    public Vector2D toRelativeSpace(Vector2D a, Vector2D b, int entity, int referenceEntity, double alpha) {
        return toWorldSpace(a, entity, alpha).subtract(toWorldSpace(b, referenceEntity, alpha));
    }

}
