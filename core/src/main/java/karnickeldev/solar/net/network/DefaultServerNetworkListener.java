package karnickeldev.solar.net.network;

import karnickeldev.solar.net.packets.Packet;
import karnickeldev.solar.util.Logger;

public class DefaultServerNetworkListener implements ServerNetworkListener {

    @Override
    public void onClientConnected(int clientId) {
        Logger.log(Logger.SERVER, "Client connected");
    }

    @Override
    public void onClientDisconnected(int clientId) {
        Logger.log(Logger.SERVER, "Client disconnected");
    }

    @Override
    public void onPacketReceived(int clientId, Packet packet) {
        //Logger.log(Logger.SERVER, "Packet received");
    }
}
