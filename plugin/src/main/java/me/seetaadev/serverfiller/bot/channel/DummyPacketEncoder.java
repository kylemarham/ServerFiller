package me.seetaadev.serverfiller.bot.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.protocol.Packet;

public class DummyPacketEncoder extends MessageToByteEncoder<Packet<?>> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> packet, ByteBuf out) {
    }
}
