package karnickeldev.solar.world;

import karnickeldev.solar.ecs.ECSContext;
import karnickeldev.solar.network.net.core.Network;
import karnickeldev.solar.worldview.orbitgraph.OrbitGraphSystem;
import karnickeldev.solar.worldview.transform.WorldTransformData;

public abstract class World {

    protected final WorldTime worldTime;

    private final OrbitGraphSystem orbitGraphSystem;
    private final WorldTransformData worldTransform;

    protected World() {
        orbitGraphSystem = new OrbitGraphSystem();
        worldTransform = new WorldTransformData();

        // TODO: initialize world time from save-file
        worldTime = new WorldTime(0);
    }

    @Override
    public String toString() {
        return "World("+getID()+')';
    }

    public abstract int getID();

    public abstract ECSContext getECS();

    public abstract Network getNetwork();

    public final WorldTime getWorldTime() {
        return worldTime;
    }

    public OrbitGraphSystem getOrbitGraphSystem() {
        return orbitGraphSystem;
    }

    public WorldTransformData getWorldTransform() {
        return worldTransform;
    }

    public void update(long deltaTime) {
        getECS().update(deltaTime);
    }

}
