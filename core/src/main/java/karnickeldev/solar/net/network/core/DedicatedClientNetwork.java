package karnickeldev.solar.net.network.core;

import karnickeldev.solar.net.network.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.net.network.listener.ClientNetworkListener;
import karnickeldev.solar.net.packets.*;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ServerWorld;
import karnickeldev.solar.world.WorldManager;

import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author : KarnickelDev
 * @since : 21.06.2025
 **/
public class DedicatedClientNetwork implements ClientNetwork {

    private final int port;
    private final ClientNetworkListener listener;
    private final MainThreadDispatcher dispatcher;

    private volatile boolean connected = false;

    private final Queue<DatagramPacket> outgoingPackets = new ConcurrentLinkedQueue<>();

    private DatagramSocket socket;

    private final InetSocketAddress address;

    public DedicatedClientNetwork(int port, ClientNetworkListener listener, MainThreadDispatcher dispatcher) {
        this.port = port;
        this.listener = listener;
        this.dispatcher = dispatcher;

        this.address = new InetSocketAddress("localhost", this.port);
    }


    public void connect() {
        try {
            socket = new DatagramSocket(); // Bind to any available port
            socket.setSoTimeout(1); // Non-blocking poll
            connected = true;
            System.out.println("Connected to server at " + "localhost");
            send(PacketFactory.createPingPacket(System.nanoTime()));
            send(new FullSnapshotRequestPacket(1));
        } catch (SocketException e) {
            throw new RuntimeException("Failed to open UDP socket", e);
        }
    }

    public void disconnect() {
        connected = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public void send(Packet packet) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeShort(packet.getType());
            packet.serialize(dos);
            byte[] data = baos.toByteArray();
            DatagramPacket datagram = new DatagramPacket(data, data.length, address);
            outgoingPackets.add(datagram);
        } catch (IOException e) {
            System.err.println("Failed to encode packet: " + e.getMessage());
        }
    }

    public boolean updateNetwork() {
        if (!isConnected()) return false;

        boolean activity = false;

        // Send outgoing packets
        while (!outgoingPackets.isEmpty()) {
            DatagramPacket toSend = outgoingPackets.poll();
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

            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
                received.getData(), 0, received.getLength()))) {
                short type = dis.readShort();
                Packet packet = PacketRegistry.deserializePacket(type, dis);
                //Logger.log(Logger.GENERAL, "received packet " + PacketTypes.values()[packet.getType()].getPacketClass().getSimpleName());
                handlePacket(packet);
                activity = true;

            } catch (IOException e) {
                System.err.println("Failed to decode packet: " + e.getMessage());
            }

        } catch (SocketTimeoutException ignore) {
            // No packet received during timeout — not an error
        } catch (IOException e) {
            if (connected) {
                System.err.println("Failed to receive UDP packet: " + e.getMessage());
            }
        }

        return activity;
    }

    private void handlePacket(Packet packet) {
        if(packet.isFastHandled()) {
            listener.onPacketReceived(packet);
        } else {
            dispatcher.dispatch(() -> listener.onPacketReceived(packet));
        }
    }
}
