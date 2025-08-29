package karnickeldev.solar.world;

import com.badlogic.gdx.Gdx;
import karnickeldev.solar.ecs.ClientECS;
import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.render.camera.FloatingOriginCamera;

public class ClientWorld extends World {

    private final int worldId;

    private final ClientECS ecs = new ClientECS();
    private final FloatingOriginCamera camera;

    public ClientWorld(int worldId) {
        this.worldId = worldId;
        camera = new FloatingOriginCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), ecs.hcs);
    }

    @Override
    public int getID() {
        return worldId;
    }

    @Override
    public ClientECS getECS() {
        return ecs;
    }

    @Override
    public ClientNetwork getNetwork() {
        return null;
    }

    public FloatingOriginCamera getCamera() {
        return camera;
    }
}
