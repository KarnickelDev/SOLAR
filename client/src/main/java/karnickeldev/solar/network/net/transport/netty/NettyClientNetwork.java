package karnickeldev.solar.network.net.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import karnickeldev.solar.context.GameContext;
import karnickeldev.solar.network.net.core.ClientNetwork;
import karnickeldev.solar.network.net.dispatcher.Dispatcher;
import karnickeldev.solar.network.net.handlers.HandlerRegistry;
import karnickeldev.solar.network.net.listener.ClientNetworkListener;
import karnickeldev.solar.network.net.transport.ClientNetworkTracker;
import karnickeldev.solar.network.net.transport.NetworkTracker;
import karnickeldev.solar.network.packets.*;
import karnickeldev.solar.network.sync.PacketSyncLayer;
import karnickeldev.solar.util.Logger;
import karnickeldev.solar.world.ClientClock;

/**
 * @author : KarnickelDev
 * @since : 26.06.2025
 **/
public class NettyClientNetwork implements ClientNetwork {

    private final String host;
    private final int port;
    private final ClientNetworkListener listener;

    private EventLoopGroup group;
    private Channel channel;

    private volatile boolean handshake = false;
    private volatile boolean connected = false;

    private final Dispatcher dispatcher;

    private final PacketSyncLayer syncLayer;

    private final ClientClock clientClock;

    public NettyClientNetwork(String host, int port, ClientNetworkListener listener, Dispatcher dispatcher,
                              PacketSyncLayer syncLayer, ClientClock clientClock) {
        this.host = host;
        this.port = port;
        this.listener = listener;
        this.dispatcher = dispatcher;
        this.syncLayer = syncLayer;
        this.clientClock = clientClock;
    }

    @Override
    public boolean connect() {
        NetworkTracker networkTracker = new ClientNetworkTracker();

        group = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    ch.config().setAllocator(PooledByteBufAllocator.DEFAULT);
                    p.addLast(new PacketDecoder(networkTracker));
                    p.addLast(new PacketEncoder(networkTracker));
                    p.addLast(new IdleStateHandler(5,0,0));
                    p.addLast(new ClientHandler());
                }
            });

        try {
            ChannelFuture future = b.connect(host, port).sync();
            if (!future.isSuccess()) {
                Logger.error(Logger.NETWORK, "Failed to connect: " + future.cause());
                return false;
            }

            channel = future.channel();
            channel.config().setOption(ChannelOption.TCP_NODELAY, true);
            connected = true;

            channel.writeAndFlush(new HandshakePacket("test", "password"));
            Logger.log(Logger.NETWORK, "Connected to server, handshake sent");

            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logger.error(Logger.NETWORK, "Connect interrupted");
        } catch (Exception e) {
            Logger.error(Logger.NETWORK, "Connect failed: " + e.getMessage());
        }

        // cleanup if failed
        shutdown(true);

        return false;
    }

    @Override
    public void disconnect() {
        shutdown(false);
    }

    /**
     * Initiates clean network shutdown
     * @param ignoreConnectedFlag Set True with care: will allow double shutdown.
     * Should only be used in connect() (because connect flag isn't yet setup properly there)
     */
    private void shutdown(boolean ignoreConnectedFlag) {
        if(!connected && !ignoreConnectedFlag) return;
        connected = false;

        if(handshake && isConnected()) {
            try {
                channel.writeAndFlush(new DisconnectPacket("bye")).sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.error(Logger.NETWORK, "Interrupted while sending disconnect");
            } catch (Exception e) {
                Logger.error(Logger.NETWORK, "Failed to send DisconnectPacket: " + e.getMessage());
            }
        }

        try {
            if(channel != null) channel.close().sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if(group != null) group.shutdownGracefully();

        dispatcher.dispatch(listener::onDisconnected);
    }

    @Override
    public boolean isConnected() {
        return connected && channel != null && channel.isActive();
    }

    @Override
    public void send(Packet pkt) {
        if (!isConnected()) {
            Logger.error(Logger.NETWORK, "Packet dropped: not connected");
            return;
        }

        // Dispatch onto the Netty event loop thread
        channel.eventLoop().execute(() -> {
            if (isConnected()) {
                channel.writeAndFlush(pkt);
            } else {
                Logger.error(Logger.NETWORK, "Channel inactive during send");
            }
        });
    }

    private void handlePacket(Packet pkt) {
        if(pkt.isFastHandled()) {
            HandlerRegistry.getHandler(pkt).handle(0, pkt);
        } else if(pkt instanceof GameStatePacket gp) {
            GameContext.get().getSyncLayer().receivePacket(gp);
        } else {
            dispatcher.dispatch(() -> HandlerRegistry.getHandler(pkt).handle(0, pkt));
        }
    }

    private class ClientHandler extends SimpleChannelInboundHandler<Packet> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
            if(msg == null) return;

            if(msg.getType() == PacketTypes.HANDSHAKE_RESPONSE.getType()) {
                HandshakeResponsePacket response = (HandshakeResponsePacket) msg;
                if(!response.isSuccess()) {
                    Logger.error(Logger.NETWORK, "Handshake Failed!");
                    shutdown(false);
                } else {
                    Logger.log(Logger.NETWORK, "Handshake success!");
                    handshake = true;
                    dispatcher.dispatch(listener::onConnected);
                }
                return;
            }

            handlePacket(msg);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            Logger.log(Logger.NETWORK, "Connection closed");
            shutdown(false);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Logger.error(Logger.NETWORK, "Exception: " + cause.getMessage());
            ctx.close();
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if(evt instanceof IdleStateEvent) {
                IdleStateEvent e = (IdleStateEvent) evt;
                if(e.state() == IdleState.READER_IDLE) {
                    Logger.log(Logger.NETWORK, "Connection timed out! (idle)");

                    ctx.close(); // triggers shutdown via channelInactive
                }
            }
        }
    }
}

