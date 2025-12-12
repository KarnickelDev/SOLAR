package karnickeldev.solar.context;

import karnickeldev.solar.core.SolarMain;
import karnickeldev.solar.ecs.EntityManager;
import karnickeldev.solar.network.net.DefaultClientNetworkListener;
import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.scheduler.*;
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
import karnickeldev.solar.render.shader.ShaderManager;
import karnickeldev.solar.util.threadlayout.ThreadAffinity;
import karnickeldev.solar.util.threadlayout.ClientThreadLayout;
import karnickeldev.solar.world.ClientClock;
import karnickeldev.solar.world.ClientWorld;
import karnickeldev.solar.world.WorldManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author KarnickelDev
 * @since 01.07.2025
 **/
public class GameContextBuilder {

    public static GameContextContainer buildClientDedicatedServer(String host, int port, ClientThreadLayout threadLayout) {
        WorldManager<ClientWorld> worldManager = new WorldManager<>(new ClientWorld(0));

        ClientNetworkListener clientListener = new DefaultClientNetworkListener(worldManager);

        ClientScheduler scheduler = new ClientScheduler();

        ClientClock clientClock = new ClientClock();

        PacketSyncLayer syncLayer = new PacketSyncLayer();

        ClientNetwork clientNetwork = new NettyClientNetwork(host, port, clientListener, scheduler.main(), syncLayer, clientClock);

        PlanetoidRenderSystem rs = new PlanetoidRenderSystem(worldManager, SolarMain.getInstance().getBatch(), threadLayout);

        CameraInput cameraInput = new CameraInput(worldManager);

        ShaderManager shaderManager = new ShaderManager();

        if(threadLayout.getMainContext().useCoreAffinity()) ThreadAffinity.pinToCore(threadLayout.getMainContext().nextCpuId());

        return new GameContextContainer(
            true,
            worldManager,
            scheduler,
            clientNetwork,
            clientListener,
            clientClock,
            rs,
            cameraInput,
            syncLayer,
            shaderManager
        );
    }

    public static GameContextContainer buildClientLocalServer(ClientThreadLayout threadLayout) {
        WorldManager<ClientWorld> worldManager = new WorldManager<>(new ClientWorld(0)); // TODO: remove EmptyWorld, no longer needed

        ClientNetworkListener clientListener = new DefaultClientNetworkListener(worldManager);

        ClientScheduler scheduler = new ClientScheduler();

        PacketSyncLayer syncLayer = new PacketSyncLayer();

        ClientNetwork clientNetwork;

        ServerNetworkListener serverListener = new DefaultServerNetworkListener();

        Scheduler serverDispatcher = new DefaultScheduler();

        ClientClock time = new ClientClock();

        BlockingQueue<Packet> toServer = new LinkedBlockingQueue<>(128);
        BlockingQueue<Packet> fromServer = new LinkedBlockingQueue<>(128);

        clientNetwork = new LocalClientNetwork(toServer, fromServer, clientListener, scheduler.main());
        ServerNetwork serverNetwork = new LocalServerNetwork(toServer, fromServer, serverListener, serverDispatcher.main());

        Server server = LocalServer.create(serverNetwork, serverDispatcher, threadLayout.getSimulationContext());
        ServerContext.setContext(ServerContextBuilder.buildServerContext(server));

        for(int i = 0; i < EntityManager.MAX_ENTITIES; i++) {
            server.getWorldManager().getWorld(1).getECS().hcs.add(i,0,0,0);
        }

        PlanetoidRenderSystem rs = new PlanetoidRenderSystem(worldManager, SolarMain.getInstance().getBatch(), threadLayout);

        CameraInput cameraInput = new CameraInput(worldManager);

        ShaderManager shaderManager = new ShaderManager();

        if(threadLayout.getMainContext().useCoreAffinity()) ThreadAffinity.pinToCore(threadLayout.getMainContext().nextCpuId());

        return new GameContextContainer(
            false,
            worldManager,
            scheduler,
            clientNetwork,
            clientListener,
            time,
            rs,
            cameraInput,
            syncLayer,
            shaderManager
            );
    }

}
