package karnickeldev.solar.net.server;

import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.net.network.MainThreadDispatcher;
import karnickeldev.solar.net.network.NetworkThread;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.PacketRegistry;
import karnickeldev.solar.simulation.execution.SimulationManagerThread;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

public abstract class Server implements GameServer {

    private static Server instance = null;
    public static Server getInstance() {
        return instance;
    }

    protected final ServerNetwork serverNetwork;
    protected final NetworkThread networkThread;

    protected final WorldManager<ServerWorld> worldManager;

    protected final SimulationManagerThread simulationManagerThread;

    private boolean running = false;

    public Server(ServerNetwork serverNetwork, NetworkThread networkThread, WorldManager<ServerWorld> worldManager, MainThreadDispatcher dispatcher) {
        this.serverNetwork = serverNetwork;
        this.networkThread = networkThread;
        this.worldManager = worldManager;

        this.simulationManagerThread = new SimulationManagerThread(8, worldManager, dispatcher);

        PacketRegistry.register(1, HCSPositionSnapshot.class, new HCSPositionSnapshot(0, 0));
        instance = this;
    }

    public final void start() {
        running = true;

        serverNetwork.start();
        networkThread.start();

        simulationManagerThread.start();
    }

    public final void stop() {
        simulationManagerThread.stop();

        serverNetwork.shutdown();

        networkThread.stop();
        running = false;
    }

    public final boolean isRunning() {
        return running;
    }

    protected abstract void preTick();

    protected abstract void postTick();

    public final ServerNetwork getServerNetwork() {
        return serverNetwork;
    }

    public final NetworkThread getNetworkThread() {
        return networkThread;
    }

    public final SimulationManagerThread getSimulationManagerThread() {
        return simulationManagerThread;
    }
}
