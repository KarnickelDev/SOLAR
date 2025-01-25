package karnickeldev.solar.server.servers;

import karnickeldev.solar.gamestate.GameState;
import karnickeldev.solar.server.packets.Packet;
import karnickeldev.solar.server.simulation.SimulationExecutor;

public class DefaultServer implements SolarServer {

    private final SimulationExecutor simulationExecutor;

    public DefaultServer(GameState initialGameState) {
        simulationExecutor = new SimulationExecutor(initialGameState);
    }

    @Override
    public void start() {
        simulationExecutor.start();
    }

    @Override
    public void stop() {
        simulationExecutor.stop(5000);
    }

    @Override
    public GameState getCurrentGameState() {
        return simulationExecutor.getCurrentGameState();
    }

    @Override
    public void sendPacket() {

    }

    @Override
    public Packet receivePacket() {
        return null;
    }
}
