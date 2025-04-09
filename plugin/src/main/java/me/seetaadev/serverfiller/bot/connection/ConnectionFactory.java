package me.seetaadev.serverfiller.bot.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;
import me.seetaadev.serverfiller.bot.channel.BotChannelHandler;
import me.seetaadev.serverfiller.bot.channel.DummyCompressHandler;
import me.seetaadev.serverfiller.bot.channel.DummyDecoder;
import me.seetaadev.serverfiller.bot.channel.DummyDecompressHandler;
import me.seetaadev.serverfiller.bot.channel.DummyPacketEncoder;
import me.seetaadev.serverfiller.bot.channel.FakeHandshakeHandler;
import me.seetaadev.serverfiller.bot.channel.FakeLoginHandler;
import me.seetaadev.serverfiller.bot.channel.OutboundConfigHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

import java.net.InetSocketAddress;
import java.util.UUID;

public class ConnectionFactory {
    private final String hostname;
    private final int port;

    private static final EventLoopGroup GROUP = new DefaultEventLoopGroup();
    private static final LocalAddress LOCAL_ADDRESS = new LocalAddress("bot_channel");

    private static boolean serverInitialized = false;

    public ConnectionFactory(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;

        if (!serverInitialized) {
            initLocalServer();
            serverInitialized = true;
        }
    }

    private void initLocalServer() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(GROUP)
                .channel(LocalServerChannel.class)
                .childHandler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel ch) {
                    }
                });

        bootstrap.bind(LOCAL_ADDRESS).syncUninterruptibly();
    }

    public Connection createConnection(String name, UUID uuid) {
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        connection.address = new InetSocketAddress(hostname, port);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(GROUP)
                .channel(LocalChannel.class)
                .handler(new ChannelInitializer<LocalChannel>() {
                    @Override
                    protected void initChannel(LocalChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new DummyDecoder());
                        pipeline.addLast("encoder", new DummyPacketEncoder());
                        pipeline.addLast("decompress", new DummyDecompressHandler());
                        pipeline.addLast("compress", new DummyCompressHandler());
                        pipeline.addLast("handshake_handler", new FakeHandshakeHandler());
                        pipeline.addLast("login_handler", new FakeLoginHandler());
                        pipeline.addLast("outbound_config", new OutboundConfigHandler());
                        pipeline.addLast("packet_handler", new BotChannelHandler(connection, hostname, port, name, uuid));
                    }
                });

        ChannelFuture future = bootstrap.connect(LOCAL_ADDRESS).syncUninterruptibly();
        connection.channel = future.channel();

        return connection;
    }
}
