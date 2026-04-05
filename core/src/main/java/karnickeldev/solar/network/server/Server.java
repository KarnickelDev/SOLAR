package karnickeldev.solar.network.server;

import karnickeldev.solar.ecs.components.ComponentType;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.net.handlers.ServerChatMessageHandler;
import karnickeldev.solar.scheduler.Dispatcher;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.scheduler.Scheduler;
import karnickeldev.solar.simulation.execution.SimulationManagerThread;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

public abstract class Server implements GameServer {

    protected final ServerNetwork serverNetwork;

    protected final WorldManager<ServerWorld> worldManager;

    protected final SimulationManagerThread simulationManagerThread;

    protected final Scheduler scheduler;

    private boolean running = false;

    public Server(ServerNetwork serverNetwork, WorldManager<ServerWorld> worldManager, SimulationManagerThread simulationManagerThread, Scheduler scheduler) {
        this.serverNetwork = serverNetwork;
        this.worldManager = worldManager;
        this.simulationManagerThread = simulationManagerThread;
        this.scheduler = scheduler;
    }

    public final void start() {

        PacketTypes.registerCommon();
        PacketTypes.CHAT_MESSAGE.registerHandler(new ServerChatMessageHandler());

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

        scheduler.shutdown();
        scheduler.main().update(30_000);
        scheduler.timer().update(System.currentTimeMillis());

        running = false;
    }

    public final boolean isRunning() {
        return running;
    }

    protected abstract void preTick();

    protected abstract void postTick();

    public final Scheduler getScheduler() {
        return scheduler;
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
