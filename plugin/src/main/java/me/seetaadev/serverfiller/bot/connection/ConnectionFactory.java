package me.seetaadev.serverfiller.bot.connection;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import me.seetaadev.serverfiller.bot.channel.BotChannelHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

import java.net.InetSocketAddress;

public class ConnectionFactory {
    private final String hostname;
    private final int port;

    public ConnectionFactory(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public Connection createConnection() {
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        connection.address = new InetSocketAddress(hostname, port);
        connection.channel = new EmbeddedChannel(new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) { }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                promise.setSuccess();
            }
        });
        connection.channel.pipeline().addLast(new BotChannelHandler());
        return connection;
    }
}
