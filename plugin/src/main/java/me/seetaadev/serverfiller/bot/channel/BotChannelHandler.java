package me.seetaadev.serverfiller.bot.channel;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;

import java.util.UUID;

public class BotChannelHandler extends ChannelInboundHandlerAdapter {

    private final Connection connection;
    private final String hostname;
    private final int port;
    private final String name;
    private final UUID uuid;

    public BotChannelHandler(Connection connection, String hostname, int port, String name, UUID uuid) {
        this.connection = connection;
        this.hostname = hostname;
        this.port = port;
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(765);
        buffer.writeUtf(hostname);
        buffer.writeShort(port);
        buffer.writeVarInt(ClientIntent.LOGIN.id());

        ClientIntentionPacket handshake = ClientIntentionPacket.STREAM_CODEC.decode(buffer);
        connection.send(handshake);

        ServerboundHelloPacket loginStart = new ServerboundHelloPacket(name, uuid);
        connection.send(loginStart);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
