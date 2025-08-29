package karnickeldev.solar.network.net.transport.local;

import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ServerNetworkListener;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.util.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalServerNetwork implements ServerNetwork {

    private final BlockingQueue<Packet> loopbackToServerQueue;
    private final BlockingQueue<Packet> loopbackFromServerQueue;
    private final ServerNetworkListener listener;
    private final Dispatcher dispatcher;

    private final ConcurrentLinkedQueue<Packet> outgoingPacketQueue = new ConcurrentLinkedQueue<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ExecutorService workers;

    public LocalServerNetwork(
        BlockingQueue<Packet> loopbackToServerQueue,
        BlockingQueue<Packet> loopbackFromServerQueue,
        ServerNetworkListener listener,
        Dispatcher dispatcher) {
        this.loopbackToServerQueue = loopbackToServerQueue;
        this.loopbackFromServerQueue = loopbackFromServerQueue;
        this.listener = listener;
        this.dispatcher = dispatcher;

        this.workers = Executors.newSingleThreadExecutor();
    }

    @Override
    public void broadcast(Packet packet) {
        sendToClient(0, packet);
    }

    @Override
    public void sendToClient(int id, Packet packet) {
        outgoingPacketQueue.offer(packet);
    }

    @Override
    public void flush() {
        Packet pkt;
        while((pkt = outgoingPacketQueue.poll()) != null) {
            if(!loopbackFromServerQueue.offer(pkt)) {
                Logger.log(Logger.NETWORK, "Packet dropped: " + pkt);
            }
        }
    }

    private void runLoop() {
        try {
            while (running.get()) {
                // Blocking wait for any packet
                Packet pkt = loopbackToServerQueue.poll(10, TimeUnit.MILLISECONDS);
                if (pkt != null) {
                    handlePacket(pkt);
                }
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void handlePacket(Packet pkt) {
        if(pkt.isFastHandled()) {
            HandlerRegistry.getHandler(pkt).handle(0, pkt);
        } else {
            dispatcher.dispatch(() -> HandlerRegistry.getHandler(pkt).handle(0, pkt));
        }
    }

    @Override
    public boolean start() {
        if(running.getAndSet(true)) return true;

        workers.submit(this::runLoop);

        Logger.log(Logger.SERVER, "Server-Network started");

        dispatcher.dispatch(() -> listener.onClientConnected(0));

        return true;
    }

    @Override
    public void shutdown() {
        if(!running.getAndSet(false)) return;

        dispatcher.dispatch(() -> listener.onClientDisconnected(0));

        workers.shutdown();

        boolean shutdown = false;

        try {
            shutdown = workers.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error(Logger.NETWORK, "Error in ServerNetwork shutdown", e);
            Thread.currentThread().interrupt();
        }

        if(!shutdown) workers.shutdownNow();

        Logger.log(Logger.SERVER, "Server-Network shutdown");
    }

}
