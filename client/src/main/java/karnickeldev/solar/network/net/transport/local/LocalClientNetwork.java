package karnickeldev.solar.network.net.transport.local;

import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.ECSUpdateHandler;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.ECSUpdatePacket;
import karnickeldev.solar.network.packets.GameStatePacket;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.util.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocalClientNetwork implements ClientNetwork {

    private final BlockingQueue<Packet> loopbackToServerQueue;
    private final BlockingQueue<Packet> loopbackFromServerQueue;
    private final ClientNetworkListener listener;
    private final Dispatcher dispatcher;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ExecutorService workers;

    public LocalClientNetwork(
        BlockingQueue<Packet> loopbackToServerQueue,
        BlockingQueue<Packet> loopbackFromServerQueue,
        ClientNetworkListener listener,
        Dispatcher dispatcher) {
        this.loopbackToServerQueue = loopbackToServerQueue;
        this.loopbackFromServerQueue = loopbackFromServerQueue;
        this.listener = listener;
        this.dispatcher = dispatcher;
        this.workers = Executors.newSingleThreadExecutor();
    }

    @Override
    public void send(Packet packet) {
        if(!loopbackToServerQueue.offer(packet)) {
            Logger.error("Packet dropped: " + packet);
        }
    }


    @Override
    public boolean connect() {
        if(running.getAndSet(true)) return true;

        dispatcher.dispatch(listener::onConnected);

        workers.submit(this::runLoop);

        return true;
    }

    private void runLoop() {
        try {
            while (running.get()) {
                // Blocking wait for any packet
                Packet pkt = loopbackFromServerQueue.poll(10, TimeUnit.MILLISECONDS);
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
            if(pkt instanceof ECSUpdatePacket) {
                ECSUpdatePacket p = (ECSUpdatePacket) pkt;
                GameContext.get().getSyncLayer().receivePacket(p);
            } else {
                dispatcher.dispatch(() -> HandlerRegistry.getHandler(pkt).handle(0, pkt));
            }
        }
    }

    @Override
    public void disconnect() {
        if(!running.getAndSet(false)) return;

        dispatcher.dispatch(listener::onDisconnected);
        workers.shutdownNow();
    }

    @Override
    public boolean isConnected() {
        return running.get();
    }

}
