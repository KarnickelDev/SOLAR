package karnickeldev.solar.context;

import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.render.shader.ShaderManager;
import karnickeldev.solar.world.ClientClock;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

/**
 * @author KarnickelDev
 * @since 30.06.2025
 **/
public class GameContextContainer {

    private final WorldManager<ClientWorld> worldManager;
    private final Dispatcher dispatcher;

    private final ClientNetwork clientNetwork;
    private final ClientNetworkListener clientListener;

    private final ClientClock clientClock;
    private final PlanetoidRenderSystem rs;
    private final CameraInput cameraInput;

    private final PacketSyncLayer syncLayer;

    private final boolean multiplayer;

    private final ShaderManager shaderManager;

    public GameContextContainer(boolean multiplayer, WorldManager<ClientWorld> worldManager, Dispatcher dispatcher,
                                ClientNetwork clientNetwork, ClientNetworkListener clientListener,
                                ClientClock clientClock, PlanetoidRenderSystem rs, CameraInput cameraInput, PacketSyncLayer syncLayer, ShaderManager shaderManager) {
        this.multiplayer = multiplayer;
        this.worldManager = worldManager;
        this.dispatcher = dispatcher;
        this.clientNetwork = clientNetwork;
        this.clientListener = clientListener;
        this.clientClock = clientClock;
        this.rs = rs;
        this.cameraInput = cameraInput;
        this.syncLayer = syncLayer;
        this.shaderManager = shaderManager;
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

    public Dispatcher getDispatcher() {
        return dispatcher;
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

    public CameraInput getCameraInput() {
        return cameraInput;
    }

    public PlanetoidRenderSystem getPlanetoidRenderSystem() {
        return rs;
    }

    public PacketSyncLayer getSyncLayer() {
        return syncLayer;
    }

    public ShaderManager getShaderManager() {
        return shaderManager;
    }
}
