package karnickeldev.solar.world;

import karnickeldev.solar.ecs.ECSContext;
import karnickeldev.solar.net.network.Network;

public abstract class World {

    protected final WorldTime worldTime;

    protected World() {
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

    public void update(long deltaTime) {
        getECS().update(deltaTime);
    }

}
