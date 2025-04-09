package me.seetaadev.serverfiller.bot.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.seetaadev.serverfiller.bot.channel.utils.ByteBufUtils;

import java.util.List;
import java.util.zip.Inflater;

public class CompressionDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final int threshold;

    public CompressionDecoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        int uncompressedSize = ByteBufUtils.readVarInt(buf); // varint con tamaño descomprimido

        if (uncompressedSize == 0) {
            out.add(buf.readBytes(buf.readableBytes()));
            return;
        }

        if (uncompressedSize < threshold) {
            throw new DecoderException("Badly compressed packet - size below threshold: " + uncompressedSize);
        }

        ByteBuf uncompressed = ctx.alloc().buffer(uncompressedSize);
        Inflater inflater = new Inflater();
        try {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            inflater.setInput(bytes);

            byte[] result = new byte[uncompressedSize];
            int resultLength = inflater.inflate(result);

            if (!inflater.finished()) {
                throw new DecoderException("Incomplete decompression");
            }

            uncompressed.writeBytes(result, 0, resultLength);
            out.add(uncompressed);
        } finally {
            inflater.end();
        }
    }
}

