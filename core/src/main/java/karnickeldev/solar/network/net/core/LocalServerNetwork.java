package karnickeldev.solar.network.net.core;

import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ServerNetworkListener;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocalServerNetwork implements ServerNetwork {

    private static final int OUTGOING_QUEUE_CAPACITY = 256;

    private final BlockingQueue<Packet> loopbackToServerQueue;
    private final BlockingQueue<Packet> loopbackFromServerQueue;
    private final ServerNetworkListener listener;
    private final Dispatcher dispatcher;

    private final ConcurrentLinkedQueue<Packet> outgoingPacketQueue = new ConcurrentLinkedQueue<>();

    public LocalServerNetwork(
        BlockingQueue<Packet> loopbackToServerQueue,
        BlockingQueue<Packet> loopbackFromServerQueue,
        ServerNetworkListener listener,
        Dispatcher dispatcher) {
        this.loopbackToServerQueue = loopbackToServerQueue;
        this.loopbackFromServerQueue = loopbackFromServerQueue;
        this.listener = listener;
        this.dispatcher = dispatcher;
    }

    @Override
    public void broadcast(Packet packet) {
        sendToClient(0, packet);
    }

    @Override
    public void sendToClient(int id, Packet packet) {
        if (outgoingPacketQueue.size() >= OUTGOING_QUEUE_CAPACITY) {
            Logger.error(Logger.NETWORK, "Packet dropped due to server queue-buffer overflow");
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
            if (!loopbackFromServerQueue.offer(packet)) {
                Logger.error(Logger.NETWORK, "Packet dropped due to server send-buffer overflow");
            }
        }

        // receive incoming and dispatch handling on main thread
        while ((packet = loopbackToServerQueue.poll()) != null) {
            work = true;
            Packet finalPacket = packet;
            if(finalPacket.isFastHandled()) {
                //listener.onPacketReceived(0, finalPacket);
                HandlerRegistry.getHandler(packet).handle(0, packet);
            } else {
                //dispatcher.dispatch(() -> listener.onPacketReceived(0, finalPacket));
                dispatcher.dispatch(() -> HandlerRegistry.getHandler(finalPacket).handle(0, finalPacket));
            }
        }

        return work;
    }

    @Override
    public boolean start() {
        Logger.log(Logger.SERVER, "Server-Network started");
        return true;
    }

    @Override
    public void shutdown() {
        Logger.log(Logger.SERVER, "Server-Network shutdown");
    }

}
