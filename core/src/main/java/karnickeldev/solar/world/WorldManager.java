package karnickeldev.solar.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WorldManager<T extends World> {

    private final Map<Integer, T> worlds = new HashMap<>();
    private T activeWorld;
    private final T emptyWorld;

    public WorldManager(T emptyWorld) {
        this.emptyWorld = emptyWorld;
        activeWorld = emptyWorld;
    }

    public T getActiveWorld() {
        return activeWorld;
    }

    public boolean isEmpty() {
        return emptyWorld.getID() == activeWorld.getID();
    }

    private void setActiveWorld(T world) {
        if(world == null || !worlds.containsKey(world.getID())) throw new RuntimeException("Tried to change to invalid world!");
        activeWorld = world;
    }

    public void changeWorld(int newWorldId) {
        T newWorld = worlds.get(newWorldId);

        setActiveWorld(newWorld);
    }

    public T addWorld(T world) {
        if(world == null) return null;
        return worlds.put(world.getID(), world);
    }

    public T getWorld(int id) {
        T world =  worlds.get(id);
        if(world == null) throw new RuntimeException("World does not exist!");
        return world;
    }

    public Collection<T> getWorlds() {
        return worlds.values();
    }

}
