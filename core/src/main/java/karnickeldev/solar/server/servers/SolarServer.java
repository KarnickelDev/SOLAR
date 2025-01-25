package karnickeldev.solar.server.servers;

import karnickeldev.solar.gamestate.GameState;
import karnickeldev.solar.server.packets.Packet;

public interface SolarServer {

    void start();

    void stop();

    void sendPacket();

    Packet receivePacket();

    GameState getCurrentGameState();
}
