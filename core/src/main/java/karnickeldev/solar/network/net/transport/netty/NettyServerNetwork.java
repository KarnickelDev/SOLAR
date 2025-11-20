package karnickeldev.solar.network.net.transport.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.*;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import karnickeldev.solar.network.net.core.ServerNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ServerNetworkListener;
import karnickeldev.solar.network.packets.HandshakePacket;
import karnickeldev.solar.network.packets.HandshakeResponsePacket;
import karnickeldev.solar.network.packets.Packet;
import karnickeldev.solar.network.packets.PacketTypes;
import karnickeldev.solar.util.Logger;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KarnickelDev
 * @since 26.06.2025
 **/
public class NettyServerNetwork implements ServerNetwork {

    private final int port;

    private final Map<ChannelId, ClientSession> sessions = new ConcurrentHashMap<>();
    private final Map<Integer, ClientSession> clientIdMap = new ConcurrentHashMap<>();

    private final AtomicInteger clientIdCounter = new AtomicInteger(1);

    private final Queue<PacketContext> outgoing = new ConcurrentLinkedQueue<>();

    private final ServerNetworkListener listener;
    private final Dispatcher dispatcher;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyServerNetwork(int port, ServerNetworkListener listener, Dispatcher dispatcher) {
        this.port = port;
        this.listener = listener;
        this.dispatcher = dispatcher;
    }

    @Override
    public boolean start() {
        bossGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        workerGroup = new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                        ChannelPipeline p = ch.pipeline();
                        //p.addLast(new LengthFieldPrepender(4));
                        p.addLast(new PacketDecoder());
                        p.addLast(new PacketEncoder());
                        p.addLast(new IdleStateHandler(20,0,0));
                        p.addLast(new ServerHandler());
                    }
                });
            serverChannel = b.bind(port).sync().channel();
            serverChannel.config().setOption(ChannelOption.TCP_NODELAY, true);
        } catch (Exception e) {
            Logger.error(Logger.NETWORK, "Error starting Server", e);
            return false;
        }

        return true;
    }

    @Override
    public void shutdown() {
        try {

            for(ClientSession session: clientIdMap.values()) {
                session.close();
            }

            serverChannel.close().sync();
            workerGroup.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();

            Logger.error(Logger.SERVER, "Server shutdown, bye!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error(Logger.SERVER, "Interrupted during shutdown", e);
        } catch (Exception e) {
            Logger.error(Logger.SERVER, "Error during shutdown", e);
        }
    }

    @Override
    public void broadcast(Packet packet) {
        sessions.values().forEach(session -> sendToClient(session.getClientId(), packet));
    }

    @Override
    public void sendToClient(int clientId, Packet packet) {
        if(packet == null) return;
//        if(!clientIdMap.get(clientId).channel.isWritable()) {
//            Logger.error(Logger.NETWORK + "Packet dropped (channel not writable): " + packet);
//            return;
//        }
        //Logger.log("packet queue: " + outgoing.size());
        if(!clientIdMap.get(clientId).isHandshake() || !outgoing.offer(new PacketContext(clientId, packet))) {
            Logger.error(Logger.NETWORK + "Packet dropped: " + packet);
        }
    }

    @Override
    public void flush() {
        PacketContext pkt;

        while((pkt = outgoing.poll()) != null) {
            ClientSession session = clientIdMap.get(pkt.clientId);
            if(session != null) session.send(pkt.packet);
        }
    }

    private static class PacketContext {
        private final int clientId;
        private final Packet packet;

        protected PacketContext(int clientId, Packet pkt) {
            this.clientId = clientId;
            this.packet = pkt;
        }
    }

    private class ServerHandler extends SimpleChannelInboundHandler<Packet> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            int id = clientIdCounter.getAndIncrement();
            if(sessions.containsKey(ctx.channel().id())) {
                throw new IllegalStateException("Double client registration");
            } else {
                ClientSession session = new ClientSession(id, ctx.channel());
                sessions.put(ctx.channel().id(), session);
                clientIdMap.put(id, session);
                dispatcher.dispatch(() -> listener.onClientConnected(id));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            ClientSession session = sessions.remove(ctx.channel().id());
            if (session != null) {
                clientIdMap.remove(session.getClientId());
                dispatcher.dispatch(() -> listener.onClientDisconnected(session.getClientId()));
            }
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            ClientSession session = sessions.get(ctx.channel().id());
            if (session != null && msg != null) {

                if(msg.getType() == PacketTypes.HANDSHAKE.getType()) {
                    HandshakePacket pkt = (HandshakePacket) msg;

                    if(!pkt.getPassword().equals("password")) {
                        Logger.error(Logger.NETWORK, "Client " + session.getClientId() + " failed handshake");
                        ctx.channel().writeAndFlush(new HandshakeResponsePacket(HandshakeResponsePacket.FAILURE))
                            .addListener(ChannelFutureListener.CLOSE);
                    } else {
                        Logger.log(Logger.NETWORK, "Handshake accepted for client " + session.getClientId());
                        session.completeHandshake();
                        ctx.channel().writeAndFlush(new HandshakeResponsePacket(HandshakeResponsePacket.SUCCESS));
                    }
                    return;
                }

                if(session.isHandshake()) {
                    if(msg.isFastHandled()) {
                        HandlerRegistry.getHandler(msg).handle(session.getClientId(), msg);
                    } else {
                        dispatcher.dispatch(() -> HandlerRegistry.getHandler(msg).handle(session.getClientId(), msg));
                    }
                } else {
                    Logger.error(Logger.NETWORK, "Packet dropped: incomplete handshake" + msg);
                }
            } else {
                Logger.error(Logger.NETWORK, "channelRead0 with null ChannelHandlerContext or Packet");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Logger.error(cause.getMessage());
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.READER_IDLE) {
                    Logger.log(Logger.NETWORK, "Client " + sessions.get(ctx.channel().id()).getClientId() + " timed out!");
                    ctx.close();
                }
            }
        }

    }
}

