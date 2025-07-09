package karnickeldev.solar.context;

import karnickeldev.solar.network.net.core.*;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.TimeEstimator;
import karnickeldev.solar.world.WorldManager;
import karnickeldev.solar.world.WorldTime;

/**
 * @author : KarnickelDev
 * @since : 30.06.2025
 **/
public class GameContextContainer {

    private final WorldManager<ClientWorld> worldManager;
    private final Dispatcher dispatcher;

    private final NetworkThread networkThread;
    private final ClientNetwork clientNetwork;
    private final ClientNetworkListener clientListener;

    private final TimeEstimator time;
    private final PlanetoidRenderSystem rs;
    private final CameraInput cameraInput;

    private final PacketSyncLayer syncLayer;

    private final boolean multiplayer;

    public GameContextContainer(boolean multiplayer, WorldManager<ClientWorld> worldManager, Dispatcher dispatcher,
                                NetworkThread networkThread, ClientNetwork clientNetwork, ClientNetworkListener clientListener,
                                TimeEstimator time, PlanetoidRenderSystem rs, CameraInput cameraInput, PacketSyncLayer syncLayer) {
        this.multiplayer = multiplayer;
        this.worldManager = worldManager;
        this.dispatcher = dispatcher;
        this.networkThread = networkThread;
        this.clientNetwork = clientNetwork;
        this.clientListener = clientListener;
        this.time = time;
        this.rs = rs;
        this.cameraInput = cameraInput;
        this.syncLayer = syncLayer;
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

    public NetworkThread getNetworkThread() {
        return networkThread;
    }

    public ClientNetwork getClientNetwork() {
        return clientNetwork;
    }

    public ClientNetworkListener getClientListener() {
        return clientListener;
    }

    public TimeEstimator getTime() {
        return time;
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
}
