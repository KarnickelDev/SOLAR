package karnickeldev.solar.network.net.core;

import karnickeldev.solar.network.net.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.FullSnapshotRequestPacket;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.util.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalClientNetwork implements ClientNetwork {

    private static final int OUTGOING_QUEUE_CAPACITY = 128;

    private final BlockingQueue<Packet> loopbackToServerQueue;
    private final BlockingQueue<Packet> loopbackFromServerQueue;
    private final ClientNetworkListener listener;
    private final MainThreadDispatcher dispatcher;

    private final BlockingQueue<Packet> outgoingPacketQueue = new LinkedBlockingQueue<>(128);

    public LocalClientNetwork(
        BlockingQueue<Packet> loopbackToServerQueue,
        BlockingQueue<Packet> loopbackFromServerQueue,
        ClientNetworkListener listener,
        MainThreadDispatcher dispatcher) {
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
            Packet finalPacket = packet;
            if(finalPacket.isFastHandled()) {
                listener.onPacketReceived(finalPacket);
            } else {
                dispatcher.dispatch(() -> listener.onPacketReceived(finalPacket));
            }
        }

        return work;
    }

    @Override
    public void connect() {
        dispatcher.dispatch(listener::onConnected);
        send(new FullSnapshotRequestPacket(1));
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
