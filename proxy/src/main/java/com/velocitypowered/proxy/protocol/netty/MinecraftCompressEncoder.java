package com.velocitypowered.proxy.protocol.netty;

import com.velocitypowered.natives.compression.VelocityCompressor;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MinecraftCompressEncoder extends MessageToByteEncoder<ByteBuf> {
    private final int threshold;
    private final VelocityCompressor compressor;

    public MinecraftCompressEncoder(int threshold, VelocityCompressor compressor) {
        this.threshold = threshold;
        this.compressor = compressor;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int uncompressed = msg.readableBytes();
        if (uncompressed <= threshold) {
            // Under the threshold, there is nothing to do.
            ProtocolUtils.writeVarInt(out, 0);
            out.writeBytes(msg);
        } else {
            ProtocolUtils.writeVarInt(out, uncompressed);
            compressor.deflate(msg, out);
        }
    }

    @Override
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) throws Exception {
        if (msg.readableBytes() <= threshold) {
            return ctx.alloc().directBuffer(msg.readableBytes() + 1);
        }
        // A reasonable assumption about compression savings
        return ctx.alloc().directBuffer(msg.readableBytes() / 3);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        compressor.dispose();
    }
}
