package karnickeldev.solar.network.net.core;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.GameStatePacket;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.util.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalClientNetwork implements ClientNetwork {

    private static final int OUTGOING_QUEUE_CAPACITY = 128;

    private final BlockingQueue<Packet> loopbackToServerQueue;
    private final BlockingQueue<Packet> loopbackFromServerQueue;
    private final ClientNetworkListener listener;
    private final Dispatcher dispatcher;

    private final BlockingQueue<Packet> outgoingPacketQueue = new LinkedBlockingQueue<>(128);

    public LocalClientNetwork(
        BlockingQueue<Packet> loopbackToServerQueue,
        BlockingQueue<Packet> loopbackFromServerQueue,
        ClientNetworkListener listener,
        Dispatcher dispatcher) {
        this.loopbackToServerQueue = loopbackToServerQueue;
        this.loopbackFromServerQueue = loopbackFromServerQueue;
        this.listener = listener;
        this.dispatcher = dispatcher;
    }

    @Override
    public void send(Packet packet) {
        if (outgoingPacketQueue.size() >= OUTGOING_QUEUE_CAPACITY) {
            Logger.error(Logger.NETWORK, "Packet dropped due to client queue-buffer overflow");
        } else {
            outgoingPacketQueue.offer(packet);
        }
    }


    @Override
    public boolean updateNetwork() {
        boolean work = false;

        Packet packet;

        // flush outgoing packets
        while ((packet = outgoingPacketQueue.poll()) != null) {
            work = true;
            if (!loopbackToServerQueue.offer(packet)) {
                Logger.error(Logger.NETWORK, "Packet dropped due to client send-buffer overflow");
            } else {
                //Logger.log("Client send packet " + packet.getTick());
            }
        }

        // receive incoming and dispatch handling on main thread
        while ((packet = loopbackFromServerQueue.poll()) != null) {
            work = true;
            //Logger.log("Client received Packet " + packet.getTick());
            if(packet.isFastHandled()) {
                //listener.onPacketReceived(finalPacket);
                HandlerRegistry.getHandler(packet).handle(0, packet);
            } else if(packet instanceof GameStatePacket) {
                GameContext.get().getSyncLayer().receivePacket((GameStatePacket) packet);
            } else {
                Packet finalPacket = packet;
                //dispatcher.dispatch(() -> listener.onPacketReceived(finalPacket));
                dispatcher.dispatch(() -> HandlerRegistry.getHandler(finalPacket).handle(0, finalPacket));
            }
        }

        return work;
    }

    @Override
    public boolean connect() {
        dispatcher.dispatch(listener::onConnected);
        return true;
    }

    @Override
    public void disconnect() {
        dispatcher.dispatch(listener::onDisconnected);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

}
