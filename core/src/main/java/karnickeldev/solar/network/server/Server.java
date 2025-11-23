package karnickeldev.solar.network.server;

import karnickeldev.solar.ecs.components.ComponentType;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.simulation.execution.SimulationManagerThread;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

public abstract class Server implements GameServer {

    protected final ServerNetwork serverNetwork;

    protected final WorldManager<ServerWorld> worldManager;

    protected final SimulationManagerThread simulationManagerThread;

    protected final Dispatcher dispatcher;

    private boolean running = false;

    public Server(ServerNetwork serverNetwork, WorldManager<ServerWorld> worldManager, SimulationManagerThread simulationManagerThread, Dispatcher dispatcher) {
        this.serverNetwork = serverNetwork;
        this.worldManager = worldManager;
        this.simulationManagerThread = simulationManagerThread;
        this.dispatcher = dispatcher;
    }

    public final void start() {

        PacketTypes.registerCommon();

        ComponentType.registerSnapshotDeserializers();

        // make sure buffers are correctly initialized
        for(ServerWorld world: worldManager.getWorlds()) {
            world.getECS().hcs.swapBuffers();
        }

        running = true;

        serverNetwork.start();

        simulationManagerThread.start();
    }

    public final void stop() {
        simulationManagerThread.stop();

        serverNetwork.shutdown();

        dispatcher.shutdown();
        dispatcher.update(30_000);

        running = false;
    }

    public final boolean isRunning() {
        return running;
    }

    protected abstract void preTick();

    protected abstract void postTick();

    public final Dispatcher getDispatcher() {
        return dispatcher;
    }

    public final ServerNetwork getServerNetwork() {
        return serverNetwork;
    }

    public final WorldManager<ServerWorld> getWorldManager() {
        return worldManager;
    }

    public final SimulationManagerThread getSimulationManagerThread() {
        return simulationManagerThread;
    }
}
