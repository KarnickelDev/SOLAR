package karnickeldev.solar.world;

import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.net.network.core.ServerNetwork;

import java.util.Objects;

public class ServerWorld extends World {

    public static int IDS = 0;

    private ServerECS serverECS;
    private final int id;

    private final ServerNetwork serverNetwork;

    public static ServerWorld create(ServerNetwork serverNetwork) {
        if(serverNetwork == null) return null;
        ServerWorld world = new ServerWorld(serverNetwork);
        world.serverECS = new ServerECS(world);
        return world;
    }

    private ServerWorld(ServerNetwork serverNetwork) {
        this.id = IDS;
        IDS++;

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
