package karnickeldev.solar.world;

import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.network.net.core.ServerNetwork;

import java.util.Objects;

public class ServerWorld extends World {

    private ServerECS serverECS;
    private final int id;

    private final ServerNetwork serverNetwork;

    public static ServerWorld create(int id, ServerNetwork serverNetwork) {
        if(serverNetwork == null) return null;
        ServerWorld world = new ServerWorld(id, serverNetwork);
        world.serverECS = new ServerECS(world);
        return world;
    }

    private ServerWorld(int id, ServerNetwork serverNetwork) {
        this.id = id;

        this.serverNetwork = Objects.requireNonNull(serverNetwork);
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public ServerECS getECS() {
        return serverECS;
    }

    @Override
    public ServerNetwork getNetwork() {
        return serverNetwork;
    }
}
