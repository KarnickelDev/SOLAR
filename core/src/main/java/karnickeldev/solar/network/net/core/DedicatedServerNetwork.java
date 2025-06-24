package karnickeldev.solar.network.net.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import karnickeldev.solar.network.net.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.network.net.listener.ServerNetworkListener;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.util.Logger;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author : KarnickelDev
 * @since : 21.06.2025
 **/
public class DedicatedServerNetwork implements ServerNetwork {

    private final int port;

    private final ServerNetworkListener listener;
    private final MainThreadDispatcher dispatcher;

    private DatagramSocket socket;

    private final Map<Integer, SocketAddress> clients = new HashMap<>();
    private final Queue<DatagramPacket> outgoingQueue = new ConcurrentLinkedQueue<>();

    public DedicatedServerNetwork(int port, ServerNetworkListener listener, MainThreadDispatcher dispatcher) {
        this.port = port;
        this.listener = listener;
        this.dispatcher = dispatcher;
    }

    @Override
    public void broadcast(Packet packet) {
        for (SocketAddress address : clients.values()) {
            sendPacketToAddress(packet, address);
        }
    }

    @Override
    public void sendToClient(int id, Packet packet) {
        SocketAddress address = clients.get(id);
        if (address != null) {
            sendPacketToAddress(packet, address);
        }
    }

    private void sendPacketToAddress(Packet packet, SocketAddress address) {
            ByteBuf dos = Unpooled.buffer();
            dos.writeShort(packet.getType());
            packet.write(dos);
            byte[] data = dos.array();
            DatagramPacket datagram = new DatagramPacket(data, data.length, address);
            outgoingQueue.add(datagram);
            //Logger.log(Logger.SERVER, "sending packet " + PacketTypes.values()[packet.getType()].getPacketClass().getSimpleName());
    }

    @Override
    public boolean updateNetwork() {
        boolean activity = false;

        if(socket == null) return activity;

        // Send queued packets
        while (!outgoingQueue.isEmpty()) {
            DatagramPacket toSend = outgoingQueue.poll();
            try {
                socket.send(toSend);
                activity = true;
            } catch (IOException e) {
                System.err.println("Failed to send packet: " + e.getMessage());
            }
        }

        // Receive incoming packets
        try {
            byte[] buffer = new byte[65507];
            DatagramPacket received = new DatagramPacket(buffer, buffer.length);
            socket.receive(received);
            handleIncomingPacket(received);
            activity = true;
        } catch (SocketTimeoutException ignore) {
            // no packet received in timeout period — not an error
        } catch (IOException e) {
            System.err.println("Failed to receive UDP packet: " + e.getMessage());
        }

        return activity;
    }

    volatile boolean running = false;

    @Override
    public void start() {
        if(running) return;
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(1); // non-blocking poll
            running = true;
            Logger.log(Logger.SERVER, "DedicatedServerNetwork started on port " + port);
        } catch (SocketException e) {
            throw new RuntimeException("Failed to bind UDP socket", e);
        }
    }

    @Override
    public void shutdown() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        clients.clear();

        Logger.log(Logger.SERVER, "Server-Network shutdown");
    }

    private void handleIncomingPacket(DatagramPacket datagram) {
        ByteBuf dis = Unpooled.copiedBuffer(datagram.getData());
            short type = dis.readShort();
            Packet packet = PacketRegistry.create(type, dis);
            int clientId = datagram.getPort(); // or another field that identifies the sender
            clients.putIfAbsent(clientId, datagram.getSocketAddress());
            handlePacket(packet, clientId);
    }

    private void handlePacket(Packet packet, int clientId) {
        if(packet.isFastHandled()) {
            listener.onPacketReceived(clientId, packet);
        } else {
            dispatcher.dispatch(() -> listener.onPacketReceived(clientId, packet));
        }
    }

}
