package karnickeldev.solar.world;

import karnickeldev.solar.ecs.ServerECS;
import karnickeldev.solar.net.network.ServerNetwork;

public class ServerWorld extends World {

    public static int IDS = 0;

    private final ServerECS serverECS;
    private final int id;

    private final ServerNetwork serverNetwork;

    public ServerWorld(ServerNetwork serverNetwork) {
        this.id = IDS;
        IDS++;

        serverECS = new ServerECS(this);
        this.serverNetwork = serverNetwork;
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
