package karnickeldev.solar.context;

import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.scheduler.ClientScheduler;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.render.shader.ShaderManager;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.world.ClientClock;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;
import karnickeldev.solar.worldview.orbitsolver.OrbitSolver;

/**
 * @author KarnickelDev
 * @since 30.06.2025
 **/
public class GameContextContainer {

    private final WorldManager<ClientWorld> worldManager;
    private final ClientScheduler scheduler;

    private final ClientNetwork clientNetwork;
    private final ClientNetworkListener clientListener;

    private final ClientClock clientClock;

    private final OrbitSolver orbitSolver;

    private final PacketSyncLayer syncLayer;

    private final boolean multiplayer;

    private final ShaderManager shaderManager;

    private final ClientThreadLayout clientThreadLayout;

    public GameContextContainer(boolean multiplayer, WorldManager<ClientWorld> worldManager, ClientScheduler scheduler,
                                ClientNetwork clientNetwork, ClientNetworkListener clientListener,
                                ClientClock clientClock, OrbitSolver orbitSolver, PacketSyncLayer syncLayer, ShaderManager shaderManager,
                                ClientThreadLayout clientThreadLayout) {
        this.multiplayer = multiplayer;
        this.worldManager = worldManager;
        this.scheduler = scheduler;
        this.clientNetwork = clientNetwork;
        this.clientListener = clientListener;
        this.clientClock = clientClock;
        this.orbitSolver = orbitSolver;
        this.syncLayer = syncLayer;
        this.shaderManager = shaderManager;
        this.clientThreadLayout = clientThreadLayout;
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }

    public boolean isSingleplayer() {
        return !multiplayer;
    }

    public WorldManager<ClientWorld> getWorldManager() {
        return worldManager;
    }

    public ClientScheduler getScheduler() {
        return scheduler;
    }

    public ClientNetwork getClientNetwork() {
        return clientNetwork;
    }

    public ClientNetworkListener getClientListener() {
        return clientListener;
    }

    public ClientClock getClock() {
        return clientClock;
    }

    public OrbitSolver getOrbitSolver() {
        return orbitSolver;
    }

    public PacketSyncLayer getSyncLayer() {
        return syncLayer;
    }

    public ShaderManager getShaderManager() {
        return shaderManager;
    }

    public ClientThreadLayout getThreadLayout() {
        return clientThreadLayout;
    }
}
