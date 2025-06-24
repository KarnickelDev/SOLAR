package karnickeldev.solar.network.net.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.Unpooled;
import karnickeldev.solar.network.net.dispatcher.MainThreadDispatcher;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.*;

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
        ByteBuf dos = Unpooled.buffer();
        dos.writeShort(packet.getType());
        packet.write(dos);
        byte[] data = dos.array();
        DatagramPacket datagram = new DatagramPacket(data, data.length, address);
        outgoingPackets.add(datagram);

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

            ByteBuf dis = Unpooled.copiedBuffer(received.getData());

            short type = dis.readShort();
            Packet packet = PacketRegistry.create(type, dis);
            //Logger.log(Logger.GENERAL, "received packet " + PacketTypes.values()[packet.getType()].getPacketClass().getSimpleName());
            handlePacket(packet);
            activity = true;



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
