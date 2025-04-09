package me.seetaadev.serverfiller.bot.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class DummyDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Simula un paquete vacío (o puedes implementar un parser si quieres realismo)
        if (in.readableBytes() > 0) {
            ByteBuf copied = in.readBytes(in.readableBytes());
            out.add(copied);
        }
    }
}

