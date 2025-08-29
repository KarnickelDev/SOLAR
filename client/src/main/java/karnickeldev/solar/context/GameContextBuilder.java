package karnickeldev.solar.context;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.network.net.DefaultClientNetworkListener;
import karnickeldev.solar.network.net.core.*;
import karnickeldev.solar.network.net.dispatcher.DefaultDispatcher;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.net.listener.DefaultServerNetworkListener;
import karnickeldev.solar.network.net.listener.ServerNetworkListener;
import karnickeldev.solar.network.net.transport.local.LocalClientNetwork;
import karnickeldev.solar.network.net.transport.local.LocalServerNetwork;
import karnickeldev.solar.network.net.transport.netty.NettyClientNetwork;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.server.LocalServer;
import karnickeldev.solar.network.server.Server;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.render.PlanetoidRenderSystem;
import karnickeldev.solar.render.camera.CameraInput;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.ClientClock;
import karnickeldev.solar.world.WorldManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author : KarnickelDev
 * @since : 01.07.2025
 **/
public class GameContextBuilder {

    public static GameContextContainer buildClientDedicatedServer(String host, int port) {
        WorldManager<ClientWorld> worldManager = new WorldManager<>(new ClientWorld(0));

        ClientNetworkListener clientListener = new DefaultClientNetworkListener(worldManager);
        Dispatcher dispatcher = new DefaultDispatcher();

        ClientClock clientClock = new ClientClock();

        PacketSyncLayer syncLayer = new PacketSyncLayer();

        ClientNetwork clientNetwork = new NettyClientNetwork(host, port, clientListener, dispatcher, syncLayer, clientClock);

        PlanetoidRenderSystem rs = new PlanetoidRenderSystem(worldManager, SolarMain.getInstance().getBatch());

        CameraInput cameraInput = new CameraInput(worldManager);

        return new GameContextContainer(
            true,
            worldManager,
            dispatcher,
            clientNetwork,
            clientListener,
            clientClock,
            rs,
            cameraInput,
            syncLayer
        );
    }

    public static GameContextContainer buildClientLocalServer() {
        WorldManager<ClientWorld> worldManager = new WorldManager<>(new ClientWorld(0));

        ClientNetworkListener clientListener = new DefaultClientNetworkListener(worldManager);
        Dispatcher dispatcher = new DefaultDispatcher();

        PacketSyncLayer syncLayer = new PacketSyncLayer();

        ClientNetwork clientNetwork;

        ServerNetworkListener serverListener = new DefaultServerNetworkListener();

        Dispatcher serverDispatcher = new DefaultDispatcher();

        ClientClock time = new ClientClock();

        BlockingQueue<Packet> toServer = new LinkedBlockingQueue<>(128);
        BlockingQueue<Packet> fromServer = new LinkedBlockingQueue<>(128);

        clientNetwork = new LocalClientNetwork(toServer, fromServer, clientListener, dispatcher);
        ServerNetwork serverNetwork = new LocalServerNetwork(toServer, fromServer, serverListener, serverDispatcher);

        Server server = LocalServer.create(serverNetwork, serverDispatcher);
        ServerContext.setContext(ServerContextBuilder.buildServerContext(server));


        PlanetoidRenderSystem rs = new PlanetoidRenderSystem(worldManager, SolarMain.getInstance().getBatch());

        CameraInput cameraInput = new CameraInput(worldManager);

        return new GameContextContainer(
            false,
            worldManager,
            dispatcher,
            clientNetwork,
            clientListener,
            time,
            rs,
            cameraInput,
            syncLayer
            );
    }

}
