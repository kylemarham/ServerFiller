package me.seetaadev.serverfiller.bot.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.seetaadev.serverfiller.bot.channel.utils.ByteBufUtils;

import java.util.zip.Deflater;

public class CompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private final int threshold;

    public CompressionEncoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        int size = msg.readableBytes();

        if (size < threshold) {
            ByteBufUtils.writeVarInt(out, 0);
            out.writeBytes(msg);
            return;
        }

        byte[] input = new byte[size];
        msg.readBytes(input);

        ByteBuf compressed = ctx.alloc().buffer();
        Deflater deflater = new Deflater();
        try {
            deflater.setInput(input);
            deflater.finish();

            byte[] buffer = new byte[8192];
            while (!deflater.finished()) {
                int bytesCompressed = deflater.deflate(buffer);
                compressed.writeBytes(buffer, 0, bytesCompressed);
            }

            ByteBufUtils.writeVarInt(out, input.length);
            out.writeBytes(compressed);
        } finally {
            deflater.end();
        }
    }
}

