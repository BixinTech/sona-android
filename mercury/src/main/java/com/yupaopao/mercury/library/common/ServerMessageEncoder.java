package com.yupaopao.mercury.library.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author qinwei
 */
@ChannelHandler.Sharable
public class ServerMessageEncoder extends MessageToByteEncoder<AccessMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, AccessMessage msg, ByteBuf out) throws Exception {
        MessageCodec.encode(out, msg);
    }

}
