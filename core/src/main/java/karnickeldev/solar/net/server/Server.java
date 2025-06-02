package karnickeldev.solar.net.server;

import karnickeldev.solar.ecs.components.HCSPositionSnapshot;
import karnickeldev.solar.net.network.MainThreadDispatcher;
import karnickeldev.solar.net.network.NetworkThread;
import karnickeldev.solar.net.network.ServerNetwork;
import karnickeldev.solar.net.packets.ComponentSnapshotRegistry;
import karnickeldev.solar.simulation.SimulationExecutor;
import karnickeldev.solar.simulation.execution.SimulationManager;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

public abstract class Server implements GameServer {

    public static final int TICK_RATE = 60;

    protected final ServerNetwork serverNetwork;
    protected final NetworkThread networkThread;

    protected final WorldManager<ServerWorld> worldManager;
    protected final SimulationExecutor simulationExecutor;

    protected final SimulationManager simulationManager;

    private boolean running = false;

    public Server(ServerNetwork serverNetwork, NetworkThread networkThread, WorldManager<ServerWorld> worldManager, SimulationExecutor simulationExecutor, MainThreadDispatcher mainThreadDispatcher) {
        this.serverNetwork = serverNetwork;
        this.networkThread = networkThread;
        this.simulationExecutor = simulationExecutor;
        this.worldManager = worldManager;

        this.simulationManager = new SimulationManager(8, worldManager);

        ComponentSnapshotRegistry.register(1, HCSPositionSnapshot.class, new HCSPositionSnapshot(0, 0));
    }

    public final void start() {
        running = true;

        serverNetwork.start();
        networkThread.start();

        simulationExecutor.start();
    }

    public final void stop() {
        running = false;

        simulationExecutor.stop();

        serverNetwork.shutdown();

        networkThread.stop();
    }

    public final boolean isRunning() {
        return running;
    }

    protected void tick() {

    }

    protected abstract void preTick();

    protected abstract void postTick();

    public final ServerNetwork getServerNetwork() {
        return serverNetwork;
    }

    public final NetworkThread getNetworkThread() {
        return networkThread;
    }

    public final SimulationExecutor getSimulationExecutor() {
        return simulationExecutor;
    }
}
