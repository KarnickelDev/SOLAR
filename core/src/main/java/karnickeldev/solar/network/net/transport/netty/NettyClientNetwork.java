package karnickeldev.solar.network.net.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import karnickeldev.solar.context.ServerContext;
import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.util.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author : KarnickelDev
 * @since : 26.06.2025
 **/
public class NettyClientNetwork implements ClientNetwork {

    private final String host;
    private final int port;
    private final ClientNetworkListener listener;
    private final Queue<Packet> outgoing = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> incoming = new ConcurrentLinkedQueue<>();

    private EventLoopGroup group;
    private Channel channel;
    private boolean handshake = false;

    private final Dispatcher dispatcher;

    private final PacketSyncLayer syncLayer;

    public NettyClientNetwork(String host, int port, ClientNetworkListener listener, Dispatcher dispatcher, PacketSyncLayer syncLayer) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        this.dispatcher = dispatcher;
        this.syncLayer = syncLayer;
    }

    @Override
    public boolean connect() {
        group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    //p.addLast(new LengthFieldPrepender(4));
                    p.addLast(new PacketDecoder());
                    p.addLast(new PacketEncoder());
                    p.addLast(new IdleStateHandler(5,0,0));
                    p.addLast(new ClientHandler());
                }
            });

        ChannelFuture future = b.connect(host, port);
        try {
            future.sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error(Logger.NETWORK, "Failed to connect to server");
            return false;
        } catch (Exception e) {
            Logger.error(Logger.NETWORK, "Failed to connect to server");
            return false;
        }

        if(future.isSuccess()) {
            channel = future.channel();
            channel.writeAndFlush(new HandshakePacket("test", "password"));
            Logger.log(Logger.NETWORK, "Connecting to server...");
        } else {
            Logger.error(Logger.NETWORK, future.cause().getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void disconnect() {
        try {
            if(channel.isActive() && handshake) channel.writeAndFlush(new DisconnectPacket("bye")).sync();
        } catch (InterruptedException e) {
            Logger.error(Logger.NETWORK, "Sending DisconnectPacket failed");
            Thread.currentThread().interrupt();
        }


        try {
            if (channel != null && channel.isActive()) channel.close().sync();
            if (group != null) group.shutdownGracefully();
        } catch (InterruptedException e) {
            Logger.error(Logger.NETWORK, e.getMessage());
            Thread.currentThread().interrupt();
        }

    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isActive();
    }

    @Override
    public void send(Packet packet) {
        if(!isConnected()) return;

        if(handshake) {
            outgoing.add(packet);
        } else {
            Logger.error(Logger.NETWORK, "Packet dropped: incomplete handshake");
        }
    }

    @Override
    public boolean updateNetwork() {
        Packet pkt;

        while ((pkt = outgoing.poll()) != null && isConnected()) {
            channel.writeAndFlush(pkt);
        }

        while ((pkt = incoming.poll()) != null && isConnected()) {
            if(pkt.isFastHandled()) {
                //listener.onPacketReceived(pkt);
                HandlerRegistry.getHandler(pkt).handle(0, pkt);
            } else {
                if(pkt instanceof GameStatePacket) {
                    GameStatePacket p = (GameStatePacket) pkt;
                    syncLayer.receivePacket(p);
                } else {
                    Packet p = pkt;
                    dispatcher.dispatch(() -> HandlerRegistry.getHandler(p).handle(0, p));
                }
            }
        }

        return true;
    }

    private class ClientHandler extends SimpleChannelInboundHandler<Packet> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            if(msg != null) {

                if(msg.getType() == PacketTypes.HANDSHAKE_RESPONSE.getType()) {
                    HandshakeResponsePacket pkt = (HandshakeResponsePacket) msg;
                    if(!pkt.isSuccess()) {
                        Logger.error(Logger.NETWORK, "Handshake Failed!");
                        disconnect();
                    } else {
                        handshake = true;
                        dispatcher.dispatch(listener::onConnected);
                    }
                    return;
                }

                if(!incoming.offer(msg)) {
                    Logger.error(Logger.NETWORK + "Packet dropped: " + msg);
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            dispatcher.dispatch(listener::onDisconnected);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Logger.error(Logger.NETWORK, cause.getMessage());
            ctx.close();
            disconnect();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.READER_IDLE) {
                    Logger.log(Logger.NETWORK, "Connection timed out!");
                    ctx.close();
                }
            }
        }
    }
}

